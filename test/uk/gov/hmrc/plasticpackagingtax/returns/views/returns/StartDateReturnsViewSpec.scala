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

package uk.gov.hmrc.plasticpackagingtax.returns.views.returns

import org.jsoup.nodes.Document
import org.scalatest.matchers.must.Matchers
import play.api.data.Form
import uk.gov.hmrc.plasticpackagingtax.returns.base.unit.UnitViewSpec
import uk.gov.hmrc.plasticpackagingtax.returns.controllers.home.routes
import uk.gov.hmrc.plasticpackagingtax.returns.forms.returns.StartDateReturnForm
import uk.gov.hmrc.plasticpackagingtax.returns.forms.returns.StartDateReturnForm.{
  ErrorKey,
  FieldKey
}
import uk.gov.hmrc.plasticpackagingtax.returns.views.html.returns.start_date_returns_page

class StartDateReturnsViewSpec extends UnitViewSpec with Matchers {

  val page: start_date_returns_page = instanceOf[start_date_returns_page]
  val emptyForm: Form[Boolean]      = StartDateReturnForm.form()

  private def createView(form: Form[Boolean] = emptyForm): Document =
    page(form, defaultObligation)(request, messages)

  override def exerciseGeneratedRenderingMethods(): Unit = {
    page.f(emptyForm, defaultObligation)(request, messages)
    page.render(emptyForm, defaultObligation, request, messages)
  }

  "Start Date Returns View" should {

    lazy val view = createView()

    "contain timeout dialog function" in {

      containTimeoutDialogFunction(view) mustBe true
    }

    "display sign out link" in {

      displaySignOutLink(view)
    }

    "display 'Back' button" in {

      view.getElementById("back-link") must haveHref(routes.HomeController.displayPage())
    }

    val headingQuestion = messages("returns.startDateReturns.heading",
                                   messages(s"month.${defaultObligation.fromDate.getMonthValue}"),
                                   messages(s"month.${defaultObligation.toDate.getMonthValue}"),
                                   defaultObligation.toDate.getYear.toString
    )
    "display title" in {

      view.select("title").text() must include(headingQuestion)
    }

    "display heading" in {

      view.getElementsByTag("h1").text() must include(headingQuestion)
    }

    "display 'Save and Continue' button" in {

      view must containElementWithID("submit")
      view.getElementById("submit").text() mustBe messages("returns.startDateReturns.button")
    }

  }

  "display error" when {

    "weight is not entered" in {

      val erroredForm = emptyForm
        .bind(Map(FieldKey -> "jibberish"))
      val view = createView(erroredForm)

      view must haveGovukGlobalErrorSummary

      view must haveGovukFieldError(FieldKey, messages(ErrorKey))
    }
  }
}
