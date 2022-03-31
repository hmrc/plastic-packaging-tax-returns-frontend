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

package controllers

import com.google.inject.Inject
import connectors.{ServiceError, TaxReturnsConnector}
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import controllers.helpers.TaxReturnHelper
import models.returns.TaxReturn
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers._
import viewmodels.govuk.summarylist._
import views.html.CheckYourAnswersView

import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersController @Inject()
(
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  returnsConnector: TaxReturnsConnector,
  taxReturnHelper: TaxReturnHelper,
  val controllerComponents: MessagesControllerComponents,
  view: CheckYourAnswersView
) extends FrontendBaseController with I18nSupport {

  def onPageLoad(): Action[AnyContent] =
    (identify andThen getData andThen requireData) {
      implicit request =>
        val list = SummaryListViewModel(rows =
          Seq(AmendManufacturedPlasticPackagingSummary,
            AmendImportedPlasticPackagingSummary,
            AmendHumanMedicinePlasticPackagingSummary,
            AmendDirectExportPlasticPackagingSummary,
            AmendRecycledPlasticPackagingSummary
          ).flatMap(_.row(request.userAnswers))
        )

        Ok(view(list))
    }

  def onSubmit()(implicit ec: ExecutionContext): Action[AnyContent] =
    (identify andThen getData andThen requireData).async {
      implicit request =>
        val taxReturn = taxReturnHelper.getTaxReturn("pptref", request.userAnswers)

        submit(taxReturn).map {
          case Right(_) =>
            Redirect(routes.AmendConfirmationController.onPageLoad())

          case Left(error) =>
            throw error
        }


    }

  private def submit(
                      taxReturn: TaxReturn
                    )(implicit hc: HeaderCarrier): Future[Either[ServiceError, Unit]] =
    returnsConnector.submit(taxReturn)
}
