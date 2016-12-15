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

import play.api.mvc.{Controller, Request}
import uk.gov.hmrc.play.http.HeaderCarrier
import play.api.Play.current

import scala.concurrent.{ExecutionContext, Future}

trait PropertyLinkingController extends Controller {

  implicit lazy val messages = play.api.i18n.Messages.Implicits.applicationMessages
  def showForbiddenError(implicit request: Request[_]) = Forbidden(views.html.errors.forbidden())
  def internalServerError(implicit request: Request[_]) = InternalServerError(views.html.errors.error())

  implicit def hc(implicit request: Request[_]): HeaderCarrier = HeaderCarrier.fromHeadersAndSession(request.headers, Some(request.session))
  implicit def future[A](a: A): Future[A] = Future.successful(a)
  implicit val ec: ExecutionContext = play.api.libs.concurrent.Execution.Implicits.defaultContext
}
