/*
 * Copyright 2020 HM Revenue & Customs
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

package connectors.authorisation.errorhandler

import config.AuthorisationFailed
import play.api.http.Status.UNAUTHORIZED
import uk.gov.hmrc.http.{HttpErrorFunctions, HttpResponse}

import scala.util.Try

trait AuthorisationHttpErrorFunctions extends HttpErrorFunctions {

  override def handleResponse(httpMethod: String, url: String)(response: HttpResponse): HttpResponse =
    response.status match {
      case UNAUTHORIZED if hasJsonBody(response) =>
        (response.json \ "errorCode").asOpt[String] match {
          case Some(str) => throw AuthorisationFailed(str)
          case None      => super.handleResponse(httpMethod, url)(response)
        }
      case _ => super.handleResponse(httpMethod, url)(response)
    }

  def hasJsonBody(response: HttpResponse) = Try(response.json).isSuccess

}
