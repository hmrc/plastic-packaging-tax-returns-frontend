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

import connectors.CacheConnector
import controllers.actions._
import forms.ExportedRecycledPlasticPackagingWeightFormProvider
import models.Mode
import models.requests.DataRequest
import navigation.Navigator
import pages.ExportedRecycledPlasticPackagingWeightPage
import pages.returns.ExportedPlasticPackagingWeightPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.returns.ExportedRecycledPlasticPackagingWeightView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ExportedRecycledPlasticPackagingWeightController @Inject()(
                                        override val messagesApi: MessagesApi,
                                        cacheConnector: CacheConnector,
                                        navigator: Navigator,
                                        identify: IdentifierAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        formProvider: ExportedRecycledPlasticPackagingWeightFormProvider,
                                        val controllerComponents: MessagesControllerComponents,
                                        view: ExportedRecycledPlasticPackagingWeightView
                                      )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val preparedForm = request.userAnswers.fill(ExportedRecycledPlasticPackagingWeightPage, form)

      exportedAmount.fold[Result](identity, amount => Ok(view(preparedForm, mode, amount)))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors => {
          Future.successful(exportedAmount.fold[Result](identity, amount => BadRequest(view(formWithErrors, mode, amount))))
        },
        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(ExportedRecycledPlasticPackagingWeightPage, value))
            _              <- cacheConnector.set(request.pptReference, updatedAnswers)
          } yield Redirect(navigator.nextPage(ExportedRecycledPlasticPackagingWeightPage, mode, updatedAnswers))
      )
  }

  private def exportedAmount(implicit request: DataRequest[_]): Either[Result, Long] =
    request.userAnswers.get(ExportedPlasticPackagingWeightPage)
      .fold[Either[Result, Long]](Left(Redirect(controllers.routes.IndexController.onPageLoad)))(Right(_))
}
