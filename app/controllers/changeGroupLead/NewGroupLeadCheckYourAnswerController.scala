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

package controllers.changeGroupLead

import controllers.actions.JourneyAction
import models.changeGroupLead.RepresentativeMemberDetails
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.changeGroupLead.NewGroupLeadCheckYourAnswerView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class NewGroupLeadCheckYourAnswerController @Inject
(
  override val messagesApi: MessagesApi,
  journeyAction: JourneyAction,
  featureGuard: FeatureGuard,
  val controllerComponents: MessagesControllerComponents,
  view: NewGroupLeadCheckYourAnswerView
)
(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport{

  def onPageLoad: Action[AnyContent] = journeyAction {
    implicit request =>
      featureGuard.check()
      Ok(view(RepresentativeMemberDetails(request.userAnswers)))
  }

  def onSubmit: Action[AnyContent] = journeyAction.async {
    implicit request =>
      /*
      todo: call sub create api and redirect to confirmation page if Success
      otherwise a error on page
       */
      //otherwise
      featureGuard.check()
      Future.successful(Redirect(controllers.changeGroupLead.routes.NewGroupLeadConfirmationController.onPageLoad))
  }

}
