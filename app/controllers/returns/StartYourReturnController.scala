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
import controllers.helpers.TaxReturnHelper
import forms.returns.StartYourReturnFormProvider
import models.{Mode, UserAnswers}
import navigation.Navigator
import pages.returns.StartYourReturnPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.returns.StartYourReturnView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class StartYourReturnController @Inject()(
                                           override val messagesApi: MessagesApi,
                                           cacheConnector: CacheConnector,
                                           navigator: Navigator,
                                           identify: IdentifierAction,
                                           getData: DataRetrievalAction,
                                           formProvider: StartYourReturnFormProvider,
                                           val controllerComponents: MessagesControllerComponents,
                                           view: StartYourReturnView,
                                           taxReturnHelper: TaxReturnHelper
                                         )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData).async {
    implicit request =>

      val pptId: String = request.pptReference

      val preparedForm = request.userAnswers.getOrElse(UserAnswers(request.userId)).get(StartYourReturnPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      taxReturnHelper.nextOpenObligationAndIfFirst(pptId) flatMap { case (taxReturnObligation, isFirst) =>

        Future.fromTry(request.userAnswers.getOrElse(UserAnswers(request.request.user.identityData.internalId)).
          set(ObligationCacheable, taxReturnObligation)).map { ans => cacheConnector.set(pptId, ans) }

        Future.successful(Ok(view(preparedForm, mode, taxReturnObligation, isFirst)))

      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData).async {
    implicit request =>

      val pptId: String = request.pptReference

      form.bindFromRequest().fold(
        formWithErrors =>
          taxReturnHelper.nextOpenObligationAndIfFirst(pptId) map { case (taxReturnObligation, isFirst) =>
            BadRequest(view(formWithErrors, mode, taxReturnObligation, isFirst))
          },
        value =>
          for {
            updatedAnswers <- Future.fromTry(
              request.userAnswers.getOrElse(UserAnswers(request.request.user.identityData.internalId)).set(StartYourReturnPage, value)
            )
            _ <- cacheConnector.set(pptId, updatedAnswers)
          } yield Redirect(navigator.nextPage(StartYourReturnPage, mode, updatedAnswers))
      )

  }
}
