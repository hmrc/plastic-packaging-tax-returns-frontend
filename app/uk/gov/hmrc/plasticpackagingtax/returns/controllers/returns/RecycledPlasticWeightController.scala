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
import uk.gov.hmrc.plasticpackagingtax.returns.forms.RecycledPlasticWeight
import uk.gov.hmrc.plasticpackagingtax.returns.forms.RecycledPlasticWeight.form
import uk.gov.hmrc.plasticpackagingtax.returns.models.domain.{
  Cacheable,
  TaxReturn,
  RecycledPlasticWeight => RecycledPlasticWeightDetails
}
import uk.gov.hmrc.plasticpackagingtax.returns.models.request.{JourneyAction, JourneyRequest}
import uk.gov.hmrc.plasticpackagingtax.returns.views.html.returns.recycled_plastic_weight_page
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RecycledPlasticWeightController @Inject() (
  authenticate: AuthAction,
  journeyAction: JourneyAction,
  override val returnsConnector: TaxReturnsConnector,
  mcc: MessagesControllerComponents,
  page: recycled_plastic_weight_page
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with Cacheable with I18nSupport {

  def displayPage(): Action[AnyContent] =
    (authenticate andThen journeyAction) { implicit request: JourneyRequest[AnyContent] =>
      val obligation = request.taxReturn.obligation.getOrElse(
        throw new IllegalStateException(s"No Obligation for return id:${request.enrolmentId}")
      )
      request.taxReturn.recycledPlasticWeight match {
        case Some(data) =>
          Ok(page(form().fill(RecycledPlasticWeight(totalKg = data.totalKg.toString)), obligation))
        case _ => Ok(page(form(), obligation))
      }
    }

  def submit(): Action[AnyContent] =
    (authenticate andThen journeyAction).async { implicit request: JourneyRequest[AnyContent] =>
      val obligation = request.taxReturn.obligation.getOrElse(
        throw new IllegalStateException(s"No Obligation for return id:${request.enrolmentId}")
      )
      RecycledPlasticWeight.form()
        .bindFromRequest()
        .fold(
          (formWithErrors: Form[RecycledPlasticWeight]) =>
            Future.successful(BadRequest(page(formWithErrors, obligation))),
          weight =>
            updateTaxReturn(weight).map {
              case Right(_) =>
                FormAction.bindFromRequest match {
                  case SaveAndContinue =>
                    Redirect(returnRoutes.ConvertedPackagingCreditController.displayPage())
                  case _ => Redirect(homeRoutes.HomeController.displayPage())
                }
              case Left(error) => throw error
            }
        )
    }

  private def updateTaxReturn(
    formData: RecycledPlasticWeight
  )(implicit req: JourneyRequest[_]): Future[Either[ServiceError, TaxReturn]] =
    update { taxReturn =>
      taxReturn.copy(recycledPlasticWeight =
        Some(RecycledPlasticWeightDetails(totalKg = formData.totalKg.toLong))
      )
    }

}
