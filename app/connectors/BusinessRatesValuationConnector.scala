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

import config.ApplicationConfig
import javax.inject.Inject
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, NotFoundException}
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}

class BusinessRatesValuationConnector @Inject()(config: ApplicationConfig, http: HttpClient)(
      implicit val executionContext: ExecutionContext) {

  def isViewable(uarn: Long, valuationId: Long, propertyLinkId: Long)(implicit hc: HeaderCarrier): Future[Boolean] = {
    val url = config.baseUrl("business-rates-valuation")

    http
      .GET[HttpResponse](s"$url/property-link/$propertyLinkId/assessment/$valuationId")
      .map(_ => true)
      .recover {
        case _: NotFoundException => false
      }
  }

  def isViewableExternal(uarn: Long, valuationId: Long, propertyLinkSubmissionId: String)(
        implicit hc: HeaderCarrier): Future[Boolean] = {
    val url = config.baseUrl("business-rates-valuation")
    http
      .GET[HttpResponse](
        s"$url/properties/$uarn/valuations/$valuationId",
        Seq("propertyLinkSubmissionId" -> propertyLinkSubmissionId, "projection" -> "detailed")
      )
      .map(_ => true)
      .recover {
        case _: NotFoundException => false
      }
  }
}
