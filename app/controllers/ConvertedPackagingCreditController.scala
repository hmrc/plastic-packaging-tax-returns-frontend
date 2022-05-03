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

package controllers

import cacheables.ObligationCacheable
import connectors.CacheConnector
import controllers.actions._
import forms.ConvertedPackagingCreditFormProvider

import javax.inject.Inject
import models.Mode
import models.returns.TaxReturnObligation
import navigation.Navigator
import pages.ConvertedPackagingCreditPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ConvertedPackagingCreditView

import scala.concurrent.{ExecutionContext, Future}

class ConvertedPackagingCreditController @Inject() (
  override val messagesApi: MessagesApi,
  cacheConnector: CacheConnector,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: ConvertedPackagingCreditFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: ConvertedPackagingCreditView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async {
      implicit request =>
        val preparedForm = request.userAnswers.get(ConvertedPackagingCreditPage) match {
          case None        => form
          case Some(value) => form.fill(value)
        }

        request.userAnswers.get[TaxReturnObligation](ObligationCacheable) match {
          case Some(obligation) => Future.successful(Ok(view(preparedForm, mode, obligation)))
          case None             => Future.successful(Redirect(routes.IndexController.onPageLoad))
        }    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async {
      implicit request =>
        val pptId: String = request.request.enrolmentId.getOrElse(throw new IllegalStateException("no enrolmentId, all users at this point should have one"))
        val obligation = request.userAnswers.get[TaxReturnObligation](ObligationCacheable).getOrElse(
          throw new IllegalStateException("Must have an obligation to Submit against")
        )
        form.bindFromRequest().fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, obligation))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(
                request.userAnswers.set(ConvertedPackagingCreditPage, value)
              )
              _ <- cacheConnector.set(pptId, updatedAnswers)
            } yield Redirect(navigator.nextPage(ConvertedPackagingCreditPage, mode, updatedAnswers))
        )
    }

}
