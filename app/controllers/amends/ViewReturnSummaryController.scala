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
import connectors.{CacheConnector, TaxReturnsConnector}
import controllers.actions._
import controllers.helpers.TaxReturnHelper
import models.requests.OptionalDataRequest
import models.returns.{ReturnDisplayApi, TaxReturnObligation}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import uk.gov.hmrc.http.HeaderCarrier
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
  taxReturnHelper: TaxReturnHelper,
  returnsConnector: TaxReturnsConnector
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  def onPageLoad(periodKey: String): Action[AnyContent] =
    (identify andThen getData).async {
      implicit request =>

        fetchData(periodKey).map { case (_, submittedReturn, obligation, ddInProgress) =>
          val returnPeriod = views.ViewUtils.displayReturnQuarter(obligation)
          val amendCall: Option[Call] = Some(controllers.amends.routes.ViewReturnSummaryController.amendReturn(periodKey)).filter(_ => !ddInProgress)
          Ok(view(returnPeriod, ViewReturnSummaryViewModel(submittedReturn), amendCall))
        }
    }
    
  def amendReturn(periodKey: String): Action[AnyContent] =
    (identify andThen getData).async {
      implicit request =>
        
      fetchData(periodKey).flatMap { case (pptReference, submittedReturn, obligation, ddInProgress) =>
        if (ddInProgress) throw new Exception("Can not amend a return that has a Direct Debit collection in progress") else {
          val future = if (!request.userAnswers.get(AmendSelectedPeriodKey).contains(periodKey)) 
              reinitialiseCache(periodKey, request, pptReference, submittedReturn, obligation)
            else
              Future.unit
          future.map(_ => Redirect(controllers.amends.routes.CheckYourAnswersController.onPageLoad()))
        }
      }
    }
    
  private def fetchData(periodKey: String) (implicit request: OptionalDataRequest[_]) = {
    val pptReference: String = request.pptReference
    if (!periodKey.matches("[A-Z0-9]{4}")) throw new Exception(s"Period key '$periodKey' is not allowed.")

    val futureReturn = taxReturnHelper.fetchTaxReturn(pptReference, periodKey)
    val futureObligation = taxReturnHelper.getObligation(pptReference, periodKey)
    val futureDDInProgress = returnsConnector.ddInProgress(pptReference, periodKey)
    for {
      submittedReturn <- futureReturn
      obligation <- futureObligation
      ddInProgress <- futureDDInProgress
    } yield {
      (pptReference, submittedReturn, obligation, ddInProgress.isDdCollectionInProgress)
    }
  }

  private def reinitialiseCache(periodKey: String, request: OptionalDataRequest[_], pptReference: String, 
    submittedReturn: ReturnDisplayApi, obligation: TaxReturnObligation) (implicit hc: HeaderCarrier) = {
    request.userAnswers
      .reset
      .setUnsafe(AmendSelectedPeriodKey, periodKey)
      .setUnsafe(ObligationCacheable, obligation)
      .setUnsafe(ReturnDisplayApiCacheable, submittedReturn)
      .save(cacheConnector.saveUserAnswerFunc(pptReference))
  }
}
