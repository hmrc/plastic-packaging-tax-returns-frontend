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
import uk.gov.hmrc.plasticpackagingtax.returns.builders.TaxReturnBuilder
import uk.gov.hmrc.plasticpackagingtax.returns.controllers.returns.routes
import uk.gov.hmrc.plasticpackagingtax.returns.forms.HumanMedicinesPlasticWeight
import uk.gov.hmrc.plasticpackagingtax.returns.forms.HumanMedicinesPlasticWeight.form
import uk.gov.hmrc.plasticpackagingtax.returns.views.html.returns.human_medicines_plastic_weight_page
import uk.gov.hmrc.plasticpackagingtax.returns.views.tags.ViewTest

@ViewTest
class HumanMedicinesPlasticWeightViewSpec extends UnitViewSpec with Matchers with TaxReturnBuilder {

  private val page = instanceOf[human_medicines_plastic_weight_page]

  private def createView(
    form: Form[HumanMedicinesPlasticWeight] = HumanMedicinesPlasticWeight.form()
  ): Document =
    page(form, defaultObligation)(request, messages)

  override def exerciseGeneratedRenderingMethods(): Unit = {
    page.f(form(), defaultObligation)(request, messages)
    page.render(form(), defaultObligation, request, messages)
  }

  "Human Medicines Plastic Weight View" should {

    val view = createView()

    "contain timeout dialog function" in {

      containTimeoutDialogFunction(view) mustBe true
    }

    "display sign out link" in {

      displaySignOutLink(view)
    }

    "display 'Back' button" in {

      view.getElementById("back-link") must haveHref(routes.ImportedPlasticController.weight())
    }

    "display title" in {

      view.select("title").text() must include(
        messages("returns.humanMedicinesPlasticWeight.meta.title")
      )
    }

    "display header" in {

      view.getElementById("section-header").text() must include(
        "April to June 2022"
      ) // from defaultObligation
    }

    "display hint" in {

      view.getElementsByClass("govuk-!-margin-bottom-7").text() must include(
        messages("returns.humanMedicinesPlasticWeight.hint")
      )
    }

    "display total weight label" in {

      view.getElementsByAttributeValueMatching("for", "totalKg").text() must include(
        messages("returns.humanMedicinesPlasticWeight.total.weight")
      )
    }

    "display total weight input box" in {

      view must containElementWithID("totalKg")
    }

    "display 'Save and Continue' button" in {

      view must containElementWithID("submit")
      view.getElementById("submit").text() mustBe messages("site.button.saveAndContinue")
    }

  }

  "Human Medicines Plastic Weight View when filled" should {

    "display data in total weight input" in {

      val form = HumanMedicinesPlasticWeight
        .form()
        .fill(HumanMedicinesPlasticWeight("1000"))
      val view = createView(form)

      view.getElementById("totalKg").attr("value") mustBe "1000"
    }
  }

  "display error" when {

    "weight is not entered" in {

      val form = HumanMedicinesPlasticWeight
        .form()
        .fillAndValidate(HumanMedicinesPlasticWeight(""))
      val view = createView(form)

      view must haveGovukGlobalErrorSummary

      view must haveGovukFieldError("totalKg", "Enter an amount to continue")
    }
  }
}
