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
import connectors.{CacheConnector, CalculateCreditsConnector, ServiceError, TaxReturnsConnector}
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import controllers.helpers.TaxReturnHelper
import models.UserAnswers
import models.requests.DataRequest
import models.returns.Credits._
import models.returns._
import pages.returns.credits.{ConvertedCreditsPage, ExportedCreditsPage, WhatDoYouWantToDoPage}
import play.api.Logging
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import repositories.SessionRepository.Paths
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.TaxReturnViewModel
import views.html.returns.ReturnsCheckYourAnswersView

import scala.concurrent.{ExecutionContext, Future}

class ReturnsCheckYourAnswersController @Inject()(
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  returnsConnector: TaxReturnsConnector,
  creditsCalculatorConnector: CalculateCreditsConnector,
  taxReturnHelper: TaxReturnHelper,
  sessionRepository: SessionRepository,
  val controllerComponents: MessagesControllerComponents,
  appConfig: FrontendAppConfig,
  view: ReturnsCheckYourAnswersView, 
  cacheConnector: CacheConnector
)(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {

  def onPageLoad(): Action[AnyContent] =
    (identify andThen getData andThen requireData).async {
      implicit request =>
        request.userAnswers.get[TaxReturnObligation](ReturnObligationCacheable) match {
          case Some(obligation) => displayPage(request, obligation)
          case None => Future.successful(Redirect(controllers.routes.IndexController.onPageLoad))
        }
    }
  
  def onSubmit(): Action[AnyContent] =
    (identify andThen getData andThen requireData).async {
      implicit request =>
        val userAnswers: UserAnswers = request.userAnswers
        ensureCreditAnswersConsistency(userAnswers, request.pptReference).flatMap { isUserClaimingCredit =>
          returnsConnector.submit(request.pptReference).flatMap {
            case Right(optChargeRef) =>
              sessionRepository.set(request.cacheKey, Paths.ReturnChargeRef, optChargeRef).map {
                _ => Redirect(routes.ReturnConfirmationController.onPageLoad(isUserClaimingCredit))
              }
            case Left(_) =>
              val returnQuarter = userAnswers.getOrFail(ReturnObligationCacheable)
              sessionRepository.set(request.cacheKey, Paths.TaxReturnObligation, returnQuarter).map { _ =>
                Redirect(routes.AlreadySubmittedController.onPageLoad())
              }
          }
        }
    }

  def onRemoveCreditsClaim(): Action[AnyContent] =
    (identify andThen getData andThen requireData).async {
      implicit request =>
        request.userAnswers
          .setOrFail(WhatDoYouWantToDoPage, false)
          .save(cacheConnector.saveUserAnswerFunc(request.pptReference))
          .map(_ => Redirect(routes.ReturnsCheckYourAnswersController.onPageLoad()))
    }

  /** TODO quick fix for user removing any previous credit claim
    * @return true if the user is claiming a credit
    */
  private def ensureCreditAnswersConsistency(userAnswers: UserAnswers, pptReference: String)(implicit hc: HeaderCarrier)
  : Future[Boolean] =
    userAnswers.get(WhatDoYouWantToDoPage) match {
      case Some(true) => Future.successful(true)
      case _          =>
        userAnswers
          .setOrFail(ExportedCreditsPage, false)
          .setOrFail(ConvertedCreditsPage, CreditsAnswer.noClaim)
          .save(cacheConnector.saveUserAnswerFunc(pptReference))
          .map(_ => false)
    }

  private def callCalculationAndCreditApi(
                                          request: DataRequest[_]
                                        )
                                        (implicit hc: HeaderCarrier): Future[(Calculations, Credits)] = {
    val fCalculations = returnsConnector.getCalculationReturns(request.pptReference)
    val fIsFirstReturn = taxReturnHelper.nextOpenObligationAndIfFirst(request.pptReference)

    for {
      calculations <- fCalculations
      obligation <- fIsFirstReturn
      isFirstReturn = obligation.fold(false)(_._2)
      credits <- getCredits(request, isFirstReturn)
    } yield (calculations, credits) match {
      case (Right(calculations), Right(credits)) => (calculations, credits)
      case _ => throw new RuntimeException("Error: There was a problem retrieving return calculation or the credits balance")
    }
  }
  private def displayPage(request: DataRequest[_], obligation: TaxReturnObligation)
                         (implicit messages: Messages, hc: HeaderCarrier): Future[Result] = {

    callCalculationAndCreditApi(request).map {
      case (calculations, credits) =>
        val returnViewModel = TaxReturnViewModel(request.userAnswers, request.pptReference, obligation, calculations)
        Ok(view(returnViewModel, credits)(request, messages))
    }
  }

  private def getCredits(request: DataRequest[_], isFirstReturn: Boolean)
                        (implicit hc: HeaderCarrier): Future[Either[ServiceError, Credits]] =
    if (isFirstReturn || !appConfig.isCreditsForReturnsFeatureEnabled)
      Future.successful(Right(NoCreditAvailable))
    else if(request.userAnswers.getOrFail(WhatDoYouWantToDoPage))
        creditsCalculatorConnector.get(request.pptReference).map {
          case Right(creditBalance) => Right(CreditsClaimedDetails(request.userAnswers, creditBalance = creditBalance))
          case Left(error) => Left(error)
        }
    else
      Future.successful(Right(NoCreditsClaimed))

}
