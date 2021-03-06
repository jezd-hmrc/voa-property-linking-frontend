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

package services

import org.scalatest.concurrent.{PatienceConfiguration, ScalaFutures}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{AppendedClues, BeforeAndAfterEach, FlatSpec, MustMatchers}
import play.api.i18n.MessagesApi
import play.api.test.{DefaultAwaitTimeout, FutureAwaits}
import tests.AllMocks
import utils._

trait ServiceSpec
    extends FlatSpec with MustMatchers with FutureAwaits with DefaultAwaitTimeout with BeforeAndAfterEach
    with AppendedClues with MockitoSugar with NoMetricsOneAppPerSuite with ScalaFutures with FakeObjects
    with GlobalExecutionContext with PatienceConfiguration with AllMocks {

  override implicit def patienceConfig: PatienceConfig =
    PatienceConfig(timeout = Span(1, Seconds), interval = Span(15, Millis))

  val token = "Csrf-Token" -> "nocheck"

  implicit lazy val messageApi = app.injector.instanceOf[MessagesApi]

  override protected def beforeEach(): Unit = {
    StubIndividualAccountConnector.reset()
    StubGroupAccountConnector.reset()
    StubIdentityVerification.reset()
    StubPropertyLinkConnector.reset()
    StubBusinessRatesValuation.reset()
    StubSubmissionIdConnector.reset()
    StubPropertyRepresentationConnector.reset()
  }
}
