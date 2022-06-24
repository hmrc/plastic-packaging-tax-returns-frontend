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

package controllers.amends

import cacheables.{AmendSelectedPeriodKey, ObligationCacheable, ReturnDisplayApiCacheable}
import connectors.CacheConnector
import controllers.actions._
import controllers.helpers.TaxReturnHelper
import models.UserAnswers
import models.returns.{ReturnDisplayApi, TaxReturnObligation}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.ViewReturnSummaryViewModel
import views.html.amends.ViewReturnSummaryView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ViewReturnSummaryController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  cacheConnector: CacheConnector,
  getData: DataRetrievalAction,
  val controllerComponents: MessagesControllerComponents,
  view: ViewReturnSummaryView,
  taxReturnHelper: TaxReturnHelper
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  def onPageLoad(periodKey: String): Action[AnyContent] =
    (identify andThen getData).async {
      implicit request =>

        val pptId: String = request.pptReference

        if (!periodKey.matches("[A-Z0-9]{4}")) throw new Exception(s"Period key '$periodKey' is not allowed.")

        val submittedReturnF: Future[ReturnDisplayApi]             = taxReturnHelper.fetchTaxReturn(pptId, periodKey)
        val fulfilledObligationF: Future[Seq[TaxReturnObligation]] = taxReturnHelper.getObligation(pptId, periodKey)

        for {
          submittedReturn <- submittedReturnF
          obligation <- fulfilledObligationF
          updatedAnswers <- Future.fromTry(
            request.userAnswers.set(AmendSelectedPeriodKey, periodKey).get
              .set(ObligationCacheable, obligation.head).get
              .set(ReturnDisplayApiCacheable, submittedReturn))
          _ <- cacheConnector.set(pptId, updatedAnswers)
        } yield {
          val returnPeriod = views.ViewUtils.displayReturnQuarter(obligation.head)
          Ok(view(returnPeriod, ViewReturnSummaryViewModel(submittedReturn)))
        }
    }

}
