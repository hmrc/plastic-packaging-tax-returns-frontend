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

import connectors.CacheConnector
import controllers.actions._
import controllers.helpers.InjectableNonExportedAmountHelper
import forms.returns.NonExportedRecycledPlasticPackagingWeightFormProvider
import models.Mode
import navigation.Navigator
import pages.returns.NonExportedRecycledPlasticPackagingWeightPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.returns.NonExportedRecycledPlasticPackagingWeightView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class NonExportedRecycledPlasticPackagingWeightController @Inject()(
                                                                     override val messagesApi: MessagesApi,
                                                                     cacheConnector: CacheConnector,
                                                                     navigator: Navigator,
                                                                     identify: IdentifierAction,
                                                                     getData: DataRetrievalAction,
                                                                     requireData: DataRequiredAction,
                                                                     form: NonExportedRecycledPlasticPackagingWeightFormProvider,
                                                                     val controllerComponents: MessagesControllerComponents,
                                                                     view: NonExportedRecycledPlasticPackagingWeightView,
                                                                     nonExportedAmountHelper: InjectableNonExportedAmountHelper
                                                                 )(implicit ec: ExecutionContext)
  extends FrontendBaseController with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData) {
      implicit request =>

        val preparedForm = request.userAnswers.get(NonExportedRecycledPlasticPackagingWeightPage) match {
          case None => form()
          case Some(value) => form().fill(value)
        }

        nonExportedAmountHelper.getAmountAndDirectlyExportedAnswer(request.userAnswers).fold(
          Redirect(controllers.routes.IndexController.onPageLoad)) {
          case (amount, directlyExported, _) =>
            Ok(view(preparedForm, mode, amount, directlyExported))
        }
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async {
      implicit request =>
        val pptId: String = request.pptReference
        form().bindFromRequest().fold(
          formWithErrors =>
            Future.successful(nonExportedAmountHelper.getAmountAndDirectlyExportedAnswer(request.userAnswers)
              .fold(Redirect(controllers.routes.IndexController.onPageLoad)) {
                case (amount, directlyExported, _) => BadRequest(view(formWithErrors, mode, amount, directlyExported))
              }
            ),
          value =>
            for {
              updatedAnswers <- Future.fromTry(
                request.userAnswers.set(NonExportedRecycledPlasticPackagingWeightPage, value)
              )
              _ <- cacheConnector.set(pptId, updatedAnswers)
            } yield Redirect(
              navigator.nextPage(NonExportedRecycledPlasticPackagingWeightPage, mode, updatedAnswers)
            )
        )
    }

}
