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

import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
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
      messages must haveTranslationFor("returns.checkYourReturnPage.header.empty")
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

      messages must haveTranslationFor("returns.checkYourReturnPage.taxLiability.label")
      messages must haveTranslationFor(
        "returns.checkYourReturnPage.taxLiability.exemptPackaging.label"
      )
      messages must haveTranslationFor(
        "returns.checkYourReturnPage.taxLiability.liablePackaging.label"
      )
      messages must haveTranslationFor("returns.checkYourReturnPage.taxLiability.description")

      messages must haveTranslationFor("returns.checkYourReturnPage.totalCredits.description")
      messages must haveTranslationFor("returns.checkYourReturnPage.totalCredits.label")
      messages must haveTranslationFor(
        "returns.checkYourReturnPage.totalCredits.creditsRequested.label"
      )
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

      val checkYourReturnView = new CheckYourReturnView(view)

      "tax return table has total 8 rows" in {
        checkYourReturnView.getTableRows(0).size() mustBe 8
      }

      "tax return table table headers" in {

        checkYourReturnView.tableHeaderForColumn(0, 0).text() must include(
          messages("returns.checkYourReturnPage.header.item")
        )
        checkYourReturnView.tableHeaderForColumn(0, 1).text() must include(
          messages("returns.checkYourReturnPage.header.amount")
        )
      }

      "tax return table contain hidden table header for change link" in {
        val changeLinkTableHeader = checkYourReturnView.tableHeaderForColumn(0, 2)
        changeLinkTableHeader.text() must include(
          messages("returns.checkYourReturnPage.header.changeLink")
        )
        changeLinkTableHeader.hasClass("govuk-visually-hidden")
      }

      def asKg(value: String)     = s"$value kg"
      def asPounds(value: String) = s"£$value"

      "tax return table displaying organisation details section" in {

        checkYourReturnView.rowExists(0,
                                      0,
                                      "returns.checkYourReturnPage.manufacturedPackaging.total",
                                      asKg(
                                        taxReturn.manufacturedPlasticWeight.get.totalKg.toString
                                      ),
                                      returnRoutes.ManufacturedPlasticWeightController.displayPage()
        )
        checkYourReturnView.rowExists(0,
                                      1,
                                      "returns.checkYourReturnPage.manufacturedPackaging.liable",
                                      asKg(
                                        taxReturn.manufacturedPlasticWeight.get.totalKgBelowThreshold.toString
                                      ),
                                      returnRoutes.ManufacturedPlasticWeightController.displayPage()
        )
        checkYourReturnView.rowExists(0,
                                      2,
                                      "returns.checkYourReturnPage.importedPackaging.total",
                                      asKg(taxReturn.importedPlasticWeight.get.totalKg.toString),
                                      returnRoutes.ImportedPlasticWeightController.displayPage()
        )
        checkYourReturnView.rowExists(0,
                                      3,
                                      "returns.checkYourReturnPage.importedPackaging.liable",
                                      asKg(
                                        taxReturn.importedPlasticWeight.get.totalKgBelowThreshold.toString
                                      ),
                                      returnRoutes.ImportedPlasticWeightController.displayPage()
        )

        checkYourReturnView.rowExists(0,
                                      4,
                                      "returns.checkYourReturnPage.humansMedicinesPackaging",
                                      asKg(
                                        taxReturn.humanMedicinesPlasticWeight.get.totalKg.toString
                                      ),
                                      returnRoutes.HumanMedicinesPlasticWeightController.displayPage()
        )

        checkYourReturnView.rowExists(0,
                                      5,
                                      "returns.checkYourReturnPage.directExports",
                                      asKg(taxReturn.exportedPlasticWeight.get.totalKg.toString),
                                      returnRoutes.ExportedPlasticWeightController.displayPage()
        )
        checkYourReturnView.rowExists(0,
                                      6,
                                      "returns.checkYourReturnPage.exportsCredit",
                                      asPounds(
                                        taxReturn.exportedPlasticWeight.get.totalValueForCreditAsString
                                      ),
                                      returnRoutes.ExportedPlasticWeightController.displayPage()
        )

        checkYourReturnView.rowExists(0,
                                      7,
                                      "returns.checkYourReturnPage.conversionCredit",
                                      asPounds(
                                        taxReturn.convertedPackagingCredit.get.totalValueForCreditAsString
                                      ),
                                      returnRoutes.ConvertedPackagingCreditController.displayPage()
        )
      }

      "tax liability table has total 4 rows" in {
        checkYourReturnView.getTableRows(1).size() mustBe 3
      }

      "tax liability table title" in {

        checkYourReturnView.getTableTitleFor(1) must include(
          messages("returns.checkYourReturnPage.taxLiability.label")
        )
      }

      "tax liability table displaying tax liability section" in {

        checkYourReturnView.rowHasValue(
          1,
          0,
          "returns.checkYourReturnPage.taxLiability.exemptPackaging.label",
          asKg(taxReturn.taxLiability.totalKgExempt.toString)
        )
        checkYourReturnView.rowHasValue(
          1,
          1,
          "returns.checkYourReturnPage.taxLiability.liablePackaging.label",
          asKg(taxReturn.taxLiability.totalKgLiable.toString)
        )
        checkYourReturnView.rowHasValue(1,
                                        2,
                                        "returns.checkYourReturnPage.taxLiability.label",
                                        asPounds(taxReturn.taxLiability.taxDue.toString)
        )
      }

      "tax credits table has total 1 row" in {
        checkYourReturnView.getTableRows(2).size() mustBe 1
      }

      "tax credits table title" in {

        checkYourReturnView.getTableTitleFor(2) must include(
          messages("returns.checkYourReturnPage.totalCredits.label")
        )
      }

      "tax credits table displaying tax credit section" in {

        checkYourReturnView.rowHasValue(
          2,
          0,
          "returns.checkYourReturnPage.totalCredits.creditsRequested.label",
          asPounds(taxReturn.taxLiability.totalCredit.toString)
        )
      }
    }

    "display tax liability paragraph" in {

      view.getElementsByClass("govuk-body-m").first().text() must include(
        messages("returns.checkYourReturnPage.taxLiability.description")
      )
    }

    "display total credits paragraph" in {

      view.getElementsByClass("govuk-body-m").get(1).text() must include(
        messages("returns.checkYourReturnPage.totalCredits.description")
      )
    }

    "display send your application paragraph" in {

      view.getElementById("send-your-application").text() must include(
        messages("returns.checkYourReturnPage.sendReturn.paragraph")
      )
    }

    "display send your application declaration" in {

      view.getElementsByClass("govuk-body-m").last().text() must include(
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

class CheckYourReturnView(view: Document) extends UnitViewSpec with Matchers {

  def getTable(tableNo: Int): Element =
    view.getElementsByClass("govuk-table").get(tableNo)

  def getTableRows(tableNo: Int): Elements =
    getTable(tableNo).getElementsByClass("govuk-table__body").first()
      .getElementsByClass("govuk-table__row")

  def getRowHeaderFor(tableNo: Int, rowNo: Int): Element =
    getTableRows(tableNo).get(rowNo).getElementsByClass("govuk-table__header").first()

  def getTableTitleFor(tableNo: Int): String =
    getTable(tableNo).getElementsByClass("govuk-table__caption").first().text()

  def getRowValueFor(tableNo: Int, rowNo: Int): String =
    getTableRows(tableNo).get(rowNo).getElementsByClass("govuk-table__cell").first().text()

  def getRowChangeLinkFor(tableNo: Int, rowNo: Int): Element =
    getTableRows(tableNo).get(rowNo).getElementsByClass("govuk-table__cell").get(
      1
    ).getElementsByClass("govuk-link").first()

  def rowExists(tableNo: Int, rowNo: Int, label: String, value: String, href: Call): Unit = {
    getRowHeaderFor(tableNo, rowNo) must containMessage(label)
    getRowValueFor(tableNo, rowNo) mustBe value
    getRowChangeLinkFor(tableNo, rowNo) must haveHref(href)
  }

  def rowHasValue(tableNo: Int, rowNo: Int, label: String, value: String): Unit = {
    getRowHeaderFor(tableNo, rowNo) must containMessage(label)
    getRowValueFor(tableNo, rowNo) mustBe value
  }

  def tableHeaderForColumn(tableNo: Int, colNo: Int) =
    view.getElementsByClass("govuk-table__head").get(tableNo).getElementsByClass(
      "govuk-table__header"
    ).get(colNo)

}
