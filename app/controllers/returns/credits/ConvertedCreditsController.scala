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

package controllers.returns.credits

import connectors.CacheConnector
import controllers.actions._
import controllers.returns.credits.JourneyAction.{RequestFunction, RequestAsyncFunction}
import forms.returns.credits.ConvertedCreditsFormProvider
import models.Mode
import models.requests.DataRequest
import navigation.ReturnsJourneyNavigator
import pages.returns.credits.{ConvertedCreditsPage, WhatDoYouWantToDoPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.returns.credits.ConvertedCreditsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}


class JourneyAction @Inject()(
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
) {
  def async(function: RequestAsyncFunction): Action[AnyContent] =
    identify.andThen(getData.andThen(requireData)).async(function)

  def apply(function: RequestFunction): Action[AnyContent] =
    identify.andThen(getData.andThen(requireData)).apply(function)
}

object JourneyAction {
  type RequestFunction = DataRequest[AnyContent] => Result
  type RequestAsyncFunction = DataRequest[AnyContent] => Future[Result]
}

class ConvertedCreditsController @Inject()
(
  override val messagesApi: MessagesApi,
  cacheConnector: CacheConnector,
  navigator: ReturnsJourneyNavigator,
  journeyAction: JourneyAction,
  formProvider: ConvertedCreditsFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: ConvertedCreditsView
)(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {


  def onPageLoad(mode: Mode): Action[AnyContent] = {
    journeyAction {
      implicit request =>
        val preparedForm = request.userAnswers.fill(ConvertedCreditsPage, formProvider.apply())
        Ok(view(preparedForm, mode))
    }
  }

  implicit private def createHeaderCarrier(implicit request: DataRequest[_]): HeaderCarrier =
    request.headerCarrier

  def onSubmit(mode: Mode): Action[AnyContent] = journeyAction.async {
    implicit request: DataRequest[AnyContent] =>
      formProvider()
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
          formValue => {
            request.userAnswers
              .setOrFail(ConvertedCreditsPage, formValue)
              .setOrFail(WhatDoYouWantToDoPage, true)
              .save(cacheConnector.saveUserAnswerFunc(request.pptReference))
              .map(updatedAnswers => Redirect(navigator.convertedCreditsRoute(mode, ClaimedCredits(updatedAnswers))))
          }
        )
  }
}
