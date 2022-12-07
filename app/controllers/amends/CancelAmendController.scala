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

package controllers.amends

import cacheables.AmendObligationCacheable
import connectors.CacheConnector
import controllers.actions._
import forms.amends.CancelAmendFormProvider
import models.requests.DataRequest
import models.requests.DataRequest.headerCarrier
import models.returns.TaxReturnObligation
import play.api.data.FormBinding.Implicits.formBinding
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Results.{BadRequest, Redirect}
import play.api.mvc._
import views.html.amends.{AmendCancelledView, CancelAmendView}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CancelAmendController @Inject()
(override val messagesApi: MessagesApi,
 cacheConnector: CacheConnector,
 journeyAction: JourneyAction,
 formProvider: CancelAmendFormProvider,
 val controllerComponents: MessagesControllerComponents,
 cancelAmendView: CancelAmendView,
 amendCancelledView: AmendCancelledView
)(implicit ec: ExecutionContext) extends I18nSupport {

  def onPageLoad: Action[AnyContent] = journeyAction {
    implicit request =>
      request.userAnswers.get[TaxReturnObligation](AmendObligationCacheable)
        .fold(Results.Ok(amendCancelledView()))(
          o => Results.Ok(cancelAmendView(formProvider(), o))
        )

  }

  def onSubmit: Action[AnyContent] = journeyAction.async {
    implicit request =>

      val obligation = request.userAnswers.get[TaxReturnObligation](AmendObligationCacheable).getOrElse(
        throw new IllegalStateException("Must have an obligation to Submit against")
      )

      formProvider().bindFromRequest().fold(
        formWithErrors => Future.successful(BadRequest(cancelAmendView(formWithErrors, obligation))),
        value => if (value) {
          cancel(request)
        } else {
          Future.successful(Redirect(routes.CheckYourAnswersController.onPageLoad()))
        }
      )
  }

  def cancel(implicit request: DataRequest[_]): Future[Result] = {
    request.userAnswers
      .reset
      .save(cacheConnector.saveUserAnswerFunc(request.request.pptReference))
      .map { _ => Results.Ok(amendCancelledView()) }
  }
}
