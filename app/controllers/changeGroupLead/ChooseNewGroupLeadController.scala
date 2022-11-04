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

import config.FrontendAppConfig
import connectors.{CacheConnector, SubscriptionConnector}
import controllers.actions._
import forms.changeGroupLead.SelectNewGroupLeadForm
import models.requests.DataRequest
import pages.ChooseNewGroupLeadPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import services.SubscriptionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.changeGroupLead.ChooseNewGroupLeadView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}


//todo move
class FeatureGuard @Inject() (frontendAppConfig: FrontendAppConfig) {
  def check(): Unit = {
    if (!frontendAppConfig.isFeatureEnabledChangeOfGroupLead)
      throw new RuntimeException("Change of group lead feature is not enabled")
  }
}

class ChooseNewGroupLeadController @Inject() (
  override val messagesApi: MessagesApi,
  journeyAction: JourneyAction,
  val controllerComponents: MessagesControllerComponents,
  view: ChooseNewGroupLeadView,
  form: SelectNewGroupLeadForm,
  cacheConnector: CacheConnector,
  featureGuard: FeatureGuard,
  subscriptionService: SubscriptionService
)
  (implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(): Action[AnyContent] = journeyAction.async {
    implicit request =>
      featureGuard.check()
      subscriptionService.fetchGroupMemberNames(request.pptReference).map{ members =>
        val preparedForm = request.userAnswers.fill(ChooseNewGroupLeadPage, form(members.membersNames))

        Ok(view(preparedForm, members.membersNames))
      }
  }

  def onSubmit(): Action[AnyContent] = journeyAction.async {
    implicit request =>
      featureGuard.check()
      subscriptionService.fetchGroupMemberNames(request.pptReference).flatMap{ members =>
        form(members.membersNames)
          .bindFromRequest()
          .fold(
            errorForm => Future.successful(BadRequest(view(errorForm, members.membersNames))),
            selectedMember =>
              request.userAnswers
                .setOrFail(ChooseNewGroupLeadPage, selectedMember)
                .save(cacheConnector.saveUserAnswerFunc(request.pptReference))
                .map(_ => Redirect(controllers.routes.IndexController.onPageLoad))
          )
      }
  }

}
