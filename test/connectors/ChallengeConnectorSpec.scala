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

package connectors

import connectors.challenge.ChallengeConnector
import controllers.{DefaultPaginationParams, VoaPropertyLinkingSpec}
import models._
import models.challenge.myclients.{ChallengeCaseWithClient, ChallengeCasesWithClient}
import models.challenge.myorganisations.{ChallengeCaseWithAgent, ChallengeCasesWithAgent}
import models.dvr.cases.check.myclients.CheckCasesWithClient
import models.dvr.cases.check.myorganisation.CheckCasesWithAgent
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, NotFoundException}
import org.mockito.ArgumentMatchers

import scala.concurrent.Future

class ChallengeConnectorSpec extends VoaPropertyLinkingSpec {

  implicit val hc = HeaderCarrier()

  class Setup {
    val connector = new ChallengeConnector(servicesConfig, mockWSHttp) {
      override lazy val baseUrl: String = "tst-url"
    }
  }

  "getMyOrganisationsChallengeCases" must "return organisation's submitted challenge cases for the given property link" in new Setup {
    mockHttpGETWithQueryParam[ChallengeCasesWithAgent]("tst-url", ownerChallengeCasesResponse)
    whenReady(connector.getMyOrganisationsChallengeCases("PL1343"))(_ mustBe List(ownerChallengeCaseDetails))
  }

  "getMyClientsChallengeCases" must "return organisation's submitted challenge cases for the given property link" in new Setup {
    mockHttpGETWithQueryParam[ChallengeCasesWithClient]("tst-url", agentChallengeCasesResponse)
    whenReady(connector.getMyClientsChallengeCases("PL1343"))(_ mustBe List(agentChallengeCaseDetails))
  }

}