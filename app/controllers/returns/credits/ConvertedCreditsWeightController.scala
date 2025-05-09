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
import forms.returns.credits.ConvertedCreditsWeightFormProvider
import models.requests.DataRequest
import models.requests.DataRequest.headerCarrier
import models.returns.CreditsAnswer
import models.returns.credits.SingleYearClaim
import models.{Mode, ReturnsUserAnswers}
import navigation.ReturnsJourneyNavigator
import pages.returns.credits.ConvertedCreditsPage
import play.api.data.Form
import play.api.data.FormBinding.Implicits.formBinding
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Results}
import views.html.returns.credits.ConvertedCreditsWeightView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ConvertedCreditsWeightController @Inject() (
  override val messagesApi: MessagesApi,
  journeyAction: JourneyAction,
  val controllerComponents: MessagesControllerComponents,
  view: ConvertedCreditsWeightView,
  formProvider: ConvertedCreditsWeightFormProvider,
  cacheConnector: CacheConnector,
  navigator: ReturnsJourneyNavigator
)(implicit ec: ExecutionContext)
    extends I18nSupport {

  def onPageLoad(key: String, mode: Mode): Action[AnyContent] =
    journeyAction.async { implicit request =>
      ReturnsUserAnswers.checkCreditYear(request, key, mode) { singleYearClaim =>
        val form =
          request.userAnswers.fillWithFunc(ConvertedCreditsPage(key), formProvider(), CreditsAnswer.fillFormWeight)
        createView(form, key, mode, singleYearClaim) map (Results.Ok(_))
      }
    }

  private def createView(form: Form[Long], key: String, mode: Mode, singleYearClaim: SingleYearClaim)(implicit
    request: DataRequest[_]
  ) = {
    val submitCall = routes.ConvertedCreditsWeightController.onSubmit(key, mode)
    Future.successful(view(form, submitCall, singleYearClaim.createCreditRangeOption()))
  }

  def onSubmit(key: String, mode: Mode): Action[AnyContent] =
    journeyAction.async { implicit request =>
      ReturnsUserAnswers.checkCreditYear(request, key, mode) { singleYearClaim =>
        formProvider()
          .bindFromRequest()
          .fold(
            createView(_, key, mode, singleYearClaim) map (Results.BadRequest(_)),
            answer =>
              request.userAnswers
                .setOrFail(ConvertedCreditsPage(key), CreditsAnswer.answerWeightWith(answer))
                .save(cacheConnector.saveUserAnswerFunc(request.pptReference))
                .map(_ => Results.Redirect(navigator.convertedCreditsWeight(key, mode)))
          )
      }
    }

}
