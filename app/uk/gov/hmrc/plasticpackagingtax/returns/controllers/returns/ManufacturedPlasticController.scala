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
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import uk.gov.hmrc.plasticpackagingtax.returns.config.AppConfig
import uk.gov.hmrc.plasticpackagingtax.returns.connectors.{ServiceError, TaxReturnsConnector}
import uk.gov.hmrc.plasticpackagingtax.returns.controllers.actions.AuthAction
import uk.gov.hmrc.plasticpackagingtax.returns.controllers.returns.{routes => returnRoutes}
import uk.gov.hmrc.plasticpackagingtax.returns.forms.{
  ManufacturedPlastic,
  ManufacturedPlasticWeight
}
import uk.gov.hmrc.plasticpackagingtax.returns.models.domain.{
  TaxReturn,
  ManufacturedPlasticWeight => ManufacturedPlasticWeightDetails
}
import uk.gov.hmrc.plasticpackagingtax.returns.models.request.{JourneyAction, JourneyRequest}
import uk.gov.hmrc.plasticpackagingtax.returns.views.html.returns.{
  manufactured_plastic_page,
  manufactured_plastic_weight_page
}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ManufacturedPlasticController @Inject() (
  authenticate: AuthAction,
  journeyAction: JourneyAction,
  override val returnsConnector: TaxReturnsConnector,
  mcc: MessagesControllerComponents,
  appConfig: AppConfig,
  manufacturedPlasticPage: manufactured_plastic_page,
  manufacturedPlasticWeightPage: manufactured_plastic_weight_page
)(implicit ec: ExecutionContext)
    extends ReturnsController(mcc) {

  private val liablePackagingGuidanceLink = Call("GET", appConfig.pptLiablePackagingGuidanceLink)

  private val excludedPackagingGuidanceLink =
    Call("GET", appConfig.pptExcludedPackagingGuidanceLink)

  def contribution(): Action[AnyContent] =
    (authenticate andThen journeyAction) { implicit request: JourneyRequest[AnyContent] =>
      val form = request.taxReturn.manufacturedPlastic match {
        case Some(manufacturedPlastic) => ManufacturedPlastic.form().fill(manufacturedPlastic)
        case _                         => ManufacturedPlastic.form()
      }
      Ok(
        manufacturedPlasticPage(form,
                                liablePackagingGuidanceLink,
                                request.taxReturn.getTaxReturnObligation()
        )
      )
    }

  def submitContribution(): Action[AnyContent] =
    (authenticate andThen journeyAction).async { implicit request: JourneyRequest[AnyContent] =>
      ManufacturedPlastic.form()
        .bindFromRequest()
        .fold(
          (formWithErrors: Form[Boolean]) =>
            Future.successful(
              BadRequest(
                manufacturedPlasticPage(formWithErrors,
                                        liablePackagingGuidanceLink,
                                        request.taxReturn.getTaxReturnObligation()
                )
              )
            ),
          manufacturedContribution =>
            updateTaxReturn(manufacturedContribution).map {
              case Right(_) =>
                if (manufacturedContribution)
                  Redirect(returnRoutes.ManufacturedPlasticController.weight())
                else
                  Redirect(returnRoutes.ImportedPlasticController.contribution())
              case Left(error) => throw error
            }
        )
    }

  def weight(): Action[AnyContent] =
    (authenticate andThen journeyAction) { implicit request: JourneyRequest[AnyContent] =>
      val form = request.taxReturn.manufacturedPlasticWeight match {
        case Some(data) =>
          ManufacturedPlasticWeight.form().fill(
            ManufacturedPlasticWeight(totalKg = data.totalKg.toString)
          )
        case _ => ManufacturedPlasticWeight.form()
      }
      Ok(
        manufacturedPlasticWeightPage(form,
                                      excludedPackagingGuidanceLink,
                                      request.taxReturn.getTaxReturnObligation()
        )
      )
    }

  def submitWeight(): Action[AnyContent] =
    (authenticate andThen journeyAction).async { implicit request: JourneyRequest[AnyContent] =>
      ManufacturedPlasticWeight.form()
        .bindFromRequest()
        .fold(
          (formWithErrors: Form[ManufacturedPlasticWeight]) =>
            Future.successful(
              BadRequest(
                manufacturedPlasticWeightPage(formWithErrors,
                                              excludedPackagingGuidanceLink,
                                              request.taxReturn.getTaxReturnObligation()
                )
              )
            ),
          weight =>
            updateTaxReturn(weight).map {
              case Right(_) =>
                Redirect(returnRoutes.ImportedPlasticController.contribution())
              case Left(error) => throw error
            }
        )
    }

  private def updateTaxReturn(
    manufacturedContribution: Boolean
  )(implicit req: JourneyRequest[_]): Future[Either[ServiceError, TaxReturn]] =
    update { taxReturn =>
      if (manufacturedContribution)
        taxReturn.copy(manufacturedPlastic = Some(true))
      else
        taxReturn.copy(manufacturedPlastic = Some(false),
                       manufacturedPlasticWeight =
                         Some(ManufacturedPlasticWeightDetails(totalKg = 0))
        )
    }

  private def updateTaxReturn(
    manufacturedPlasticWeight: ManufacturedPlasticWeight
  )(implicit req: JourneyRequest[_]): Future[Either[ServiceError, TaxReturn]] =
    update { taxReturn =>
      taxReturn.copy(manufacturedPlasticWeight =
        Some(ManufacturedPlasticWeightDetails(totalKg = manufacturedPlasticWeight.totalKg.toLong))
      )
    }

}
