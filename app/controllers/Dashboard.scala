/*
 * Copyright 2017 HM Revenue & Customs
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
import models.{Assessment, CapacityType, DetailedPropertyLink, DraftCase}
import org.joda.time.{DateTime, LocalDate}
import connectors.PropertyRepresentation

import scala.concurrent.Future

trait Dashboard extends PropertyLinkingController {
  val propLinkedConnector = Wiring().propertyLinkConnector
  val reprConnector = Wiring().propertyRepresentationConnector
  val propConnector = Wiring().propertyConnector
  val individuals = Wiring().individualAccountConnector
  val groups = Wiring().groupAccountConnector
  val auth = Wiring().authConnector
  val ggAction = Wiring().ggAction
  val withAuthentication = Wiring().withAuthentication

  def home() = ggAction.async { ctx => implicit request =>
    for {
      userId <- auth.getExternalId(ctx)
      groupId <- auth.getGroupId(ctx)
      individualAccount <- individuals.get(userId)
      groupAccount <- groups.get(groupId)
    } yield {
      (individualAccount, groupAccount) match {
        case (Some(i), Some(g)) => Ok(views.html.dashboard.home(i.details, g))
        case (Some(_), None) => throw new Exception(s"User with id $userId has account but their group does not have an account (id $groupId)")
        case (None, _) => Redirect(routes.CreateIndividualAccount.show)
      }
    }
  }

  def manageProperties() = withAuthentication { implicit request =>
    propLinkedConnector.linkedProperties(request.groupAccount.id) map { props =>
      Ok(views.html.dashboard.manageProperties(ManagePropertiesVM(props)))
    }
  }

  def assessments(uarn: Long) = withAuthentication  { implicit request =>
    propLinkedConnector.assessments(uarn) map { assessments =>
      Ok(views.html.dashboard.assessments(AssessmentsVM(assessments.map(x => x.copy(address = capitalizeWords(x.address))))))
    }
  }

  def draftCases() = withAuthentication { implicit request =>
    val dummyData = Seq(
      DraftCase(146440182, "4, EX2 7LL", 123456789, new LocalDate(2017, 1, 3), "Agent ltd", "Check", new LocalDate(2017, 2, 3)),
      DraftCase(146440182, "1, RG2 9WX", 321654987, new LocalDate(2017, 1, 6), "Agent ltd", "Check", new LocalDate(2017, 2, 6))
    )
      Future.successful(Ok(views.html.dashboard.draftCases(DraftCasesVM(dummyData))))
  }

  private def capitalizeWords(text: String) = text.split(",").map(str => {
    if (str.trim.matches("[A-Z]{1,2}[0-9R][0-9A-Z]? [0-9]([A-Z]){2}") && text.endsWith(str))
      str.trim
    else
      str.toLowerCase().trim.split(" ").map(_.capitalize).mkString(" ")
  }).mkString(", ")
}

object Dashboard extends Dashboard

case class ManagePropertiesVM(properties: Seq[DetailedPropertyLink])
case class AssessmentsVM(assessments: Seq[Assessment])
case class DraftCasesVM(draftCases: Seq[DraftCase])

case class PropertyLinkRepresentations(name: String, linkId: String, capacity: CapacityType, linkedDate: DateTime,
                                       representations: Seq[PropertyRepresentation])

case class PendingPropertyLinkRepresentations(name: String, linkId: String, capacity: CapacityType,
                                              linkedDate: DateTime, representations: Seq[PropertyRepresentation])

case class LinkedPropertiesRepresentations(added: Seq[PropertyLinkRepresentations], pending: Seq[PendingPropertyLinkRepresentations])
