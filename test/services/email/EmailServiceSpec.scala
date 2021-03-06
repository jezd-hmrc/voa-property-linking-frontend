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

package services.email

import connectors.email.EmailConnector
import controllers.PayLoad
import models.{DetailedIndividualAccount, GroupAccount, IndividualDetails}
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status.OK
import play.api.libs.json.Writes
import uk.gov.hmrc.auth.core.AffinityGroup.Organisation
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class EmailServiceSpec extends UnitSpec with MockitoSugar {

  private implicit val headerCarrier = HeaderCarrier()
  val config = mock[ServicesConfig]

  "EmailService" should {

    "send new enrolment success email" in {
      val mockWSHttp = mock[HttpClient]
      val emailConnector = new EmailConnector(config, mockWSHttp)
      val emailService = new EmailService(emailConnector)

      val groupAccount =
        GroupAccount(123L, "groupId", "companyName", 221L, "email@email.com", "01234567889", true, Some(12345678L))

      when(config.baseUrl("email")).thenReturn("http://blah:2909/")

      when(
        mockWSHttp.POST[PayLoad, HttpResponse](anyString(), any[PayLoad](), any())(
          any[Writes[PayLoad]](),
          any[HttpReads[HttpResponse]](),
          any[HeaderCarrier](),
          any()))
        .thenReturn(Future.successful(HttpResponse(OK)))

      val individualDetails =
        IndividualDetails("firstName", "lastName", "email@email.com", "012345567788", None, 12345L)

      await(
        emailService.sendNewRegistrationSuccess(
          "toAddress@email.com",
          DetailedIndividualAccount("externalId", "trustId", 123L, 234L, individualDetails),
          Some(groupAccount),
          Some(Organisation)))

      verify(mockWSHttp)
        .POST(any, any, any)(any[Writes[PayLoad]](), any[HttpReads[HttpResponse]](), any[HeaderCarrier](), any())
    }
  }

}
