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

package controllers

import connectors.DraftCases
import org.mockito.ArgumentMatchers.{any, anyLong}
import org.mockito.Mockito.when
import play.api.http.Status._
import play.api.test.FakeRequest
import play.api.test.Helpers.{redirectLocation, status}
import uk.gov.hmrc.http.HeaderCarrier
import utils.{StubPropertyLinkConnector, randomDraftCase}

import scala.concurrent.Future

class ManageDraftCasesSpec extends VoaPropertyLinkingSpec {

  implicit val request = FakeRequest()

  val emptyDrafts = Seq("draft"    -> "")
  val nonEmptyDrafts = Seq("draft" -> "1234567?localhost:1234/delete-draft")

  "The manage drafts page" should "reject a continue check submission when no selection has been made" in {

    val draftCase = randomDraftCase
    when(mockDraftCases.get(anyLong)(any[HeaderCarrier])).thenReturn(Future.successful(Seq(draftCase)))

    val res = testController.continueCheck()(FakeRequest().withFormUrlEncodedBody(emptyDrafts: _*))
    status(res) mustBe BAD_REQUEST
  }

  it should "remove a draft case when a valid submission has been made" in {
    when(mockDraftCases.delete(any[String])(any[HeaderCarrier])).thenReturn(Future.successful("successful"))
    val validData = Seq(
      "draft" -> "1234567?localhost:1234/delete-draft"
    )

    val res = testController.deleteDraftCase()(FakeRequest().withFormUrlEncodedBody(validData: _*))
    status(res) mustBe SEE_OTHER
    redirectLocation(res) mustBe Some(viewDraftPage)

  }

  it should "continue a check case when a valid submission has been made" in {

    val validData = Seq(
      "draft" -> s"1234567?${routes.ManageDrafts.viewDraftCases().url}"
    )

    val res = testController.continueCheck()(FakeRequest().withFormUrlEncodedBody(validData: _*))
    status(res) mustBe SEE_OTHER
    redirectLocation(res).get.endsWith(viewDraftPage) mustBe true

  }

  it should "continue to a confirm delete case when a valid submission has been made" in {

    val validData = Seq(
      "draft" -> s"1234567?${routes.ManageDrafts.viewDraftCases().url}"
    )

    val res = testController.confirmDelete("abc124")(FakeRequest().withFormUrlEncodedBody(validData: _*))
    status(res) mustBe SEE_OTHER
    redirectLocation(res) mustBe Some(viewDraftPage)
  }

  private lazy val viewDraftPage = controllers.routes.ManageDrafts.viewDraftCases().url
  implicit lazy val mockDraftCases = mock[DraftCases]

  private lazy val testController = new ManageDrafts(
    mockCustomErrorHandler,
    preAuthenticatedActionBuilders(),
    StubPropertyLinkConnector
  )

}
