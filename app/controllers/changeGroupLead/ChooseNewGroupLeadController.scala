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
import connectors.CacheConnector
import controllers.actions._
import forms.mappings.Mappings
import models.Mode
import models.requests.DataRequest
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.JsPath
import play.api.mvc._
import play.twirl.api.Html
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.changeGroupLead.ChooseNewGroupLeadView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}


class ChooseNewGroupLeadPage extends Mappings {
  def answerPath(): JsPath = JsPath \ "chooseNewGroupLead"
  def createForm(): Form[Boolean] = Form("value" -> boolean("Pampa na tenye pensa!"))
}


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
  page: ChooseNewGroupLeadPage, 
  cacheConnector: CacheConnector,
  featureGuard: FeatureGuard,
)
  (implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = journeyAction.async {
    implicit request =>
      featureGuard.check()
      val blankForm = page.createForm()
      val answerPath = page.answerPath()
      val preparedForm = request.userAnswers.fill(answerPath, blankForm)
      val value = view(preparedForm, controllers.changeGroupLead.routes.ChooseNewGroupLeadController.onSubmit())
      Future.successful(Ok(value))
  }

  private def onError(mode: Mode) (form: Form[Boolean]) (implicit dataRequest: DataRequest[AnyContent]): Html = {
    view(form, controllers.changeGroupLead.routes.ChooseNewGroupLeadController.onSubmit())
  }

  private def onSuccess(mode: Mode) (newValue: Boolean) (implicit dataRequest: DataRequest[AnyContent]): Future[Call] = {
    dataRequest.userAnswers
      .setOrFail(page.answerPath(), newValue)
      .save(cacheConnector.saveUserAnswerFunc(dataRequest.pptReference))
      .map(_ => controllers.routes.IndexController.onPageLoad)
  }

  def onSubmit(mode: Mode): Action[AnyContent] = journeyAction.async {
    implicit request =>
      featureGuard.check()
      val blankForm = page.createForm()
      val completedForm = blankForm.bindFromRequest()
      completedForm.fold(
        x => Future.successful(Results.BadRequest(onError(mode)(x))),
        x => onSuccess(mode)(x).map(Results.Redirect)
      )
  }

}
