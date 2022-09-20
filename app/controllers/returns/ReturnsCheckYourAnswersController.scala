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

package controllers.returns

import cacheables.ReturnObligationCacheable
import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.{CalculateCreditsConnector, ServiceError, TaxReturnsConnector}
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import controllers.helpers.TaxReturnViewModel
import models.requests.DataRequest
import models.returns._
import pages.returns.credits.WhatDoYouWantToDoPage
import play.api.Logging
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.{Entry, SessionRepository}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.returns.ReturnsCheckYourAnswersView

import scala.concurrent.{ExecutionContext, Future}

class ReturnsCheckYourAnswersController @Inject()(
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  returnsConnector: TaxReturnsConnector,
  creditsCalculatorConnector: CalculateCreditsConnector,
  sessionRepository: SessionRepository,
  val controllerComponents: MessagesControllerComponents,
  view: ReturnsCheckYourAnswersView
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
        returnsConnector.submit(request.pptReference).flatMap {
          case Right(optChargeRef) =>
            sessionRepository.set(Entry(request.cacheKey, optChargeRef)).map{
              _ => Redirect(routes.ReturnConfirmationController.onPageLoad())
            }
          case Left(error) => throw error
        }
    }

  private def calCalculationAndCreditApi(
                                          request: DataRequest[_]
                                        )
                                        (implicit hc: HeaderCarrier): Future[(Calculations, Credits)] = {
    val fCalculations = returnsConnector.getCalculationReturns(request.pptReference)
    val fCredits = getCredits(request)

    for {
      calculations <- fCalculations
      credits <- fCredits
    } yield (calculations, credits) match {
      case (Right(calculations), Right(credits)) => (calculations, credits)
      case _ => throw new RuntimeException("Error: There was a problem retrieving return calculation or the credits balance")
    }
  }
  private def displayPage(request: DataRequest[_], obligation: TaxReturnObligation)
                         (implicit messages: Messages, hc: HeaderCarrier): Future[Result] = {

    calCalculationAndCreditApi(request).map {
      case (calculations, credits) =>
        val returnViewModel = TaxReturnViewModel(request, obligation, calculations)

        Ok(view(returnViewModel, credits)(request, messages))
    }
  }

  private def getCredits(request: DataRequest[_])
                        (implicit hc: HeaderCarrier): Future[Either[ServiceError, Credits]] = {
    if(request.userAnswers.getOrFail(WhatDoYouWantToDoPage)) {
        creditsCalculatorConnector.get(request.pptReference).map {
          case Right(creditBalance) => Right(CreditsClaimedDetails(request.userAnswers, creditBalance = creditBalance))
          case Left(error) => Left(error)
        }
      } else {
        Future.successful(Right(NoCreditsClaimed))
      }
  }

}
