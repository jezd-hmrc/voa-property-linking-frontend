/*
 * Copyright 2016 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers

import config.Wiring
import models.{IVDetails, IndividualAccount}
import play.api.mvc.{Action, Request}
import uk.gov.hmrc.play.frontend.auth.AuthContext
import uk.gov.hmrc.play.http.SessionKeys

import scala.concurrent.Future

trait IdentityVerification extends PropertyLinkingController {
  val groups = Wiring().groupAccountConnector
  val individuals = Wiring().individualAccountConnector
  val userDetails = Wiring().userDetailsConnector
  val auth = Wiring().authConnector
  val ggAction = Wiring().ggAction
  val keystore = Wiring().sessionCache
  val identityVerification = Wiring().identityVerification

  def startIv = ggAction.async { _ => implicit request =>
    keystore.fetchAndGetEntry[IVDetails]("ivDetails") map {
      case Some(d) => Ok(views.html.identityVerification.start(StartIVVM(d, identityVerification.verifyUrl)))
      case None => NotFound
    }
  }

  def fail = Action { implicit request =>
    Ok(views.html.identityVerification.failed())
  }

  def succeed = Action.async { implicit request =>
    Redirect(routes.IdentityVerification.withRestoredSession).addingToSession(
      SessionKeys.authToken -> request.session.get("bearerToken").getOrElse(""),
      SessionKeys.sessionId -> request.session.get("oldSessionId").getOrElse("")
    )
  }

  def withRestoredSession = ggAction.async { implicit ctx => implicit request =>
    val journeyId = request.session.get("journeyId").getOrElse("no-id")
    identityVerification.verifySuccess(journeyId) flatMap {
      case true => continue
      case false => Unauthorized("Unauthorised")
    }
  }

  private def continue(implicit ctx: AuthContext, request: Request[_]) = {
    for {
      groupId <- userDetails.getGroupId(ctx)
      userId <- auth.getExternalId(ctx)
      account <- groups.get(groupId)
      details <- keystore.getIndividualDetails
      journeyId = request.session.get("journeyId").getOrElse("no-id")
      res <- account match {
        case Some(acc) => individuals.create(IndividualAccount(userId, journeyId, acc.id, details)) map { _ =>
          Ok(views.html.createAccount.groupAlreadyExists(acc.companyName))
        }
        case _ => Future.successful(Ok(views.html.identityVerification.success()))
      }
    } yield {
      res
    }
  }
}

object IdentityVerification extends IdentityVerification

case class StartIVVM(data: IVDetails, url: String)
