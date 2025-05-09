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

import connectors.CacheConnector
import controllers.actions.JourneyAction
import forms.returns.credits.ExportedCreditsWeightFormProvider
import models.requests.DataRequest
import models.requests.DataRequest.headerCarrier
import models.returns.CreditsAnswer
import models.returns.credits.SingleYearClaim
import models.{Mode, ReturnsUserAnswers}
import navigation.ReturnsJourneyNavigator
import pages.returns.credits.ExportedCreditsPage
import play.api.data.Form
import play.api.data.FormBinding.Implicits.formBinding
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Results.{BadRequest, Ok}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Results}
import views.html.returns.credits.ExportedCreditsWeightView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ExportedCreditsWeightController @Inject() (
  override val messagesApi: MessagesApi,
  journeyAction: JourneyAction,
  cacheConnector: CacheConnector,
  val controllerComponents: MessagesControllerComponents,
  view: ExportedCreditsWeightView,
  formProvider: ExportedCreditsWeightFormProvider,
  navigator: ReturnsJourneyNavigator
)(implicit ec: ExecutionContext)
    extends I18nSupport {

  def onPageLoad(key: String, mode: Mode): Action[AnyContent] =
    journeyAction.async { implicit request =>
      ReturnsUserAnswers.checkCreditYear(request, key, mode) { singleYearClaim =>
        val form =
          request.userAnswers.fillWithFunc(ExportedCreditsPage(key), formProvider(), CreditsAnswer.fillFormWeight)
        createView(form, key, mode, singleYearClaim) map (Ok(_))
      }
    }

  def onSubmit(key: String, mode: Mode): Action[AnyContent] = journeyAction.async { implicit request =>
    ReturnsUserAnswers.checkCreditYear(request, key, mode) { singleYearClaim =>
      formProvider().bindFromRequest().fold(
        formWithErrors => createView(formWithErrors, key, mode, singleYearClaim) map (BadRequest(_)),
        formValue =>
          request.userAnswers
            .setOrFail(ExportedCreditsPage(key), CreditsAnswer.answerWeightWith(formValue))
            .save(cacheConnector.saveUserAnswerFunc(request.pptReference))
            .map(_ => Results.Redirect(navigator.exportedCreditsWeight(key, mode, request.userAnswers)))
      )
    }
  }

  private def createView(form: Form[Long], key: String, mode: Mode, singleYearClaim: SingleYearClaim)(implicit
    request: DataRequest[_]
  ) =
    Future.successful(view(form, key, mode, singleYearClaim.createCreditRangeOption()))

}
