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

package controllers.returns.credits

import controllers.actions.JourneyAction
import forms.returns.credits.WhatDoYouWantToDoFormProvider
import models.Mode
import models.requests.DataRequest
import navigation.ReturnsJourneyNavigator
import pages.returns.credits.WhatDoYouWantToDoPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Results.Ok
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Results}
import views.html.returns.credits.ClaimForWhichYearView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ClaimForWhichYearController @Inject()(
  override val messagesApi: MessagesApi,
  journeyAction: JourneyAction,
  val controllerComponents: MessagesControllerComponents,
  view: ClaimForWhichYearView,
  formProvider: WhatDoYouWantToDoFormProvider, // todo tmp
  navigator: ReturnsJourneyNavigator,

)
  (implicit ec: ExecutionContext) extends I18nSupport {

  def onPageLoad: Action[AnyContent] =
    journeyAction {
      implicit request =>
        val form = request.userAnswers.fill(WhatDoYouWantToDoPage, formProvider())
        Ok(view(form))
    }


  def onSubmit: Action[AnyContent] =
    journeyAction.async {
      implicit request =>
        Future.successful(Results.Redirect(navigator.claimForWhichYear)
      )
    }


}
