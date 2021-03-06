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

import binders.propertylinks.GetPropertyLinksParameters
import connectors.propertyLinking.PropertyLinkConnector
import controllers.{DefaultPaginationParams, VoaPropertyLinkingSpec}
import models._
import models.dvr.cases.check.myclients.CheckCasesWithClient
import models.dvr.cases.check.myorganisation.CheckCasesWithAgent
import models.propertylinking.payload.PropertyLinkPayload
import models.propertylinking.requests.PropertyLinkRequest
import models.propertyrepresentation.AgentList
import models.searchApi.{AgentPropertiesParameters, OwnerAuthResult, OwnerAuthorisation}
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalacheck.Arbitrary._
import play.api.http.Status.OK
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, NotFoundException}
import utils._

import scala.concurrent.Future

class PropertyLinkConnectorSpec extends VoaPropertyLinkingSpec {

  implicit val hc = HeaderCarrier()

  class Setup {
    val connector = new PropertyLinkConnector(servicesConfig, mockWSHttp) {
      override lazy val baseUrl: String = "tst-url"
    }
  }

  "get" must "return a property link" in new Setup {
    val propertyLink = arbitrary[PropertyLink].sample.get.copy()

    mockHttpGETOption[PropertyLink]("tst-url", propertyLink)
    whenReady(connector.getMyOrganisationPropertyLink("11111"))(_ mustBe Some(propertyLink))
  }

  "linkToProperty" must "successfully post a property link request" in new Setup {
    val response = HttpResponse(OK)
    mockHttpPOST[PropertyLinkRequest, HttpResponse]("tst-url", response)
    whenReady(connector.createPropertyLink(mock[PropertyLinkPayload]))(_ mustBe response)
  }

  "linkToProperty" must "successfully post a property link request on client behalf" in new Setup {
    val response = HttpResponse(OK)
    val clientId = 100
    mockHttpPOST[PropertyLinkRequest, HttpResponse]("/clients/clientId/property-links", response)
    whenReady(connector.createPropertyLinkOnClientBehalf(mock[PropertyLinkPayload], clientId))(_ mustBe response)
  }

  "appointableProperties" must "return the properties appointable to an agent" in new Setup {
    val agentPropertiesParameters = AgentPropertiesParameters(agentCode = 1)
    val ownerAuthResult = OwnerAuthResult(
      start = 1,
      size = 1,
      filterTotal = 1,
      total = 1,
      authorisations = Seq(arbitrary[OwnerAuthorisation].sample.get))

    mockHttpGET[OwnerAuthResult]("tst-url", ownerAuthResult)
    whenReady(connector.appointableProperties(1, agentPropertiesParameters))(_ mustBe ownerAuthResult)
  }

  "clientProperty" must "return a client property if it exists" in new Setup {
    val clientProperty = arbitrary[ClientProperty].sample.get

    mockHttpGETOption[ClientProperty]("tst-url", clientProperty)
    whenReady(connector.clientProperty(1, 1, 1))(_ mustBe Some(clientProperty))
  }

  "clientProperty" must "return None if the client property is not found" in new Setup {
    mockHttpFailedGET[ClientProperty]("tst-url", new NotFoundException("Client property not found"))
    whenReady(connector.clientProperty(1, 1, 1))(_ mustBe None)
  }

  "getLink" must "return a property link if it exists" in new Setup {
    val propertyLink = arbitrary[PropertyLink].sample.get

    mockHttpGETOption[PropertyLink]("tst-url", propertyLink)
    whenReady(connector.getMyOrganisationPropertyLink("1"))(_ mustBe Some(propertyLink))
  }

  "getMyOrganisationAgents" must "return organisation's agents" in new Setup {
    mockHttpGET[AgentList]("tst-url", organisationsAgentsList)
    whenReady(connector.getMyOrganisationAgents())(_ mustBe organisationsAgentsList)
  }

  "getMyAgentPropertyLinks" must "return agent property links" in new Setup {
    when(mockWSHttp.GET[OwnerAuthResult](any(), any())(any(), any(), any()))
      .thenReturn(Future.successful(ownerAuthResultWithOneAuthorisation))

    connector
      .getMyAgentPropertyLinks(agentCode, GetPropertyLinksParameters(), DefaultPaginationParams)
      .futureValue mustBe ownerAuthResultWithOneAuthorisation

  }

  "getMyOrganisationPropertyLinksCount" must "return organisation's property links count" in new Setup {
    val propertyLinksCount = 1

    mockHttpGET[Int]("tst-url", propertyLinksCount)
    whenReady(connector.getMyOrganisationPropertyLinksCount())(_ mustBe propertyLinksCount)
  }

  "clientPropertyLink" must "return a client property link if it exists" in new Setup {
    val clientProperty = mock[ClientPropertyLink]

    mockHttpGETOption[ClientPropertyLink]("tst-url", clientProperty)
    whenReady(connector.clientPropertyLink("some-submission-id"))(_ mustBe Some(clientProperty))
  }

  "clientPropertyLink" must "return None if the client property link is not found" in new Setup {
    mockHttpFailedGET[ClientPropertyLink]("tst-url", new NotFoundException("Client property not found"))
    whenReady(connector.clientPropertyLink("some-submission-id"))(_ mustBe None)
  }

  "getMyOrganisationsCheckCases" must "return organisation's submitted check cases for the given property link" in new Setup {
    mockHttpGET[CheckCasesWithAgent]("tst-url", ownerCheckCasesResponse)
    whenReady(connector.getMyOrganisationsCheckCases("PL1343"))(_ mustBe List(ownerCheckCaseDetails))
  }

  "getMyClientsCheckCases" must "return organisation's submitted check cases for the given property link" in new Setup {
    mockHttpGET[CheckCasesWithClient]("tst-url", agentCheckCasesResponse)
    whenReady(connector.getMyClientsCheckCases("PL1343"))(_ mustBe List(agentCheckCaseDetails))
  }

}
