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
import forms.returns.credits.ExportedCreditsFormProvider
import models.Mode
import models.requests.DataRequest
import models.requests.DataRequest.headerCarrier
import models.returns.{CreditRangeOption, CreditsAnswer}
import navigation.ReturnsJourneyNavigator
import pages.returns.credits.ExportedCreditsPage
import play.api.data.Form
import play.api.data.FormBinding.Implicits.formBinding
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.JsPath
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Results}
import views.html.returns.credits.ExportedCreditsView

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ExportedCreditsController @Inject()
(
  override val messagesApi: MessagesApi,
  cacheConnector: CacheConnector,
  navigator: ReturnsJourneyNavigator,
  journeyAction: JourneyAction,
  formProvider: ExportedCreditsFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: ExportedCreditsView
)(implicit ec: ExecutionContext) extends I18nSupport {

  def onPageLoad(key: String, mode: Mode): Action[AnyContent] = {
    journeyAction {
      implicit request =>

        val preparedForm = request.userAnswers.fillWithFunc(ExportedCreditsPage(key), formProvider(), CreditsAnswer.fillFormYesNo)
        Results.Ok(createView(preparedForm, key, mode))
    }
  }

  def onSubmit(key: String, mode: Mode): Action[AnyContent] =
    journeyAction.async {
      implicit request =>
        formProvider()
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(Results.BadRequest(createView(formWithErrors, key, mode))),
            formValue => {
              val saveFunc = cacheConnector.saveUserAnswerFunc(request.pptReference)
              request.userAnswers
                .changeWithFunc(ExportedCreditsPage(key), CreditsAnswer.changeYesNoTo(formValue), saveFunc)
                .map(_ => Results.Redirect(navigator.exportedCreditsYesNo(key, mode, formValue, request.userAnswers)))
            }
          )
    }

  private def createView(form: Form[Boolean], key: String, mode: Mode)(implicit request: DataRequest[_]) = {
    val fromDate = request.userAnswers.getOrFail[String](JsPath \ "credit" \ key \ "fromDate")
    val toDate = request.userAnswers.getOrFail[String](JsPath \ "credit" \ key \ "toDate")
    val creditRangeOption = CreditRangeOption(LocalDate.parse(fromDate), LocalDate.parse(toDate))
    view(form, key, mode, creditRangeOption)
  }
    
}
