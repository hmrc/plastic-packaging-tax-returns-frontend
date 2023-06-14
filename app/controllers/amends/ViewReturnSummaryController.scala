/*
 * Copyright 2023 HM Revenue & Customs
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

import cacheables.{AmendObligationCacheable, AmendSelectedPeriodKey, ReturnDisplayApiCacheable}
import connectors.{CacheConnector, TaxReturnsConnector}
import controllers.actions._
import controllers.amends.ViewReturnSummaryController.Unamendable
import models.requests.DataRequest.headerCarrier
import controllers.helpers.TaxReturnHelper
import handlers.ErrorHandler
import models.UserAnswers
import models.requests.DataRequest
import models.returns.{DDInProgressApi, ReturnDisplayApi, SubmittedReturn, TaxReturnObligation}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Results.{NotFound, Ok, Redirect}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import viewmodels.PrintTaxRate
import viewmodels.checkAnswers.ViewReturnSummaryViewModel
import views.html.amends.ViewReturnSummaryView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ViewReturnSummaryController @Inject() (
  override val messagesApi: MessagesApi,
  journeyAction: JourneyAction,
  cacheConnector: CacheConnector,
  val controllerComponents: MessagesControllerComponents,
  view: ViewReturnSummaryView,
  taxReturnHelper: TaxReturnHelper,
  returnsConnector: TaxReturnsConnector,
  errorHandler: ErrorHandler

)(implicit ec: ExecutionContext) extends I18nSupport {

  def onPageLoad(periodKey: String): Action[AnyContent] =
    journeyAction.async {
      implicit request =>

        fetchData(periodKey).map {
          case Right((_, submittedReturn, obligation, ddInProgress)) =>
            val returnPeriod = views.ViewUtils.displayReturnQuarter(obligation)
            val amendCall = Either
              .cond(!obligation.tooOldToAmend, controllers.amends.routes.ViewReturnSummaryController.amendReturn(periodKey), Unamendable.TooOld)
              .filterOrElse(_ => !ddInProgress, Unamendable.DDInProgress)
            Ok(view(returnPeriod, ViewReturnSummaryViewModel(submittedReturn.displayReturnJson), amendCall, submittedReturn.taxRate.asPoundPerTonne))
          case Left(result) => result
        }
    }
    
  def amendReturn(periodKey: String): Action[AnyContent] =
    journeyAction.async {
      implicit request =>
        
      fetchData(periodKey).flatMap {
        case Right((pptReference, submittedReturn, obligation, ddInProgress)) =>
          handleAPIsSuccessResponse(periodKey, pptReference, submittedReturn, obligation, ddInProgress)
        case Left(result) => Future.successful(result)
      }
    }

  private def handleAPIsSuccessResponse(
    periodKey: String,
    pptReference: String,
    submittedReturn: SubmittedReturn,
    obligation: TaxReturnObligation,
    ddInProgress: Boolean
  )(implicit request: DataRequest[_]): Future[Result] = {
    if (ddInProgress)
      throw new Exception("Can not amend a return that has a Direct Debit collection in progress")
    else
      handleNewPeriodKey(periodKey, pptReference, submittedReturn, obligation)
        .map(_ => Redirect(controllers.amends.routes.CheckYourAnswersController.onPageLoad()))
  }

  private def handleNewPeriodKey
  (
    periodKey: String,
    pptReference: String,
    submittedReturn: SubmittedReturn,
    obligation: TaxReturnObligation
  )(implicit request: DataRequest[_]): Future[Any] = {

    if(!request.userAnswers.get(AmendSelectedPeriodKey).contains(periodKey))
      reinitialiseCache(periodKey, pptReference, submittedReturn.displayReturnJson, obligation)
    else Future.unit
  }

  private def fetchData(periodKey: String)(implicit request: DataRequest[_])
  : Future[Either[Result, (String, SubmittedReturn, TaxReturnObligation, Boolean)]] = {
    val pptReference: String = request.pptReference
    if (!periodKey.matches("""\d{2}C[1-4]""")) Future.successful(Left(NotFound(errorHandler.notFoundTemplate(request.request))))
    else {

      val futureReturn: Future[SubmittedReturn] = taxReturnHelper.fetchTaxReturn(pptReference, periodKey)
      val futureMaybeObligation: Future[Option[TaxReturnObligation]] = taxReturnHelper.getObligation(pptReference, periodKey)
      val futureDDInProgress: Future[DDInProgressApi] = returnsConnector.ddInProgress(pptReference, periodKey)
      for {
        submittedReturn <- futureReturn
        maybeObligation <- futureMaybeObligation
        ddInProgress <- futureDDInProgress
      } yield {
        maybeObligation match {
          case Some(obligation) => Right((pptReference, submittedReturn, obligation, ddInProgress.isDdCollectionInProgress))
          case None => Left(NotFound(errorHandler.notFoundTemplate))
        }

      }
    }
  }

  private def reinitialiseCache(
    periodKey: String,
    pptReference: String,
    submittedReturn: ReturnDisplayApi,
    obligation: TaxReturnObligation
  )(implicit request: DataRequest[_]): Future[UserAnswers] = {
    request.userAnswers
      .reset
      .setOrFail(AmendSelectedPeriodKey, periodKey)
      .setOrFail(AmendObligationCacheable, obligation)
      .setOrFail(ReturnDisplayApiCacheable, submittedReturn)
      .save(cacheConnector.saveUserAnswerFunc(pptReference))
  }

}
 object ViewReturnSummaryController {

   sealed trait Unamendable
   object Unamendable {
     case object TooOld extends Unamendable
     case object DDInProgress extends Unamendable
   }
 }