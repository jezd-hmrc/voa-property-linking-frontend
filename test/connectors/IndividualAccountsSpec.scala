/*
 * Copyright 2018 HM Revenue & Customs
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

import controllers.VoaPropertyLinkingSpec
import models._
import org.scalacheck.Arbitrary._
import play.api.http.Status._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.{JsValue, Json}
import resources._
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import utils.StubServicesConfig

class IndividualAccountsSpec extends VoaPropertyLinkingSpec {

  implicit val hc = HeaderCarrier()

  class Setup {
    val connector = new IndividualAccounts(StubServicesConfig, mockWSHttp) {
      override lazy val baseUrl: String = "tst-url"
    }
  }

  "get" must "return a valid detailed individual account account using the person ID" in new Setup {
    val validDetailedIndividualAccount = arbitrary[DetailedIndividualAccount].sample.get

    mockHttpGETOption[DetailedIndividualAccount]("tst-url", validDetailedIndividualAccount)
    whenReady(connector.get(1))(_ mustBe Some(validDetailedIndividualAccount))
  }

  "withExternalId" must "return a valid detailed individual account using the external ID" in new Setup {
    val validDetailedIndividualAccount = arbitrary[DetailedIndividualAccount].sample.get

    mockHttpGETOption[DetailedIndividualAccount]("tst-url", validDetailedIndividualAccount)
    whenReady(connector.withExternalId("EXTERNAL_ID"))(_ mustBe Some(validDetailedIndividualAccount))
  }

  "update" must "successfully update an individual account" in new Setup {
    val validDetailedIndividualAccount = arbitrary[DetailedIndividualAccount].sample.get

    mockHttpPUT[IndividualAccount, HttpResponse]("tst-url", HttpResponse(OK))
    whenReady(connector.update(validDetailedIndividualAccount))(_ mustBe ())
  }

  "create" must "create an individual account and return the ID" in new Setup {
    val individualAccountSubmission = mock[IndividualAccountSubmission]
    val accountId = Json.obj("id" -> 1)

    mockHttpPOST[IndividualAccountSubmission, JsValue]("tst-url", accountId)
    whenReady(connector.create(individualAccountSubmission))(_ mustBe 1)
  }

}