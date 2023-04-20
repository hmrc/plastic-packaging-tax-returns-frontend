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
import controllers.actions.JourneyAction
import forms.returns.credits.ConvertedCreditsWeightFormProvider
import models.Mode
import models.requests.DataRequest
import models.requests.DataRequest.headerCarrier
import navigation.ReturnsJourneyNavigator
import play.api.data.Form
import play.api.data.FormBinding.Implicits.formBinding
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.JsPath
import play.api.mvc.Results.Ok
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Results}
import views.html.returns.credits.ConvertedCreditsWeightView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ConvertedCreditsWeightController @Inject()(
  override val messagesApi: MessagesApi,
  journeyAction: JourneyAction,
  val controllerComponents: MessagesControllerComponents,
  view: ConvertedCreditsWeightView,
  formProvider: ConvertedCreditsWeightFormProvider,
  cacheConnector: CacheConnector, 
)
  (implicit ec: ExecutionContext) extends I18nSupport {

  private val userAnswerPath = JsPath \ "convertedCredits" \ "weight"

  def onPageLoad(mode: Mode): Action[AnyContent] =
    journeyAction {
      implicit request =>
        val form = request.userAnswers.fill(userAnswerPath, formProvider())
        Ok(createView(form, mode))
    }

  private def createView(form: Form[Long], mode: Mode)(implicit request: DataRequest[_]) = {
    view(form, routes.ConvertedCreditsWeightController.onSubmit(mode))
  }

  def onSubmit(mode: Mode) : Action[AnyContent] =
    journeyAction.async {
      implicit request =>
        formProvider()
          .bindFromRequest()
          .fold(formHasErrors(mode, _),formIsGood(mode, _))
    }

  private def formIsGood(mode: Mode, answer: Long) (implicit request: DataRequest[AnyContent]) = {
    val userAnswersSaveFunc = cacheConnector.saveUserAnswerFunc(request.pptReference)
    request.userAnswers.changeWithPath(userAnswerPath, answer, userAnswersSaveFunc)
      .map { _ =>
        val nextPage = controllers.returns.credits.routes.ConfirmPackagingCreditController.onPageLoad(mode)
        Results.Redirect(nextPage)
      }
  }

  private def formHasErrors(mode: Mode, formWithErrors: Form[Long]) (implicit request: DataRequest[AnyContent]) = {
    Future.successful(Results.BadRequest(createView(formWithErrors, mode)))
  }
}
