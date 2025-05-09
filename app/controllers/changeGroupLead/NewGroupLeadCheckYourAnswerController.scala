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

package controllers.changeGroupLead

import connectors.SubscriptionConnector
import controllers.actions.JourneyAction
import models.requests.DataRequest._
import navigation.ChangeGroupLeadNavigator
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Results.{Ok, Redirect}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.CountryService
import viewmodels.checkAnswers.changeGroupLead._
import views.html.changeGroupLead.NewGroupLeadCheckYourAnswerView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class NewGroupLeadCheckYourAnswerController @Inject() (
  override val messagesApi: MessagesApi,
  journeyAction: JourneyAction,
  countryService: CountryService,
  subscriptionConnector: SubscriptionConnector,
  val controllerComponents: MessagesControllerComponents,
  view: NewGroupLeadCheckYourAnswerView,
  navigator: ChangeGroupLeadNavigator
)(implicit ec: ExecutionContext)
    extends I18nSupport {

  def onPageLoad: Action[AnyContent] = journeyAction { implicit request =>
    val summaryRows = Seq(
      ChooseNewGroupLeadSummary,
      NewGroupLeadEnterContactAddressSummary(countryService),
      MainContactNameSummary,
      MainContactJobTitleSummary
    ).flatMap(_.row(request.userAnswers))

    Ok(view(summaryRows))
  }

  def onSubmit: Action[AnyContent] = journeyAction.async { implicit request =>
    subscriptionConnector.changeGroupLead(request.pptReference).map { _ =>
      Redirect(navigator.checkYourAnswers)
    }
  }

}
