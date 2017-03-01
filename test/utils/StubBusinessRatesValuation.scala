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

import connectors.BusinessRatesValuationConnector
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

object StubBusinessRatesValuation extends BusinessRatesValuationConnector(StubHttp) {
  private var stubbedValuations: Map[Long, Boolean] = Map()

  def stubValuation(assessmentRef: Long, isViewable: Boolean) = {
    stubbedValuations = stubbedValuations.updated(assessmentRef, isViewable)
  }

  def reset() = { stubbedValuations = Map() }

  override def isViewable(authorisationId: Long, assessmentRef: Long)(implicit hc: HeaderCarrier): Future[Boolean] = Future.successful(stubbedValuations(assessmentRef))
}