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

import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, Flash, MessagesControllerComponents}
import uk.gov.hmrc.plasticpackagingtax.returns.connectors.{ServiceError, TaxReturnsConnector}
import uk.gov.hmrc.plasticpackagingtax.returns.controllers.actions.{
  AuthAction,
  FormAction,
  SaveAndContinue
}
import uk.gov.hmrc.plasticpackagingtax.returns.controllers.home.{routes => homeRoutes}
import uk.gov.hmrc.plasticpackagingtax.returns.models.domain.{Cacheable, MetaData, TaxReturn}
import uk.gov.hmrc.plasticpackagingtax.returns.models.request.{JourneyAction, JourneyRequest}
import uk.gov.hmrc.plasticpackagingtax.returns.models.response.FlashKeys
import uk.gov.hmrc.plasticpackagingtax.returns.views.html.returns.check_your_return_page
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

// TODO: the logic below requires refactoring once we include call to submit tax return.
@Singleton
class CheckYourReturnController @Inject() (
  authenticate: AuthAction,
  journeyAction: JourneyAction,
  override val returnsConnector: TaxReturnsConnector,
  mcc: MessagesControllerComponents,
  page: check_your_return_page
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with Cacheable with I18nSupport {

  def displayPage(): Action[AnyContent] =
    (authenticate andThen journeyAction) { implicit request: JourneyRequest[AnyContent] =>
      if (request.taxReturn.isReturnSubmitReady)
        Ok(page(request.taxReturn))
      else
        Redirect(homeRoutes.HomeController.displayPage())
    }

  def submit(): Action[AnyContent] =
    (authenticate andThen journeyAction).async { implicit request: JourneyRequest[AnyContent] =>
      FormAction.bindFromRequest match {
        case SaveAndContinue => Future.successful(Redirect(homeRoutes.HomeController.displayPage()))
        case _ =>
          val refId = s"PPTR12345678${Random.nextInt(1000000)}"
          markReturnCompleted().map {
            case Right(_) =>
              Redirect(homeRoutes.HomeController.displayPage()).flashing(
                Flash(Map(FlashKeys.referenceId -> refId))
              )
            case Left(error) => throw error
          }
      }
    }

  private def markReturnCompleted()(implicit
    req: JourneyRequest[_]
  ): Future[Either[ServiceError, TaxReturn]] =
    update { taxReturn =>
      taxReturn.copy(metaData = MetaData(returnCompleted = true))
    }

}
