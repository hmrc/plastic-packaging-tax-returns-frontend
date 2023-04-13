package factories

import models.UserAnswers
import pages.returns.credits.ExportedCreditsWeightPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkAnswers.returns.credits.{CreditsExportedPlasticSummary, CreditsExportedWeightSummary, CreditsTaxRateSummary}

class CreditSummaryListFactory {

  def createSummaryList(taxRate: BigDecimal, userAnswer: UserAnswers)(implicit messages: Messages): Seq[SummaryListRow] = {
    CreditsTaxRateSummary(taxRate) +:
      Seq(
        CreditsExportedPlasticSummary,
        CreditsExportedWeightSummary
      ).flatMap(_.row(userAnswer))
  }

}
