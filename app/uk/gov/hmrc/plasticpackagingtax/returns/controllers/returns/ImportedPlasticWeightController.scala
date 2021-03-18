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

package uk.gov.hmrc.plasticpackagingtax.returns.controllers.returns

import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.plasticpackagingtax.returns.connectors.{ServiceError, TaxReturnsConnector}
import uk.gov.hmrc.plasticpackagingtax.returns.controllers.actions.{
  AuthAction,
  FormAction,
  SaveAndContinue
}
import uk.gov.hmrc.plasticpackagingtax.returns.controllers.home.{routes => homeRoutes}
import uk.gov.hmrc.plasticpackagingtax.returns.forms.ImportedPlasticWeight
import uk.gov.hmrc.plasticpackagingtax.returns.models.domain.{Cacheable, TaxReturn}
import uk.gov.hmrc.plasticpackagingtax.returns.models.request.{JourneyAction, JourneyRequest}
import uk.gov.hmrc.plasticpackagingtax.returns.views.html.returns.imported_plastic_weight_page
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ImportedPlasticWeightController @Inject() (
  authenticate: AuthAction,
  journeyAction: JourneyAction,
  override val returnsConnector: TaxReturnsConnector,
  mcc: MessagesControllerComponents,
  page: imported_plastic_weight_page
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with Cacheable with I18nSupport {

  def displayPage(): Action[AnyContent] =
    (authenticate andThen journeyAction) { implicit request: JourneyRequest[AnyContent] =>
      request.taxReturn.importedPlasticWeight match {
        case data: Any =>
          Ok(
            page(
              ImportedPlasticWeight.form().fill(
                ImportedPlasticWeight(totalKg = data.totalKg.map(_.toString),
                                      totalKgBelowThreshold =
                                        data.totalKgBelowThreshold.map(_.toString)
                )
              )
            )
          )
        case _ => Ok(page(ImportedPlasticWeight.form()))
      }
    }

  def submit(): Action[AnyContent] =
    (authenticate andThen journeyAction).async { implicit request: JourneyRequest[AnyContent] =>
      ImportedPlasticWeight.form()
        .bindFromRequest()
        .fold(
          (formWithErrors: Form[ImportedPlasticWeight]) =>
            Future.successful(BadRequest(page(formWithErrors))),
          weight =>
            updateTaxReturn(weight).map {
              case Right(_) =>
                FormAction.bindFromRequest match {
                  case SaveAndContinue => Redirect(homeRoutes.HomeController.displayPage())
                  case _               => Redirect(homeRoutes.HomeController.displayPage())
                }
              case Left(error) => throw error
            }
        )
    }

  private def updateTaxReturn(
    formData: ImportedPlasticWeight
  )(implicit req: JourneyRequest[_]): Future[Either[ServiceError, TaxReturn]] =
    update { taxReturn =>
      val updatedImportedPlasticWeight =
        taxReturn.importedPlasticWeight.copy(totalKg = formData.totalKg.map(_.trim.toLong),
                                             totalKgBelowThreshold =
                                               formData.totalKgBelowThreshold.map(_.trim.toLong)
        )
      taxReturn.copy(importedPlasticWeight = updatedImportedPlasticWeight)
    }

}
