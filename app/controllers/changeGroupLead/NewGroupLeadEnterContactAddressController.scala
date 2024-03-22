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

package controllers.changeGroupLead

import connectors.CacheConnector
import controllers.actions._
import forms.changeGroupLead.NewGroupLeadEnterContactAddressFormProvider
import models.Mode
import models.requests.DataRequest.headerCarrier
import navigation.ChangeGroupLeadNavigator
import pages.changeGroupLead.{ChooseNewGroupLeadPage, NewGroupLeadEnterContactAddressPage}
import play.api.data.FormBinding.Implicits.formBinding
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Results}
import services.CountryService
import views.html.changeGroupLead.NewGroupLeadEnterContactAddressView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class NewGroupLeadEnterContactAddressController @Inject() (
  override val messagesApi: MessagesApi,
  cacheConnector: CacheConnector,
  navigator: ChangeGroupLeadNavigator,
  journeyAction: JourneyAction,
  formProvider: NewGroupLeadEnterContactAddressFormProvider,
  countryService: CountryService,
  val controllerComponents: MessagesControllerComponents,
  view: NewGroupLeadEnterContactAddressView
)(implicit ec: ExecutionContext)
    extends I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = journeyAction.async { implicit request =>
    val selectedMember = request.userAnswers.getOrFail(ChooseNewGroupLeadPage)
    val preparedForm   = request.userAnswers.fill(NewGroupLeadEnterContactAddressPage, formProvider.apply())
    Future.successful(Results.Ok(view(preparedForm, countryService.getAll, selectedMember.organisationName, mode)))
  }

  def onSubmit(implicit mode: Mode): Action[AnyContent] = journeyAction.async { implicit request =>
    val selectedMember = request.userAnswers.getOrFail(ChooseNewGroupLeadPage)

    formProvider().bindFromRequest().fold(
      formWithErrors =>
        Future.successful(
          Results.BadRequest(view(formWithErrors, countryService.getAll, selectedMember.organisationName, mode))
        ),
      contactAddress =>
        request.userAnswers
          .setOrFail(NewGroupLeadEnterContactAddressPage, contactAddress)
          .save(cacheConnector.saveUserAnswerFunc(request.pptReference))
          .map(_ => Results.Redirect(navigator.enterContactAddress))
    )
  }
}
