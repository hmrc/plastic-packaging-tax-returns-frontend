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
import forms.returns.ManufacturedPlasticPackagingFormProvider
import models.returns.TaxReturnObligation
import models.{Mode, UserAnswers}
import navigation.ReturnsJourneyNavigator
import pages.returns.ManufacturedPlasticPackagingPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.returns.ManufacturedPlasticPackagingView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ManufacturedPlasticPackagingController @Inject() (
  override val messagesApi: MessagesApi,
  cacheConnector: CacheConnector,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: ManufacturedPlasticPackagingFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: ManufacturedPlasticPackagingView,
  returnsNavigator: ReturnsJourneyNavigator
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  private val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData) {
      implicit request =>
        val preparedForm = request.userAnswers.fill(ManufacturedPlasticPackagingPage, form)

        request.userAnswers.get[TaxReturnObligation](ObligationCacheable) match {
          case Some(obligation) => Ok(view(preparedForm, mode, obligation))
          case None => Redirect(controllers.routes.IndexController.onPageLoad)
        }
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async {
      implicit request =>
        val pptId: String = request.pptReference
        val userAnswers = request.userAnswers

        val obligation = userAnswers.get[TaxReturnObligation](ObligationCacheable).getOrElse(
          throw new IllegalStateException("Must have an obligation to Submit against")
        )

        form.bindFromRequest().fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, obligation))),
          newAnswer => updateAnswersAndGotoNextPage(mode, pptId, userAnswers, newAnswer)
        )
    }

  private def updateAnswersAndGotoNextPage(mode: Mode, pptId: String, previousAnswers: UserAnswers, newAnswer: Boolean) 
    (implicit hc: HeaderCarrier) = {
    
    previousAnswers.change(ManufacturedPlasticPackagingPage, newAnswer)
      .fold[Future[Boolean]](Future.successful(false))(updatedUserAnswers => cacheConnector.set(pptId, updatedUserAnswers).map(_ => true))
      .map(hasAnswerChanged => Redirect(returnsNavigator.manufacturedPlasticPackagingRoute(mode, hasAnswerChanged, newAnswer)))
  }
  
}
