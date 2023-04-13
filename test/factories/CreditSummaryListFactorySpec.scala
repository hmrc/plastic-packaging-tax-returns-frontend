package factories

import models.Mode.CheckMode
import models.UserAnswers
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchersSugar.any
import org.mockito.MockitoSugar.{mock, when}
import org.scalatestplus.play.PlaySpec
import play.api.i18n.Messages
import org.mockito.ArgumentMatchers.{eq => meq}
import uk.gov.hmrc.govukfrontend.views.Aliases.{SummaryListRow, Text, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{ActionItem, Actions, Key}
import pages.returns.credits.{ConvertedCreditsPage, ConvertedCreditsWeightPage, ExportedCreditsPage, ExportedCreditsWeightPage}

class CreditSummaryListFactorySpec extends PlaySpec {

  private val sut = new CreditSummaryListFactory();
  private val answer = mock[UserAnswers]
  private val messages = mock[Messages]

  "factory" should {
    "a summary list containing credit details" in {
      when(messages.apply(meq("confirmPackagingCredit.taxRate"))).thenReturn("Tax Rate")
      when(messages.apply(meq("confirmPackagingCredit.exported.answer"))).thenReturn("exported")
      when(messages.apply(meq("confirmPackagingCredit.exported.weight"))).thenReturn("exported weight")
      when(messages.apply(meq("confirmPackagingCredit.converted.answer"))).thenReturn("converted")
      when(messages.apply(meq("confirmPackagingCredit.converted.weight"))).thenReturn("converted weight")
      when(answer.get(meq(ExportedCreditsPage))(any)).thenReturn(Some(true))
      when(answer.get(meq(ExportedCreditsWeightPage))(any)).thenReturn(Some(10L))
      when(answer.get(meq(ConvertedCreditsPage))(any)).thenReturn(Some(true))
      when(answer.get(meq(ConvertedCreditsWeightPage))(any)).thenReturn(Some(20L))

      val res = sut.createSummaryList(0.30, answer)(messages)

      res mustBe Seq(
        createTaxRateRow,
        createExportedPlasticRow,
        createExportedPlasticWeightRow,
        createConvertedPlasticRow,
        createConvertedPlasticWeightRow
      )
    }
  }

  private def createTaxRateRow: SummaryListRow = {
    SummaryListRow(key = Key(Text("Tax Rate")), value = Value(Text("")))
  }

  private def createExportedPlasticRow =
    createSummaryRow(
      "exported",
      controllers.returns.credits.routes.ExportedCreditsController.onPageLoad(CheckMode).url)

  private def createExportedPlasticWeightRow =
    createSummaryRow(
      "exported weight",
      controllers.returns.credits.routes.ExportedCreditsWeightController.onPageLoad(CheckMode).url)

  private def createConvertedPlasticRow =
    createSummaryRow(
      "converted",
      controllers.returns.credits.routes.ConvertedCreditsController.onPageLoad(CheckMode).url)

  private def createConvertedPlasticWeightRow =
    createSummaryRow(
      "converted weight",
      controllers.returns.credits.routes.ConvertedCreditsWeightController.onPageLoad(CheckMode).url)

  private def createSummaryRow(key: String, href: String) = {
      SummaryListRow(
        key = Key(Text(key)),
        value = Value(Text("")),
        actions = Some(
          Actions(items =
            Seq(
              ActionItem(
                href = href,
                content = Text(""))
            )
          )
        )
      )
    }
}
