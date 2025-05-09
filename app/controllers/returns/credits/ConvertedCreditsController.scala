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
import controllers.actions._
import forms.returns.credits.ConvertedCreditsFormProvider
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
import play.api.mvc._
import views.html.returns.credits.ConvertedCreditsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ConvertedCreditsController @Inject() (
  override val messagesApi: MessagesApi,
  cacheConnector: CacheConnector,
  navigator: ReturnsJourneyNavigator,
  journeyAction: JourneyAction,
  formProvider: ConvertedCreditsFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: ConvertedCreditsView
)(implicit ec: ExecutionContext)
    extends I18nSupport {

  def onPageLoad(key: String, mode: Mode): Action[AnyContent] =
    journeyAction.async { implicit request =>
      ReturnsUserAnswers.checkCreditYear(request, key, mode) { singleYearClaim =>
        val preparedForm =
          request.userAnswers.fillWithFunc(ConvertedCreditsPage(key), formProvider(), CreditsAnswer.fillFormYesNo)
        createView(preparedForm, key, mode, singleYearClaim) map (Results.Ok(_))
      }
    }

  def onSubmit(key: String, mode: Mode): Action[AnyContent] =
    journeyAction.async { implicit request =>
      ReturnsUserAnswers.checkCreditYear(request, key, mode) { singleYearClaim =>
        formProvider()
          .bindFromRequest()
          .fold(
            formWithErrors => createView(formWithErrors, key, mode, singleYearClaim) map (Results.BadRequest(_)),
            formValue => {
              val saveFunc = cacheConnector.saveUserAnswerFunc(request.pptReference)
              request.userAnswers.changeWithFunc(
                ConvertedCreditsPage(key),
                CreditsAnswer.changeYesNoTo(formValue),
                saveFunc
              )
                .map(_ => Results.Redirect(navigator.convertedCreditsYesNo(mode, key, formValue)))
            }
          )
      }
    }

  private def createView(form: Form[Boolean], key: String, mode: Mode, singleYearClaim: SingleYearClaim)(implicit
    request: DataRequest[_]
  ) =
    Future.successful(view(form, key, mode, singleYearClaim.createCreditRangeOption()))

}
