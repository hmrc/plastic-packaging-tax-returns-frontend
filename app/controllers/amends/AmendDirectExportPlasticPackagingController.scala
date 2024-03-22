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

package controllers.amends

import cacheables.AmendObligationCacheable
import connectors.CacheConnector
import controllers.actions._
import forms.amends.AmendDirectExportPlasticPackagingFormProvider
import models.requests.DataRequest.headerCarrier
import models.returns.TaxReturnObligation
import pages.amends.AmendDirectExportPlasticPackagingPage
import play.api.data.FormBinding.Implicits.formBinding
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Results.{BadRequest, Ok, Redirect}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import views.html.amends.AmendDirectExportPlasticPackagingView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AmendDirectExportPlasticPackagingController @Inject() (
  override val messagesApi: MessagesApi,
  cacheConnector: CacheConnector,
  journeyAction: JourneyAction,
  form: AmendDirectExportPlasticPackagingFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: AmendDirectExportPlasticPackagingView
)(implicit ec: ExecutionContext)
    extends I18nSupport {

  def onPageLoad: Action[AnyContent] =
    journeyAction { implicit request =>
      val preparedForm = request.userAnswers.fill(AmendDirectExportPlasticPackagingPage, form())

      if (request.userAnswers.get[TaxReturnObligation](AmendObligationCacheable).isDefined) {
        Ok(view(preparedForm))
      } else {
        Redirect(routes.SubmittedReturnsController.onPageLoad())
      }
    }

  def onSubmit: Action[AnyContent] =
    journeyAction.async { implicit request =>
      form().bindFromRequest().fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors))),
        value =>
          request.userAnswers
            .setOrFail(AmendDirectExportPlasticPackagingPage, value)
            .save(cacheConnector.saveUserAnswerFunc(request.pptReference))
            .map(_ => Redirect(controllers.amends.routes.AmendExportedByAnotherBusinessController.onPageLoad))
      )
    }

}
