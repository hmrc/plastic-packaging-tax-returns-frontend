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
import forms.changeGroupLead.MainContactJobTitleFormProvider
import models.Mode
import models.requests.DataRequest._
import navigation.ChangeGroupLeadNavigator
import pages.changeGroupLead.{MainContactJobTitlePage, MainContactNamePage}
import play.api.data.FormBinding.Implicits.formBinding
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Results.{BadRequest, Ok, Redirect}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import views.html.changeGroupLead.MainContactJobTitleView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class MainContactJobTitleController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       cacheConnector: CacheConnector,
                                       navigator: ChangeGroupLeadNavigator,
                                       journeyAction: JourneyAction,
                                       featureGuard: FeatureGuard,
                                       form: MainContactJobTitleFormProvider,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: MainContactJobTitleView
                                     )(implicit ec: ExecutionContext) extends I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = journeyAction {
    implicit request =>
      featureGuard.check()
      val contactName = request.userAnswers.getOrFail(MainContactNamePage)
      val preparedForm = request.userAnswers.fill(MainContactJobTitlePage, form())

      Ok(view(preparedForm, contactName, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = journeyAction.async {
    implicit request =>
      featureGuard.check()
      val contactName = request.userAnswers.getOrFail(MainContactNamePage)

      form().bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, contactName, mode))),

        jobTitle =>
          request.userAnswers
            .setOrFail(MainContactJobTitlePage, jobTitle)
            .save(cacheConnector.saveUserAnswerFunc(request.pptReference))
            .map(_ => Redirect(navigator.mainContactJobTitle(mode)))
      )
  }
}