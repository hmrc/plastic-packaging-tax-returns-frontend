/*
 * Copyright 2021 HM Revenue & Customs
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
import play.api.mvc.Call
import uk.gov.hmrc.plasticpackagingtax.returns.base.unit.UnitViewSpec
import uk.gov.hmrc.plasticpackagingtax.returns.controllers.returns.{routes => returnRoutes}
import uk.gov.hmrc.plasticpackagingtax.returns.views.html.returns.check_your_return_page
import uk.gov.hmrc.plasticpackagingtax.returns.views.tags.ViewTest

@ViewTest
class CheckYourReturnViewSpec extends UnitViewSpec with Matchers {

  private val page = instanceOf[check_your_return_page]

  private val taxReturn = aTaxReturn(withId("01"),
                                     withManufacturedPlasticWeight(5, 5),
                                     withConvertedPackagingCredit(5),
                                     withDirectExportDetails(2, 2),
                                     withHumanMedicinesPlasticWeight(4),
                                     withImportedPlasticWeight(2, 2),
                                     withManufacturedPlasticWeight(7, 7)
  )

  private def createView(): Document =
    page(taxReturn)(request, messages)

  "Check Your Return View" should {

    "have proper messages for labels" in {
      messages must haveTranslationFor("returns.checkYourReturnPage.title")
      messages must haveTranslationFor("returns.checkYourReturnPage.label")

      messages must haveTranslationFor("returns.checkYourReturnPage.header.item")
      messages must haveTranslationFor("returns.checkYourReturnPage.header.amount")
      messages must haveTranslationFor("returns.checkYourReturnPage.header.changeLink")

      messages must haveTranslationFor("returns.checkYourReturnPage.manufacturedPackaging.total")
      messages must haveTranslationFor("returns.checkYourReturnPage.manufacturedPackaging.liable")

      messages must haveTranslationFor("returns.checkYourReturnPage.importedPackaging.total")
      messages must haveTranslationFor("returns.checkYourReturnPage.importedPackaging.liable")

      messages must haveTranslationFor("returns.checkYourReturnPage.humansMedicinesPackaging")

      messages must haveTranslationFor("returns.checkYourReturnPage.directExports")
      messages must haveTranslationFor("returns.checkYourReturnPage.exportsCredit")

      messages must haveTranslationFor("returns.checkYourReturnPage.conversionCredit")

      messages must haveTranslationFor("returns.checkYourReturnPage.sendReturn.paragraph")
      messages must haveTranslationFor("returns.checkYourReturnPage.sendReturn.description")
    }

    val view = createView()

    "display 'Back' button" in {

      view.getElementById("back-link") must haveHref(
        returnRoutes.ConvertedPackagingCreditController.displayPage()
      )
    }

    "display meta title" in {

      view.select("title").text() must include(messages("returns.checkYourReturnPage.title"))
    }

    "display title" in {

      view.getElementsByClass("govuk-label--l").first() must containMessage(
        "returns.checkYourReturnPage.title"
      )
    }

    "display labels, values and change links" when {
      def getTableRows =
        view.getElementsByClass("govuk-table__body").first()
          .getElementsByClass("govuk-table__row")

      def getRowHeaderFor(section: Int) =
        getTableRows.get(section).getElementsByClass("govuk-table__header").first()

      def getRowValueFor(section: Int) =
        getTableRows.get(section).getElementsByClass("govuk-table__cell").first().text()

      def getRowChangeLinkFor(section: Int) =
        getTableRows.get(section).getElementsByClass("govuk-table__cell").get(1).getElementsByClass(
          "govuk-link"
        ).first()

      def rowExists(rowNo: Int, label: String, value: String, href: Call): Unit = {
        getRowHeaderFor(rowNo) must containMessage(label)
        getRowValueFor(rowNo) mustBe value
        getRowChangeLinkFor(rowNo) must haveHref(href)
      }

      def tableHeaderForColumn(colNo: Int) =
        view.getElementsByClass("govuk-table__head").first().getElementsByClass(
          "govuk-table__header"
        ).get(colNo)

      "display has total 8 rows" in {
        getTableRows.size() mustBe 8
      }

      "display table headers" in {

        tableHeaderForColumn(0).text() must include(
          messages("returns.checkYourReturnPage.header.item")
        )
        tableHeaderForColumn(1).text() must include(
          messages("returns.checkYourReturnPage.header.amount")
        )
      }

      "contain hidden table header for change link" in {
        val changeLinkTableHeader = tableHeaderForColumn(2)
        changeLinkTableHeader.text() must include(
          messages("returns.checkYourReturnPage.header.changeLink")
        )
        changeLinkTableHeader.hasClass("govuk-visually-hidden")
      }

      def asKg(value: String)     = s"$value kg"
      def asPounds(value: String) = s"£$value"

      "displaying organisation details section" in {

        rowExists(0,
                  "returns.checkYourReturnPage.manufacturedPackaging.total",
                  asKg(taxReturn.manufacturedPlasticWeight.get.totalKg.toString),
                  returnRoutes.ManufacturedPlasticWeightController.displayPage()
        )
        rowExists(1,
                  "returns.checkYourReturnPage.manufacturedPackaging.liable",
                  asKg(taxReturn.manufacturedPlasticWeight.get.totalKgBelowThreshold.toString),
                  returnRoutes.ManufacturedPlasticWeightController.displayPage()
        )

        rowExists(2,
                  "returns.checkYourReturnPage.importedPackaging.total",
                  asKg(taxReturn.importedPlasticWeight.get.totalKg.toString),
                  returnRoutes.ImportedPlasticWeightController.displayPage()
        )
        rowExists(3,
                  "returns.checkYourReturnPage.importedPackaging.liable",
                  asKg(taxReturn.importedPlasticWeight.get.totalKgBelowThreshold.toString),
                  returnRoutes.ImportedPlasticWeightController.displayPage()
        )

        rowExists(4,
                  "returns.checkYourReturnPage.humansMedicinesPackaging",
                  asKg(taxReturn.humanMedicinesPlasticWeight.get.totalKg.toString),
                  returnRoutes.HumanMedicinesPlasticWeightController.displayPage()
        )

        rowExists(5,
                  "returns.checkYourReturnPage.directExports",
                  asKg(taxReturn.exportedPlasticWeight.get.totalKg.toString),
                  returnRoutes.ExportedPlasticWeightController.displayPage()
        )
        rowExists(6,
                  "returns.checkYourReturnPage.exportsCredit",
                  asPounds(taxReturn.exportedPlasticWeight.get.totalValueForCreditAsString),
                  returnRoutes.ExportedPlasticWeightController.displayPage()
        )

        rowExists(7,
                  "returns.checkYourReturnPage.conversionCredit",
                  asPounds(taxReturn.convertedPackagingCredit.get.totalValueForCreditAsString),
                  returnRoutes.ConvertedPackagingCreditController.displayPage()
        )
      }
    }

    "display send your application paragraph" in {

      view.getElementById("send-your-application").text() must include(
        messages("returns.checkYourReturnPage.sendReturn.paragraph")
      )
    }

    "display send your application declaration" in {

      view.getElementsByClass("govuk-body-s").text() must include(
        messages("returns.checkYourReturnPage.sendReturn.description")
      )
    }

    "display 'Save and Continue' button" in {

      view must containElementWithID("submit")
      view.getElementById("submit").text() mustBe "Save and Continue"
    }

    "display 'Save and come back later' button" in {

      view.getElementById("save_and_come_back_later").text() mustBe "Save and come back later"
    }
  }
}
