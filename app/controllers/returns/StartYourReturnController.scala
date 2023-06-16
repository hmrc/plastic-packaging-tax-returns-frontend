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

import audit.Auditor
import cacheables.ReturnObligationCacheable
import connectors.CacheConnector
import controllers.actions._
import controllers.helpers.TaxReturnHelper
import forms.returns.StartYourReturnFormProvider
import models.UserAnswers
import models.requests.DataRequest
import models.requests.DataRequest.headerCarrier
import models.returns.TaxReturnObligation
import navigation.ReturnsJourneyNavigator
import pages.returns.StartYourReturnPage
import play.api.Logging
import play.api.data.FormBinding.Implicits.formBinding
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.JsPath
import play.api.mvc.Results.{BadRequest, Ok, Redirect}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import views.html.returns.StartYourReturnView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class StartYourReturnController @Inject() (
  override val messagesApi: MessagesApi,
  cacheConnector: CacheConnector,
  journeyAction: JourneyAction,
  form: StartYourReturnFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: StartYourReturnView,
  taxReturnHelper: TaxReturnHelper,
  auditor: Auditor,
  returnsNavigator: ReturnsJourneyNavigator
)(implicit ec: ExecutionContext)
    extends I18nSupport with Logging {

  def onPageLoad(): Action[AnyContent] =
    journeyAction.async {
      implicit request =>
        taxReturnHelper.nextOpenObligationAndIfFirst(request.pptReference).flatMap {
          case Some((taxReturnObligation, false)) => viewCredit(taxReturnObligation)
          case Some((taxReturnObligation, true)) => viewFirstReturn(taxReturnObligation)
          case None =>
            logger.info("Trying to start return with no obligation. Redirecting to account homepage.")
            Future.successful(Redirect(controllers.routes.IndexController.onPageLoad))
        }
    }

  private def viewCredit(
    taxReturnObligation: TaxReturnObligation
  )(implicit request: DataRequest[AnyContent]): Future[Result] = {
    saveUserAnswer(false, taxReturnObligation)
      .map(_ => Redirect(controllers.returns.credits.routes.WhatDoYouWantToDoController.onPageLoad))
  }

  private def viewFirstReturn(
                                 taxReturnObligation: TaxReturnObligation
                               )(implicit request: DataRequest[AnyContent]): Future[Result] = {
    val preparedForm = request.userAnswers.fill(StartYourReturnPage, form())

    saveUserAnswer(true, taxReturnObligation)
      .map(_ => Ok(view(preparedForm, taxReturnObligation, true)))
  }

  private def saveUserAnswer(
    isFirstReturn: Boolean,
    taxReturnObligation: TaxReturnObligation
  )(implicit request: DataRequest[AnyContent]): Future[UserAnswers] = {
    request.userAnswers
      .setOrFail(ReturnObligationCacheable, taxReturnObligation)
      .setOrFail(JsPath \ "isFirstReturn", isFirstReturn)
      .save(cacheConnector.saveUserAnswerFunc(request.pptReference))
  }

  def onSubmit(): Action[AnyContent] =
    journeyAction.async {
      implicit request =>
        val userAnswers   = request.userAnswers
        val pptReference  = request.pptReference
        val obligation    = userAnswers.getOrFail(ReturnObligationCacheable)
        val isFirstReturn = userAnswers.getOrFail[Boolean](JsPath \ "isFirstReturn")

        form().bindFromRequest().fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, obligation, isFirstReturn))),
          formValue =>
            userAnswers
              .setOrFail(StartYourReturnPage, formValue)
              .save(cacheConnector.saveUserAnswerFunc(pptReference))
              .map(_ => act(formValue))
        )
    }

  private def act(formValue: Boolean) (implicit request: DataRequest[_]) = {
    if (formValue) {
      auditor.returnStarted(request.request.user.identityData.internalId, request.pptReference)
    }
    Redirect(returnsNavigator.startYourReturn(formValue))
  }

}
