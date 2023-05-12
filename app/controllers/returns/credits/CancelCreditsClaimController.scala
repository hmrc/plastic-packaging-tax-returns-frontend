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

import connectors.CacheConnector
import controllers.actions._
import forms.returns.credits.CancelCreditsClaimFormProvider
import models.requests.DataRequest.headerCarrier
import navigation.ReturnsJourneyNavigator
import play.api.data.FormBinding.Implicits.formBinding
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.JsPath
import play.api.mvc.Results.{BadRequest, Ok, Redirect}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import views.html.returns.credits.CancelCreditsClaimView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

/*
  Todo: There is already a RemoveCreditController, used by the ReturnsCheckYourAnswer page
  which only reset to false the WhatDoYouWantToDoPage page. We may could consolidate
  this CancelCreditsClaimController and the RemoveCreditController into one.
  Not done it at this point as we have not looked into the ReturnsCheckYourAnswer page
  which will need to change.
 */
class CancelCreditsClaimController @Inject()( //todo name better this will remove one year of credit
  override val messagesApi: MessagesApi,
  cacheConnector: CacheConnector,
  navigator: ReturnsJourneyNavigator,
  journeyAction: JourneyAction,
  formProvider: CancelCreditsClaimFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: CancelCreditsClaimView
) (implicit ec: ExecutionContext) extends I18nSupport {

  def onPageLoad(key: String): Action[AnyContent] = journeyAction {
    implicit request =>
      //todo view is hardcoded, it should pull back the date range in question
      Ok(view(key, formProvider()))
  }

  def onSubmit(key: String): Action[AnyContent] = journeyAction.async {
    implicit request =>

      formProvider().bindFromRequest().fold(
        formWithErrors => Future.successful(BadRequest(view(key, formWithErrors))),
        
        isRemovingYear => {
          {if (isRemovingYear) {
            request.userAnswers
              .removePath(JsPath \ "credit" \ key)
              .save(cacheConnector.saveUserAnswerFunc(request.pptReference))
          } else {Future.unit}
          }.map(_ => Redirect(navigator.cancelCredit(key)))
        }
      )
  }
}
