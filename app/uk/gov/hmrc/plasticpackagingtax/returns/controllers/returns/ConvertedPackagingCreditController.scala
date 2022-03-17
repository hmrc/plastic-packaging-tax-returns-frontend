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

package uk.gov.hmrc.plasticpackagingtax.returns.controllers.returns

import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.plasticpackagingtax.returns.connectors.{
  ExportCreditsConnector,
  ServiceError,
  TaxReturnsConnector
}
import uk.gov.hmrc.plasticpackagingtax.returns.controllers.actions.{
  AuthAction,
  FormAction,
  SaveAndContinue
}
import uk.gov.hmrc.plasticpackagingtax.returns.controllers.home.{routes => homeRoutes}
import uk.gov.hmrc.plasticpackagingtax.returns.controllers.returns.{routes => returnRoutes}
import uk.gov.hmrc.plasticpackagingtax.returns.forms.{
  ConvertedPackagingCredit => ConvertedPackagingCreditDetails
}
import uk.gov.hmrc.plasticpackagingtax.returns.models.domain.{ConvertedPackagingCredit, TaxReturn}
import uk.gov.hmrc.plasticpackagingtax.returns.models.request.{JourneyAction, JourneyRequest}
import uk.gov.hmrc.plasticpackagingtax.returns.views.html.returns.converted_packaging_credit_page

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.math.BigDecimal.RoundingMode

@Singleton
class ConvertedPackagingCreditController @Inject() (
  authenticate: AuthAction,
  journeyAction: JourneyAction,
  override val returnsConnector: TaxReturnsConnector,
  exportCreditsConnector: ExportCreditsConnector,
  mcc: MessagesControllerComponents,
  page: converted_packaging_credit_page
)(implicit ec: ExecutionContext)
    extends ReturnsController(mcc) {

  def displayPage(): Action[AnyContent] =
    (authenticate andThen journeyAction).async { implicit request: JourneyRequest[AnyContent] =>
      exportCreditBalanceAvailable().map { balanceAvailable =>
        request.taxReturn.convertedPackagingCredit match {
          case Some(convertedPackagingCredit) =>
            Ok(
              page(
                ConvertedPackagingCreditDetails.form().fill(
                  ConvertedPackagingCreditDetails(totalInPounds =
                    convertedPackagingCredit.totalInPounds.setScale(2,
                                                                    RoundingMode.HALF_EVEN
                    ).toString
                  )
                ),
                balanceAvailable,
                getTaxReturnObligation(request.taxReturn)
              )
            )
          case _ =>
            Ok(
              page(ConvertedPackagingCreditDetails.form(),
                   balanceAvailable,
                   getTaxReturnObligation(request.taxReturn)
              )
            )
        }
      }
    }

  def submit(): Action[AnyContent] =
    (authenticate andThen journeyAction).async { implicit request: JourneyRequest[AnyContent] =>
      ConvertedPackagingCreditDetails.form()
        .bindFromRequest()
        .fold(
          (formWithErrors: Form[ConvertedPackagingCreditDetails]) =>
            exportCreditBalanceAvailable().map { balanceAvailable =>
              BadRequest(
                page(formWithErrors, balanceAvailable, getTaxReturnObligation(request.taxReturn))
              )
            },
          credit =>
            updateTaxReturn(credit).map {
              case Right(_) =>
                FormAction.bindFromRequest match {
                  case SaveAndContinue =>
                    Redirect(returnRoutes.CheckYourReturnController.displayPage())
                  case _ => Redirect(homeRoutes.HomeController.displayPage())
                }
              case Left(error) => throw error
            }
        )
    }

  private def exportCreditBalanceAvailable()(implicit
    request: JourneyRequest[_]
  ): Future[Option[BigDecimal]] = {

    val obligation = getTaxReturnObligation(request.taxReturn)
    exportCreditsConnector.get(request.pptReference,
                               obligation.fromDate.minusYears(2),
                               obligation.fromDate.minusDays(1)
    )
  }.map {
    case Right(balance) => Some(balance.totalExportCreditAvailable)
    case Left(_)        => None
  }

  private def updateTaxReturn(
    formData: ConvertedPackagingCreditDetails
  )(implicit req: JourneyRequest[_]): Future[Either[ServiceError, TaxReturn]] =
    update { taxReturn =>
      taxReturn.copy(convertedPackagingCredit =
        Some(ConvertedPackagingCredit(totalInPounds = formData.totalInPounds))
      )
    }

}
