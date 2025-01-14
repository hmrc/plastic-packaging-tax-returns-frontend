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

package controllers.returns

import cacheables.ReturnObligationCacheable
import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.{CacheConnector, CalculateCreditsConnector, TaxReturnsConnector}
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import controllers.helpers.TaxReturnHelper
import models.{ReturnsUserAnswers, UserAnswers}
import models.requests.DataRequest
import models.returns.Credits._
import models.returns._
import navigation.ReturnsJourneyNavigator
import pages.returns.credits.WhatDoYouWantToDoPage
import play.api.Logging
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import models.returns.ProcessingStatus.{AlreadySubmitted, Complete, Failed}
import models.returns.ProcessingEntry
import repositories.{ReturnsProcessingRepository, SessionRepository}
import repositories.SessionRepository.Paths
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.TaxReturnViewModel
import views.html.returns.ReturnsCheckYourAnswersView

import scala.concurrent.{ExecutionContext, Future}

class ReturnsCheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  returnsConnector: TaxReturnsConnector,
  creditsCalculatorConnector: CalculateCreditsConnector,
  taxReturnHelper: TaxReturnHelper,
  sessionRepository: SessionRepository,
  processingStatusRepository: ReturnsProcessingRepository,
  val controllerComponents: MessagesControllerComponents,
  appConfig: FrontendAppConfig,
  view: ReturnsCheckYourAnswersView,
  cacheConnector: CacheConnector,
  navigator: ReturnsJourneyNavigator
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      ReturnsUserAnswers.checkObligation(request) { obligation =>
        displayPage(request, obligation)
      }
    }

  def onSubmit(): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      val userAnswers: UserAnswers = request.userAnswers
      val isUserClaimingCredit     = userAnswers.get(WhatDoYouWantToDoPage).getOrElse(false)
      val processId                = request.cacheKey

      (for {
          _ <- processingStatusRepository.set(ProcessingEntry(processId))
          _ <- returnsConnector.submit(request.pptReference).flatMap {
            case Right(optChargeRef) =>
              for {
                _ <- sessionRepository.set(request.cacheKey, Paths.ReturnChargeRef, optChargeRef)
                _ <- sessionRepository.set(request.cacheKey, Paths.AlreadySubmitted, Some(true))
                _ <- processingStatusRepository.set(ProcessingEntry(processId, Complete))
              } yield ()
            case Left(_) =>
              val returnQuarter = userAnswers.getOrFail(ReturnObligationCacheable)
              for {
                _ <- sessionRepository.set(request.cacheKey, Paths.TaxReturnObligation, returnQuarter)
                _ <- processingStatusRepository.set(ProcessingEntry(processId, AlreadySubmitted))
              } yield ()
          }
        } yield () // keeps running in background
      ).recover { case ex: Exception =>
        processingStatusRepository.set(ProcessingEntry(processId, Failed, Some(ex.getMessage)))
      }
      Future.successful(Redirect(routes.ReturnsProcessingController.onPageLoad(isUserClaimingCredit)))
    }

  private def displayPage(request: DataRequest[_], obligation: TaxReturnObligation)(implicit
    messages: Messages,
    hc: HeaderCarrier
  ): Future[Result] = {

    request.userAnswers.get(WhatDoYouWantToDoPage) match {
      // Assumption - if user has answered this question, then it cannot be their first return
      case Some(isUserClaimingCredit) => displayPage2(request, obligation, isUserClaimingCredit, isFirstReturn = false)
      case None =>
        taxReturnHelper.nextOpenObligationAndIfFirst(request.pptReference).flatMap {
          // User's first return, so they cannot claim credit
          case Some((_, true)) => displayPage2(request, obligation, isUserClaimingCredit = false, isFirstReturn = true)
          case _               => Future.successful(Redirect(controllers.routes.IndexController.onPageLoad))
        }
    }
  }

  private def displayPage2(
    request: DataRequest[_],
    obligation: TaxReturnObligation,
    isUserClaimingCredit: Boolean,
    isFirstReturn: Boolean
  )(implicit messages: Messages, hc: HeaderCarrier): Future[Result] =
    callCalculationAndCreditApi(request, isUserClaimingCredit, isFirstReturn).map { case (calculations, credits) =>
      val returnViewModel = TaxReturnViewModel(request.userAnswers, request.pptReference, obligation, calculations)
      Ok(view(returnViewModel, credits, navigator.cyaChangeCredits)(request, messages))
    }

  private def callCalculationAndCreditApi(
    request: DataRequest[_],
    isUserClaimingCredit: Boolean,
    isFirstReturn: Boolean
  )(implicit messages: Messages, hc: HeaderCarrier): Future[(Calculations, Credits)] = {

    val eventualCalculations = returnsConnector.getCalculationReturns(request.pptReference)
    val eventualCredits      = getCredits(request, isUserClaimingCredit, isFirstReturn)
    for {
      calculations <- eventualCalculations
      credits      <- eventualCredits
    } yield (calculations, credits) match {
      case (Right(calculations), credits) => (calculations, credits)
      case _ =>
        throw new RuntimeException("Error: There was a problem retrieving return calculation or the credits balance")
    }
  }

  private def getCredits(request: DataRequest[_], isUserClaimingCredit: Boolean, isFirstReturn: Boolean)(implicit
    hc: HeaderCarrier,
    messages: Messages
  ): Future[Credits] =
    if (isFirstReturn)
      Future.successful(NoCreditAvailable)
    else if (isUserClaimingCredit)
      creditsCalculatorConnector.getEventually(request.pptReference).map { creditBalance =>
        CreditsClaimedDetails(request.userAnswers, creditBalance = creditBalance)
      }
    else
      Future.successful(NoCreditsClaimed)

}
