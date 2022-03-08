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

import com.kenshoo.play.metrics.Metrics
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, Flash, MessagesControllerComponents}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.plasticpackagingtax.returns.audit.Auditor
import uk.gov.hmrc.plasticpackagingtax.returns.connectors.{ServiceError, TaxReturnsConnector}
import uk.gov.hmrc.plasticpackagingtax.returns.controllers.actions.{
  AuthAction,
  FormAction,
  SaveAndContinue
}
import uk.gov.hmrc.plasticpackagingtax.returns.controllers.home.{routes => homeRoutes}
import uk.gov.hmrc.plasticpackagingtax.returns.controllers.returns.{routes => returnRoutes}
import uk.gov.hmrc.plasticpackagingtax.returns.models.domain.{Cacheable, MetaData, TaxReturn}
import uk.gov.hmrc.plasticpackagingtax.returns.models.request.{JourneyAction, JourneyRequest}
import uk.gov.hmrc.plasticpackagingtax.returns.models.response.FlashKeys
import uk.gov.hmrc.plasticpackagingtax.returns.views.html.returns.check_your_return_page
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

@Singleton
class CheckYourReturnController @Inject() (
  authenticate: AuthAction,
  journeyAction: JourneyAction,
  metrics: Metrics,
  override val returnsConnector: TaxReturnsConnector,
  auditor: Auditor,
  mcc: MessagesControllerComponents,
  page: check_your_return_page
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with Cacheable with I18nSupport {

  private val successSubmissionCounter =
    metrics.defaultRegistry.counter("ppt.returns.success.submission.counter")

  private val failedSubmissionCounter =
    metrics.defaultRegistry.counter("ppt.returns.failed.submission.counter")

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
        case SaveAndContinue =>
          val refId = s"PPTR12345678${Random.nextInt(1000000)}" // TODO Will be obtained from NRS
          submitReturnAndMarkAsCompleted().map {
            case Right(taxReturn) =>
              auditor.auditTaxReturn(taxReturn)
              successSubmissionCounter.inc()
              Redirect(returnRoutes.ConfirmationController.displayPage()).flashing(
                Flash(Map(FlashKeys.referenceId -> refId))
              )
            case Left(error) =>
              auditor.auditTaxReturn(request.taxReturn)
              failedSubmissionCounter.inc()
              throw error
          }
        case _ =>
          Future.successful(Redirect(homeRoutes.HomeController.displayPage()))
      }
    }

  private def submitReturnAndMarkAsCompleted()(implicit
    req: JourneyRequest[_]
  ): Future[Either[ServiceError, TaxReturn]] =
    submit(req.taxReturn).flatMap { _ =>
      update { taxReturn =>
        taxReturn.copy(metaData = MetaData(returnCompleted = true))
      }
    }

  private def submit(
    taxReturn: TaxReturn
  )(implicit hc: HeaderCarrier): Future[Either[ServiceError, Unit]] =
    returnsConnector.submit(taxReturn)

}
