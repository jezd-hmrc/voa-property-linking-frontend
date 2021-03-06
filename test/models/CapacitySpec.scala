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

package models

import org.scalatest.{FlatSpec, MustMatchers}
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import utils._

class CapacitySpec extends FlatSpec with MustMatchers with ScalaCheckDrivenPropertyChecks {
  "Capacity" must "create from CapacityDeclaration" in {
    forAll({ declaration: CapacityDeclaration =>
      {
        val capacity = Capacity.fromDeclaration(declaration)
        (capacity match {
          case c: Capacity => true
          case _           => false
        }) must be(true)
      }
    })
  }
}
