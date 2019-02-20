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

package connectors.test

import config.WSHttp
import javax.inject.Inject
import play.api.Configuration
import play.api.Mode.Mode
import play.api.libs.json.{JsValue, Json, Writes}
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse}
import uk.gov.hmrc.play.config.ServicesConfig

import scala.concurrent.{ExecutionContext, Future}

class TestTaxEnrolmentConnector @Inject()(wSHttp: WSHttp, val mode: Mode, val runModeConfiguration: Configuration) extends ServicesConfig {

  private val serviceUrl = baseUrl("tax-enrolments")
  private val emacUrl = baseUrl("emac") + "/enrolment-store-proxy"

  def deEnrol(personID: Long)(implicit hc: HeaderCarrier, ex: ExecutionContext): Future[Future[HttpResponse]] =
    wSHttp.POST[JsValue, HttpResponse](s"$serviceUrl/tax-enrolments/de-enrol/HMRC-VOA-CCA", Json.obj("keepAgentAllocations" -> true))(
      implicitly[Writes[JsValue]], implicitly[HttpReads[HttpResponse]], hc.withExtraHeaders("Content-Type" -> "application/json"), ex)
      .map(_ => wSHttp.DELETE[HttpResponse](s"$emacUrl/enrolment-store/enrolments/HMRC-VOA-CCA~VOAPersonID~$personID"))

}
