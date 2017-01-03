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

import connectors.{PropertyRepresentation, PropertyRepresentationConnector}
import uk.gov.hmrc.play.http.HeaderCarrier
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.Future

object StubPropertyRepresentationConnector extends PropertyRepresentationConnector(StubHttp) {
  private var stubbedRepresentations: Seq[PropertyRepresentation] = Nil

  def stubRepresentation(rep: PropertyRepresentation) = stubbedRepresentations :+= rep

  def reset(): Unit = {
    stubbedRepresentations = Nil
  }

  override def get(representationId: String)(implicit hc: HeaderCarrier) = Future.successful(
    stubbedRepresentations.find(_.representationId == representationId)
  )

  override def find(linkId: String)(implicit hc: HeaderCarrier) = Future.successful {
    stubbedRepresentations.filter(_.linkId == linkId)
  }

  override def create(reprRequest: PropertyRepresentation)(implicit hc: HeaderCarrier) = Future.successful(Unit)
}
