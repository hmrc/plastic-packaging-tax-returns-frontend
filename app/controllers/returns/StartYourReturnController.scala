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

package controllers.returns

import audit.Auditor
import cacheables.ReturnObligationCacheable
import connectors.CacheConnector
import controllers.actions._
import controllers.helpers.TaxReturnHelper
import forms.returns.StartYourReturnFormProvider
import models.requests.OptionalDataRequest
import navigation.ReturnsJourneyNavigator
import pages.returns.StartYourReturnPage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.JsPath
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.returns.StartYourReturnView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class StartYourReturnController @Inject()(
  override val messagesApi: MessagesApi,
  cacheConnector: CacheConnector,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  form: StartYourReturnFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: StartYourReturnView,
  taxReturnHelper: TaxReturnHelper,
  auditor: Auditor,
  returnsNavigator: ReturnsJourneyNavigator
)(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData).async {
    implicit request =>
      val pptReference: String = request.pptReference
      val userAnswers = request.userAnswers

      val preparedForm = userAnswers.get(StartYourReturnPage) match {
        case None => form()
        case Some(value) => form().fill(value)
      }

      taxReturnHelper.nextOpenObligationAndIfFirst(pptReference).flatMap {
        case Some((taxReturnObligation, isFirst)) =>
          userAnswers
            .setOrFail(ReturnObligationCacheable, taxReturnObligation)
            .setOrFail(JsPath \ "isFirstReturn", isFirst)
            .save(cacheConnector.saveUserAnswerFunc(pptReference))
            .map(_ => Ok(view(preparedForm, taxReturnObligation, isFirst)))
        case None =>
          logger.info("Trying to start return with no obligation. Redirecting to account homepage.")
          Future.successful(Redirect(controllers.routes.IndexController.onPageLoad))
      }
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData).async {
    implicit request =>

      val userAnswers = request.userAnswers
      val pptReference = request.pptReference
      val obligation = userAnswers.getOrFail(ReturnObligationCacheable)
      val isFirstReturn = userAnswers.getOrFail[Boolean](JsPath \ "isFirstReturn")

      form().bindFromRequest().fold(
        formWithErrors =>
          Future.successful(
            BadRequest(view(formWithErrors, obligation, isFirstReturn)))
        ,
        formValue =>
          userAnswers
            .setOrFail(StartYourReturnPage, formValue)
            .save(cacheConnector.saveUserAnswerFunc(pptReference))
            .map(_ => act(formValue, isFirstReturn))
      )
  }

  private def act(formValue: Boolean, isFirstReturn: Boolean) (implicit request: OptionalDataRequest[_]) = {
    if (formValue) {
      auditor.returnStarted(request.request.user.identityData.internalId, request.pptReference)
    }
    Redirect(returnsNavigator.startYourReturnRoute(formValue, isFirstReturn))
  }

}
