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

package controllers

import controllers.ViewReturnSummaryController.AmendSelectedPeriodKey
import controllers.actions._
import controllers.helpers.TaxReturnHelper
import models.UserAnswers
import models.returns.ReturnDisplayApi
import navigation.Navigator
import pages.AmendHumanMedicinePlasticPackagingPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.JsPath
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import queries.{Gettable, Settable}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.ViewReturnSummaryViewModel
import views.html.ViewReturnSummaryView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ViewReturnSummaryController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  sessionRepository: SessionRepository,
  getData: DataRetrievalAction,
  val controllerComponents: MessagesControllerComponents,
  view: ViewReturnSummaryView,
  taxReturnHelper: TaxReturnHelper
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  def onPageLoad(periodKey: String): Action[AnyContent] =
    (identify andThen getData).async {
      implicit request =>
        val enrolmentId = request.request.enrolmentId.getOrElse(throw new IllegalArgumentException("Make this not optional?")) // TODO
        val submittedReturnF: Future[ReturnDisplayApi] = taxReturnHelper.fetchTaxReturn(enrolmentId, periodKey.toUpperCase())

        for {
          updatedAnswers <- Future.fromTry(
            request.userAnswers.getOrElse(UserAnswers(request.userId)).set(AmendSelectedPeriodKey, periodKey)
          )
          _ <- sessionRepository.set(updatedAnswers)
          submittedReturn <- submittedReturnF
        } yield {
          val returnPeriod = submittedReturn.calculatePeriodString()
          Ok(view(returnPeriod, ViewReturnSummaryViewModel(submittedReturn)))
        }
    }

}

object ViewReturnSummaryController {

//Cacheables?
  //todo move this to pages? or models? what is it :thinking:
  case object AmendSelectedPeriodKey extends Gettable[String] with Settable[String] {
    override def path: JsPath = JsPath \ toString

    override def toString: String = "amendSelectedPeriodKey"
  }

  case object AmendReturnPreviousReturn extends Gettable[ReturnDisplayApi] with Settable[ReturnDisplayApi] {
    override def path: JsPath = JsPath \ toString

    override def toString: String = "amendReturnPreviousReturn"
  }
}
