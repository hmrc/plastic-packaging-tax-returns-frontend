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

import connectors.{AvailableCreditYearsConnector, CacheConnector}
import controllers.actions.JourneyAction
import forms.returns.credits.ClaimForWhichYearFormProvider
import models.Mode
import models.requests.DataRequest.headerCarrier
import navigation.ReturnsJourneyNavigator
import play.api.data.FormBinding.Implicits.formBinding
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsObject, JsPath}
import play.api.mvc.Results.{Ok, Redirect}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Results}
import views.html.returns.credits.ClaimForWhichYearView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}


class ClaimForWhichYearController @Inject()(
  override val messagesApi: MessagesApi,
  journeyAction: JourneyAction,
  val controllerComponents: MessagesControllerComponents,
  view: ClaimForWhichYearView,
  formProvider: ClaimForWhichYearFormProvider,
  navigator: ReturnsJourneyNavigator,
  cacheConnector: CacheConnector,
  availableCreditYearsConnector: AvailableCreditYearsConnector
)(implicit ec: ExecutionContext) extends I18nSupport {

  //todo this needs check mode, else you loose the state coming from finalCYA
  def onPageLoad(mode: Mode): Action[AnyContent] =
    journeyAction.async { implicit request =>
      //todo should availableYears be put in to useranswers/session cache as future pages will need to hmm :thinking:
      availableCreditYearsConnector.get(request.pptReference).map {
        availableYears =>
          val alreadyUsedYears = request.userAnswers.get[Map[String, JsObject]](JsPath \ "credit").getOrElse(Map.empty).keySet
          val options = availableYears.filterNot(y => alreadyUsedYears.contains(y.key))
          if (options.isEmpty) {
            Redirect(controllers.returns.credits.routes.CreditsClaimedListController.onPageLoad(mode))
          } else {
            val form = formProvider(options)
            Ok(view(form, options, mode))
          }
      }

    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    journeyAction.async { implicit request =>
      availableCreditYearsConnector.get(request.pptReference).flatMap {
        availableYears =>
          val alreadyUsedYears = request.userAnswers.get[Map[String, JsObject]](JsPath \ "credit").getOrElse(Map.empty).keySet
          val options = availableYears.filterNot(y => alreadyUsedYears.contains(y.key))
          formProvider(options)
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(Results.BadRequest(view(formWithErrors, options, mode))),
              selectedRange => {
                request.userAnswers
                  .setOrFail(JsPath \ "credit" \ selectedRange.key \ "endDate", selectedRange.to)
                  .save(cacheConnector.saveUserAnswerFunc(request.pptReference)).map(_ =>
                  Results.Redirect(navigator.claimForWhichYear(selectedRange, mode))
                )
              }
            )
      }


    }
}
