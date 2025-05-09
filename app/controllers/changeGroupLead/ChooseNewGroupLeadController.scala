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

import connectors.CacheConnector
import controllers.actions._
import forms.changeGroupLead.SelectNewGroupLeadForm
import models.Mode
import models.requests.DataRequest
import models.requests.DataRequest._
import models.subscription.{GroupMembers, Member}
import navigation.ChangeGroupLeadNavigator
import pages.changeGroupLead.ChooseNewGroupLeadPage
import play.api.data.Form
import play.api.data.FormBinding.Implicits.formBinding
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import services.SubscriptionService
import views.html.changeGroupLead.ChooseNewGroupLeadView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ChooseNewGroupLeadController @Inject() (
  override val messagesApi: MessagesApi,
  journeyAction: JourneyAction,
  val controllerComponents: MessagesControllerComponents,
  view: ChooseNewGroupLeadView,
  form: SelectNewGroupLeadForm,
  cacheConnector: CacheConnector,
  subscriptionService: SubscriptionService,
  navigator: ChangeGroupLeadNavigator
)(implicit ec: ExecutionContext)
    extends I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = journeyAction.async { implicit request =>
    subscriptionService.fetchGroupMemberNames(request.pptReference).map { members =>
      val preparedForm = request.userAnswers.fill(ChooseNewGroupLeadPage, form(members.membersNames))
      Results.Ok(createView(preparedForm, members, mode))
    }
  }

  private def createView(form: Form[Member], members: GroupMembers, mode: Mode)(implicit request: DataRequest[_]) = {
    val onSubmit = routes.ChooseNewGroupLeadController.onSubmit(mode)
    view(form, members, onSubmit)
  }

  def onSubmit(mode: Mode): Action[AnyContent] = journeyAction.async { implicit request =>
    subscriptionService.fetchGroupMemberNames(request.pptReference).flatMap { members =>
      form(members.membersNames)
        .bindFromRequest()
        .fold(
          errorForm => Future.successful(Results.BadRequest(createView(errorForm, members, mode))),
          selectedMember =>
            request.userAnswers
              .setOrFail(ChooseNewGroupLeadPage, selectedMember)
              .save(cacheConnector.saveUserAnswerFunc(request.pptReference))
              .map(_ => Results.Redirect(navigator.selectNewGroupRep(mode)))
        )
    }
  }

}
