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

package models

import org.joda.time.LocalDate
import play.api.libs.json.{Json, Reads, Writes}

case class CapacityDeclaration(capacity: CapacityType, interestedBefore2017: Boolean, fromDate: Option[LocalDate],
                               stillInterested: Boolean, toDate: Option[LocalDate] = None)

object CapacityDeclaration {
  implicit val dateTimeReads = Reads.jodaDateReads("yyyy-MM-dd")
  implicit val dateTimeWrites = Writes.jodaDateWrites("yyyy-MM-dd")
  implicit val format = Json.format[CapacityDeclaration]
}