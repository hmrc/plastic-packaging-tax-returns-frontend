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

package controllers.returns

import cacheables.ObligationCacheable
import connectors.CacheConnector
import controllers.actions._
import forms.returns.ImportedPlasticPackagingFormProvider
import models.requests.DataRequest
import models.{Mode, UserAnswers}
import models.returns.TaxReturnObligation
import navigation.Navigator
import pages.returns.{ImportedPlasticPackagingPage, ManufacturedPlasticPackagingPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.returns.ImportedPlasticPackagingView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ImportedPlasticPackagingController @Inject() (
  override val messagesApi: MessagesApi,
  cacheConnector: CacheConnector,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: ImportedPlasticPackagingFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: ImportedPlasticPackagingView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async {
      implicit request =>
        val preparedForm = request.userAnswers.get(ImportedPlasticPackagingPage) match {
          case None => form
          case Some(value) => form.fill(value)
        }
        request.userAnswers.get[TaxReturnObligation](ObligationCacheable) match {
          case Some(obligation) => Future.successful(Ok(view(preparedForm, mode, obligation)))
          case None => Future.successful(Redirect(controllers.routes.IndexController.onPageLoad))
        }
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData) {
      implicit request =>
        val pptId: String = request.pptReference
        val userAnswers = request.userAnswers

        val obligation = request.userAnswers.get[TaxReturnObligation](ObligationCacheable).getOrElse(
          throw new IllegalStateException("Must have an obligation to Submit against")
        )

        form.bindFromRequest().fold(
          formWithErrors => BadRequest(view(formWithErrors, mode, obligation)),
          newAnswer => updateAnswerAndGotoNextPage(mode, pptId, userAnswers, newAnswer)
        )
    }

  private def updateAnswerAndGotoNextPage(mode: Mode, pptId: String, previousAnswers: UserAnswers, newAnswer: Boolean)
    (implicit hc: HeaderCarrier) = {
    val maybeAnswers = previousAnswers.change(ImportedPlasticPackagingPage, newAnswer)
        maybeAnswers match {
          case None                 => Redirect(navigator.nextPage(ImportedPlasticPackagingPage, mode, previousAnswers, false))
          case Some(updatedAnswers) =>
            cacheConnector.set(pptId, updatedAnswers)
            Redirect(navigator.nextPage(ImportedPlasticPackagingPage, mode, updatedAnswers, true))
        }
  }
}
