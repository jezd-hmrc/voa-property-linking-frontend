/*
 * Copyright 2016 HM Revenue & Customs
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

package forms

import org.scalatest.{FlatSpec, MustMatchers}
import controllers.CreateGroupAccount._
import utils.FormBindingVerification._
import views.helpers.Errors

class CreateGroupAccountFormSpec extends FlatSpec with MustMatchers {

  val validData = Map(
    keys.companyName -> "Company Ltd",
    keys.email -> "email@address.com",
    keys.confirmEmail -> "email@address.com",
    "address.line1" -> "Address line 1",
    "address.postcode" -> "AA11 1AA",
    keys.phone -> "01234567890",
    keys.isAgent -> "false",
    keys.isSmallBusiness -> "true"
  )

  "The create group account form" must "require a company name" in {
    verifyMandatory(form, validData, keys.companyName)
  }

  it must "require the company name to be no more than 45 characters" in {
    verifyCharacterLimit(form, validData, keys.companyName, 45)
  }

  it must "require a business email address" in {
    verifyMandatory(form, validData, keys.email)
  }

  it must "require the email address to be no more than 150 characters" in {
    verifyError(form, validData.updated(keys.email, (1 to 140 map { _ => "a" } mkString) + "@example.com"), keys.email, "error.maxLength")
  }

  it must "require the email address to be valid" in {
    Seq("aa.aa", "aaaa", "aa@@aa", "") foreach { invalid =>
      verifyError(form, validData.updated(keys.email, invalid), keys.email, "error.email")
    }
  }

  it must "require the confirmed email to match" in {
    verifyError(form, validData.updated(keys.email, "an.email@address.com").updated(keys.confirmEmail, "another.email@address.com"), keys.confirmEmail, Errors.emailsMustMatch)
  }

  it must "require a phone number" in {
    verifyMandatory(form, validData, keys.phone)
  }

  it must "require the phone number to be no more than 20 characters" in {
    verifyCharacterLimit(form, validData, keys.phone, 20)
  }

  it must "require the user to specify if they are an agent or not" in {
    verifyMandatory(form, validData, keys.isAgent)
  }

  it must "require the user to specify if their business is a small business" in {
    verifyMandatory(form, validData, keys.isSmallBusiness)
  }
}
