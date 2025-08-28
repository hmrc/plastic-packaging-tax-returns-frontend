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
import forms.changeGroupLead.MainContactNameFormProvider
import models.Mode
import models.requests.DataRequest._
import navigation.ChangeGroupLeadNavigator
import pages.changeGroupLead.{ChooseNewGroupLeadPage, MainContactNamePage}
import play.api.data.FormBinding.Implicits.formBinding
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Results.{BadRequest, Ok, Redirect}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import views.html.changeGroupLead.MainContactNameView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class MainContactNameController @Inject() (
  override val messagesApi: MessagesApi,
  cacheConnector: CacheConnector,
  journeyAction: JourneyAction,
  form: MainContactNameFormProvider,
  val controllerComponents: MessagesControllerComponents,
  navigator: ChangeGroupLeadNavigator,
  view: MainContactNameView
)(implicit ec: ExecutionContext)
    extends I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = journeyAction { implicit request =>
    val selectedMember = request.userAnswers.getOrFail(ChooseNewGroupLeadPage)
    val preparedForm   = request.userAnswers.fill(MainContactNamePage, form())

    Ok(view(preparedForm, selectedMember.organisationName, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = journeyAction.async { implicit request =>
    val selectedMember = request.userAnswers.getOrFail(ChooseNewGroupLeadPage)

    form().bindFromRequest().fold(
      formWithErrors => Future.successful(BadRequest(view(formWithErrors, selectedMember.organisationName, mode))),
      mainContactName =>
        request.userAnswers
          .setOrFail(MainContactNamePage, mainContactName)
          .save(cacheConnector.saveUserAnswerFunc(request.pptReference))
          .map(_ => Redirect(navigator.mainContactName(mode)))
    )
  }
}
