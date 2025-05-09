/*
 * Copyright 2025 HM Revenue & Customs
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

import cacheables.ReturnObligationCacheable
import connectors.CacheConnector
import controllers.actions._
import forms.returns.credits.DoYouWantToClaimFormProvider
import models.{ReturnsUserAnswers, UserAnswers}
import models.requests.DataRequest.headerCarrier
import navigation.ReturnsJourneyNavigator
import pages.returns.credits.WhatDoYouWantToDoPage
import play.api.data.FormBinding.Implicits.formBinding
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Results.{BadRequest, Ok, Redirect}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.http.HeaderCarrier
import views.html.returns.credits.DoYouWantToClaimView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class WhatDoYouWantToDoController @Inject() (
  override val messagesApi: MessagesApi,
  cacheConnector: CacheConnector,
  journeyAction: JourneyAction,
  formProvider: DoYouWantToClaimFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: DoYouWantToClaimView,
  returnsNavigator: ReturnsJourneyNavigator
)(implicit ec: ExecutionContext)
    extends I18nSupport {

  def onPageLoad: Action[AnyContent] =
    journeyAction { implicit request =>
      ReturnsUserAnswers.checkObligationSync(request) { obligation =>
        val preparedForm = request.userAnswers.fill(WhatDoYouWantToDoPage, formProvider())
        Ok(view(preparedForm, obligation))
      }
    }

  def onSubmit: Action[AnyContent] =
    journeyAction.async { implicit request =>
      formProvider().bindFromRequest().fold(
        formWithErrors => {
          val obligation = request.userAnswers.getOrFail(ReturnObligationCacheable)
          Future.successful(BadRequest(view(formWithErrors, obligation)))
        },
        newAnswer => updateAnswersAndGotoNextPage(request.pptReference, request.userAnswers, newAnswer)
      )
    }

  private def updateAnswersAndGotoNextPage(pptReference: String, previousAnswers: UserAnswers, newAnswer: Boolean)(
    implicit hc: HeaderCarrier
  ) =
    previousAnswers
      .change(WhatDoYouWantToDoPage, newAnswer, cacheConnector.saveUserAnswerFunc(pptReference))
      .map(_ => Redirect(returnsNavigator.whatDoYouWantDo(newAnswer)))

}
