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
import forms.changeGroupLead.MainContactJobTitleFormProvider
import models.Mode
import navigation.ChangeGroupLeadNavigator
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Results}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.changeGroupLead.MainContactJobTitleView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class MainContactJobTitleController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       cacheConnector: CacheConnector,
                                       navigator: ChangeGroupLeadNavigator,
                                       journeyAction: JourneyAction,
                                       featureGuard: FeatureGuard,
                                       formProvider: MainContactJobTitleFormProvider,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: MainContactJobTitleView
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {


  def onPageLoad(mode: Mode): Action[AnyContent] = journeyAction {
    implicit request =>
      featureGuard.check()
      val preparedForm = formProvider()
      Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = journeyAction.async {
    implicit request =>
      Future.successful(Results.Redirect(navigator.mainContactJobTitle(mode)))
  }
}