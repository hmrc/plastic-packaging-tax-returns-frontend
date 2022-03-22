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
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.plasticpackagingtax.returns.connectors.{ServiceError, TaxReturnsConnector}
import uk.gov.hmrc.plasticpackagingtax.returns.controllers.actions.{
  AuthAction,
  FormAction,
  SaveAndContinue
}
import uk.gov.hmrc.plasticpackagingtax.returns.controllers.home.{routes => homeRoutes}
import uk.gov.hmrc.plasticpackagingtax.returns.controllers.returns.{routes => returnRoutes}
import uk.gov.hmrc.plasticpackagingtax.returns.forms.{ImportedPlastic, ImportedPlasticWeight}
import uk.gov.hmrc.plasticpackagingtax.returns.forms.ImportedPlasticWeight.form
import uk.gov.hmrc.plasticpackagingtax.returns.forms.ImportedPlastic.{form => importedPlasticForm}
import uk.gov.hmrc.plasticpackagingtax.returns.models.domain.{
  Cacheable,
  TaxReturn,
  ImportedPlasticWeight => ImportedPlasticWeightDetails
}
import uk.gov.hmrc.plasticpackagingtax.returns.models.obligations.Obligation
import uk.gov.hmrc.plasticpackagingtax.returns.models.request.{JourneyAction, JourneyRequest}
import uk.gov.hmrc.plasticpackagingtax.returns.views.html.returns.{
  imported_plastic_page,
  imported_plastic_weight_page
}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ImportedPlasticController @Inject() (
  authenticate: AuthAction,
  journeyAction: JourneyAction,
  override val returnsConnector: TaxReturnsConnector,
  mcc: MessagesControllerComponents,
  importedComponentPage: imported_plastic_page,
  importedWeightPage: imported_plastic_weight_page
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with Cacheable with I18nSupport {

  def contribution(): Action[AnyContent] =
    (authenticate andThen journeyAction) { implicit request: JourneyRequest[AnyContent] =>
      val form = request.taxReturn.importedPlastic match {
        case Some(data) =>
          importedPlasticForm().fill(data)
        case _ => importedPlasticForm()
      }
      Ok(importedComponentPage(form, getObligation(request.taxReturn)))

    }

  def submitContribution(): Action[AnyContent] =
    (authenticate andThen journeyAction).async { implicit request: JourneyRequest[AnyContent] =>
      ImportedPlastic.form()
        .bindFromRequest()
        .fold(
          (formWithErrors: Form[Boolean]) =>
            Future.successful(
              BadRequest(importedComponentPage(formWithErrors, getObligation(request.taxReturn)))
            ),
          contribution =>
            updateTaxReturn(contribution).map {
              case Right(_) =>
                Redirect(returnRoutes.ImportedPlasticController.weight())
              case Left(error) => throw error
            }
        )
    }

  def weight(): Action[AnyContent] =
    (authenticate andThen journeyAction) { implicit request: JourneyRequest[AnyContent] =>
      request.taxReturn.importedPlasticWeight match {
        case Some(data) =>
          Ok(
            importedWeightPage(form().fill(ImportedPlasticWeight(totalKg = data.totalKg.toString)),
                               request.taxReturn.getTaxReturnObligation()
            )
          )
        case _ => Ok(importedWeightPage(form(), request.taxReturn.getTaxReturnObligation()))
      }
    }

  def submitWeight(): Action[AnyContent] =
    (authenticate andThen journeyAction).async { implicit request: JourneyRequest[AnyContent] =>
      ImportedPlasticWeight.form()
        .bindFromRequest()
        .fold(
          (formWithErrors: Form[ImportedPlasticWeight]) =>
            Future.successful(
              BadRequest(
                importedWeightPage(formWithErrors, request.taxReturn.getTaxReturnObligation())
              )
            ),
          weight =>
            updateTaxReturn(weight).map {
              case Right(_) =>
                FormAction.bindFromRequest match {
                  case SaveAndContinue =>
                    Redirect(returnRoutes.HumanMedicinesPlasticWeightController.displayPage())
                  case _ => Redirect(homeRoutes.HomeController.displayPage())
                }
              case Left(error) => throw error
            }
        )
    }

  private def updateTaxReturn(
    formData: ImportedPlasticWeight
  )(implicit req: JourneyRequest[_]): Future[Either[ServiceError, TaxReturn]] =
    update { taxReturn =>
      taxReturn.copy(importedPlasticWeight =
        Some(ImportedPlasticWeightDetails(totalKg = formData.totalKg.toLong))
      )
    }

  private def updateTaxReturn(
    formData: Boolean
  )(implicit req: JourneyRequest[_]): Future[Either[ServiceError, TaxReturn]] =
    update { taxReturn =>
      taxReturn.copy(importedPlastic = Some(formData))
    }

  private def getObligation(taxReturn: TaxReturn): Obligation =
    taxReturn.obligation.getOrElse(
      throw new IllegalStateException("Tax return obligation not present")
    )

}
