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

package controllers

import org.scalatest.mockito.MockitoSugar
import org.scalatest.{AppendedClues, BeforeAndAfterEach, FlatSpec, MustMatchers}
import play.api.i18n.Messages
import play.api.test.{DefaultAwaitTimeout, FutureAwaits}
import utils._

trait TemplateSpec extends FlatSpec with MustMatchers with FutureAwaits with DefaultAwaitTimeout
  with BeforeAndAfterEach with AppendedClues with MockitoSugar with NoMetricsOneAppPerSuite {
  implicit lazy val messages: Messages = Messages.Implicits.applicationMessages
}