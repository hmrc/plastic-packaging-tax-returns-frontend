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

import cacheables.ReturnObligationCacheable
import connectors.CacheConnector
import controllers.actions._
import forms.returns.ImportedPlasticPackagingWeightFormProvider
import models.{Mode, ReturnsUserAnswers}
import models.returns.TaxReturnObligation
import navigation.Navigator
import pages.returns.ImportedPlasticPackagingWeightPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.returns.ImportedPlasticPackagingWeightView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ImportedPlasticPackagingWeightController @Inject() (
  override val messagesApi: MessagesApi,
  cacheConnector: CacheConnector,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  form: ImportedPlasticPackagingWeightFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: ImportedPlasticPackagingWeightView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData) {
      implicit request =>
        ReturnsUserAnswers.checkObligationSync(request) { obligation =>
          val preparedForm = request.userAnswers.fill(ImportedPlasticPackagingWeightPage, form())
          Ok(view(preparedForm, mode, obligation))
        }
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async {
      implicit request =>
        val pptId: String = request.pptReference
        val obligation = request.userAnswers.getOrFail(ReturnObligationCacheable)

        form().bindFromRequest().fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, obligation))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(
                request.userAnswers.set(ImportedPlasticPackagingWeightPage, value)
              )
              _ <- cacheConnector.set(pptId, updatedAnswers)
            } yield Redirect(
              navigator.nextPage(ImportedPlasticPackagingWeightPage, mode, updatedAnswers)
            )
        )
    }

}
