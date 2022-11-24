/*
 * Copyright 2022 HM Revenue & Customs
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

package views.changeGroupLead

import base.ViewSpecBase
import forms.changeGroupLead.NewGroupLeadEnterContactAddressFormProvider
import forms.changeGroupLead.NewGroupLeadEnterContactAddressFormProvider._
import models.Mode.NormalMode
import org.scalatest.prop.TableDrivenPropertyChecks._
import play.api.data.Form
import play.twirl.api.Html
import support.{ViewAssertions, ViewMatchers}
import views.html.changeGroupLead.NewGroupLeadEnterContactAddressView

class NewGroupLeadEnterContactAddressViewSpec extends ViewSpecBase  with ViewAssertions with ViewMatchers {

  private val page = inject[NewGroupLeadEnterContactAddressView]
  private val formProvider = new NewGroupLeadEnterContactAddressFormProvider()
  private val organisationName = "Organisation Name"

  private def createView(form: Form[_] = formProvider.apply(), countryMap: Map[String,String] = Map()): Html = {
    page(form, countryMap, organisationName, NormalMode)(request, messages)
  }

  "view" should {
    val view = createView()

    "have a title" in {
      view.select("title").text() mustBe s"What is the organisation’s contact address? - Account - Plastic Packaging Tax - GOV.UK"
      view.select("title").text() must include(messages("newGroupLeadEnterContactAddress.title"))
    }

    "have header" in {
      view.select("h1").text() mustBe s"What is $organisationName’s contact address?"
      view.select("h1").text() mustBe messages("newGroupLeadEnterContactAddress.heading", organisationName)
    }

    "have paragraph text" in {
      view.getElementsByClass("govuk-body").text() must
        include("We’ll only use this to send information about the group’s Plastic Packaging Tax account and returns.")
      view.getElementsByClass("govuk-body").text() must include(messages("newGroupLeadEnterContactAddress.paragraph"))
    }

    "have input box" when {
      val table = Table(
        ("description", "id", "message", "key"),
        ("Address Line 1", "addressLine1", "Address line 1", "newGroupLeadEnterContactAddress.addressLine1.label"),
        ("Address Line 2", "addressLine2", "Address line 2 (optional)", "newGroupLeadEnterContactAddress.addressLine2.label"),
        ("Address Line 3", "addressLine3", "Address line 3 (optional)", "newGroupLeadEnterContactAddress.addressLine3.label"),
        ("Address Line 4", "addressLine4", "Town or city", "newGroupLeadEnterContactAddress.addressLine4.label"),
        ("Postal Code", "postalCode", "Postcode (required for UK addresses)", "newGroupLeadEnterContactAddress.postalCode.label"),
        ("Country Code", "countryCode", "Country", "newGroupLeadEnterContactAddress.countryCode.label")
      )

      forAll(table) {
        (description, id, message, key) =>
          s"for $description" in {
            view.getElementsByAttributeValue("for", id).text() mustBe message
            view.getElementsByAttributeValue("for", id).text() mustBe
              messages(key)
          }
      }
    }


    "bind form to inputViewModel" in {
      val mapData = createMap()
      val form = formProvider.apply().bind(mapData)

      val view = createView(form, Map("GB"-> "United Kingdom"))

      view.getElementById(addressLine1).`val`() mustBe mapData.get(addressLine1).get
      view.getElementById(addressLine2).`val`() mustBe mapData.get(addressLine2).get
      view.getElementById(addressLine3).`val`() mustBe mapData.get(addressLine3).get
      view.getElementById(addressLine4).`val`() mustBe mapData.get(addressLine4).get
      view.getElementById(postalCode).`val`() mustBe mapData.get(postalCode).get
      view.select("select#countryCode option[selected]").text() mustBe "United Kingdom"

    }

    "have save and continue button" in {
      view.getElementsByClass("govuk-button").text() mustBe "Save and continue"
      view.getElementsByClass("govuk-button").text() mustBe messages("site.continue")
    }

    "display an error summary box"  in {
      val form = formProvider.apply().withError("error key", "error message")

      val view = createView(form)

      view.getElementById("error-summary-title").text() mustBe "There is a problem"
      view.getElementById("error-summary-title").text() mustBe messages("error.summary.title")
      view.getElementsByClass("govuk-error-summary__list").get(0).text() mustBe "error message"

    }
  }

  private def createMap
  (
    addressLine1: String = "line1",
    addressLine2: String = "line2",
    addressLine3: String = "line3",
    addressLine4: String = "line4",
    postalCode: String = "NE5 4SF",
    countryCode: String = "GB"
  ): Map[String,String] = {
    Map(
      "addressLine1" -> addressLine1,
      "addressLine2" -> addressLine2,
      "addressLine3" -> addressLine3,
      "addressLine4" -> addressLine4,
      "postalCode" -> postalCode,
      "countryCode" -> countryCode
    )
  }
}
