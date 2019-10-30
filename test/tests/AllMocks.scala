/*
 * Copyright 2019 HM Revenue & Customs
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

package tests

import actions.AuthenticatedAction
import actions.propertylinking.WithLinkingSession
import auditing.AuditingService
import auth.GovernmentGatewayProvider
import connectors.authorisation.BusinessRatesAuthorisation
import connectors.{Addresses, DVRCaseManagementConnector}
import models.{DetailedIndividualAccount, GroupAccount}
import org.mockito.Mockito
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.Request
import repositories.{PersonalDetailsSessionRepository, SessionRepository}
import services.EnrolmentService
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.voa.propertylinking.errorhandler.CustomErrorHandler

trait AllMocks {
  self: MockitoSugar with BeforeAndAfterEach =>

  val mockAddresses: Addresses = mock[Addresses]
  val mockAuditingService: AuditingService = mock[AuditingService]
  val mockAuthConnector: AuthConnector = mock[AuthConnector]
  val mockAuthenticatedAction: AuthenticatedAction = mock[AuthenticatedAction]
  val mockBusinessRatesAuthorisation: BusinessRatesAuthorisation = mock[BusinessRatesAuthorisation]
  val mockCustomErrorHandler: CustomErrorHandler = mock[CustomErrorHandler]
  val mockDetailedIndividualAccount: DetailedIndividualAccount = mock[DetailedIndividualAccount]
  val mockDvrCaseManagement: DVRCaseManagementConnector = mock[DVRCaseManagementConnector]
  val mockEnrolmentService: EnrolmentService = mock[EnrolmentService]
  val mockRequest: Request[_] = mock[Request[_]]
  val mockGovernmentGatewayProvider: GovernmentGatewayProvider = mock[GovernmentGatewayProvider]
  val mockGroupAccount: GroupAccount = mock[GroupAccount]
  val mockSessionRepository: SessionRepository = mock[SessionRepository]
  val mockWithLinkingSession: WithLinkingSession = mock[WithLinkingSession]
  val mockPersonalDetailsSessionRepository: PersonalDetailsSessionRepository = mock[PersonalDetailsSessionRepository]


  override protected def beforeEach(): Unit =
    Seq(
      mockAddresses,
      mockAuditingService,
      mockBusinessRatesAuthorisation,
      mockAuthConnector,
      mockAuthenticatedAction,
      mockBusinessRatesAuthorisation,
      mockCustomErrorHandler,
      mockDetailedIndividualAccount,
      mockDvrCaseManagement,
      mockEnrolmentService,
      mockRequest,
      mockGovernmentGatewayProvider,
      mockGroupAccount,
      mockSessionRepository,
      mockWithLinkingSession,
      mockPersonalDetailsSessionRepository
    ).foreach(Mockito.reset(_))

}