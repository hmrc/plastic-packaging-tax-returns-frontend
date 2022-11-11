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

import connectors.CacheConnector
import controllers.actions._
import forms.changeGroupLead.NewGroupLeadEnterContactAddressFormProvider
import forms.mappings.Mappings
import models.Mode
import models.Mode.NormalMode
import models.requests.DataRequest
import navigation.ChangeGroupLeadNavigator
import play.api.data.Form
import play.api.data.Form
import play.api.data.FormBinding.Implicits.formBinding
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Results}
import views.html.changeGroupLead.NewGroupLeadEnterContactAddressView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class NewGroupLeadEnterContactAddressController @Inject()(
  override val messagesApi: MessagesApi,
  cacheConnector: CacheConnector,
  navigator: ChangeGroupLeadNavigator,
  journeyAction: JourneyAction,
  formProvider: NewGroupLeadEnterContactAddressFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: NewGroupLeadEnterContactAddressView
)(implicit ec: ExecutionContext) extends I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = journeyAction.async {
    implicit request =>
      val preparedForm = request.userAnswers.fill(NewGroupLeadEnterContactAddressPage, formProvider.apply())
      Future.successful(Results.Ok(createView(mode, preparedForm)))
  }

  private def createView(mode: Mode, preparedForm: Form[NewGroupLeadAddressDetails]) (implicit request: DataRequest[_]) = {
    view(preparedForm, "name", mode)
  }

  def onSubmit(implicit mode: Mode): Action[AnyContent] = journeyAction.async {
    implicit request =>

      formProvider().bindFromRequest().fold(
        formWithErrors => Future.successful(Results.BadRequest(view(formWithErrors, "name", mode))),
        newValue =>
          request.userAnswers
            .setOrFail(NewGroupLeadEnterContactAddressPage, newValue)
            .save(cacheConnector.saveUserAnswerFunc(request.pptReference))
            .map(_ => Results.Redirect(navigator.enterContactAddressNextPage))
      )
  }
}