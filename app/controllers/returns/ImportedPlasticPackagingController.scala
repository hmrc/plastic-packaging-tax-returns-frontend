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
import forms.returns.ImportedPlasticPackagingFormProvider
import models.returns.TaxReturnObligation
import models.{Mode, ReturnsUserAnswers, UserAnswers}
import navigation.ReturnsJourneyNavigator
import pages.returns.ImportedPlasticPackagingPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.returns.ImportedPlasticPackagingView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ImportedPlasticPackagingController @Inject()
(
  override val messagesApi: MessagesApi,
  cacheConnector: CacheConnector,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: ImportedPlasticPackagingFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: ImportedPlasticPackagingView,
  returnsNavigator: ReturnsJourneyNavigator
)(implicit ec: ExecutionContext)
  extends FrontendBaseController with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData) {
      implicit request =>
        ReturnsUserAnswers.checkObligationSync(request) { obligation =>
          val preparedForm = request.userAnswers.fill(ImportedPlasticPackagingPage, formProvider())
          Ok(view(preparedForm, mode, obligation))
        }
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async {
      implicit request =>
        val pptId: String = request.pptReference
        val userAnswers = request.userAnswers

        val obligation = request.userAnswers.get[TaxReturnObligation](ReturnObligationCacheable).getOrElse(
          throw new IllegalStateException("Must have an obligation to Submit against")
        )

        formProvider().bindFromRequest().fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, obligation))),
          newAnswer => updateAnswerAndGotoNextPage(mode, pptId, userAnswers, newAnswer)
        )
    }

  private def updateAnswerAndGotoNextPage(mode: Mode, pptReference: String, previousAnswers: UserAnswers, newAnswer: Boolean)
                                         (implicit hc: HeaderCarrier): Future[Result] = {

    previousAnswers
      .change(ImportedPlasticPackagingPage, newAnswer, cacheConnector.saveUserAnswerFunc(pptReference))
      .map(hasAnswerChanged => Redirect(returnsNavigator.importedPlasticPackaging(mode, hasAnswerChanged, newAnswer)))
  }

}
