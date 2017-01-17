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

package utils

import actions.AuthenticatedAction
import connectors.{AuthorisationResult, BusinessRatesAuthorisation, InvalidGGSession}
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

object StubAuthentication extends AuthenticatedAction {
  private var authorisationResult: AuthorisationResult = InvalidGGSession

  private object StubBusinessRatesAuthorisation extends BusinessRatesAuthorisation(StubHttp) {
    override def authenticate(implicit hc: HeaderCarrier) = Future.successful(authorisationResult)
  }

  def stubAuthenticationResult(result: AuthorisationResult) = {
    authorisationResult = result
  }

  def reset() = {
    authorisationResult = InvalidGGSession
  }

  override val businessRatesAuthentication: BusinessRatesAuthorisation = StubBusinessRatesAuthorisation
  override val groupAccounts = StubGroupAccountConnector
  override val individualAccounts = StubIndividualAccountConnector
}