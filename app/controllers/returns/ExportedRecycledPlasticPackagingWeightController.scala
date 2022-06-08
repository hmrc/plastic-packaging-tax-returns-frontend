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
import forms.returns.ExportedRecycledPlasticPackagingWeightFormProvider
import models.Mode
import models.requests.DataRequest
import navigation.Navigator
import pages.returns.{ExportedPlasticPackagingWeightPage, ExportedRecycledPlasticPackagingWeightPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
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

      val preparedForm = request.userAnswers.get(ExportedRecycledPlasticPackagingWeightPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode, extractExportedAmount))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode, extractExportedAmount))),
        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(ExportedRecycledPlasticPackagingWeightPage, value))
            _              <- cacheConnector.set(request.pptReference, updatedAnswers)
          } yield Redirect(navigator.nextPage(ExportedRecycledPlasticPackagingWeightPage, mode, updatedAnswers))
      )
  }

  private def extractExportedAmount() (implicit request: DataRequest[AnyContent]): Long = {
    request.userAnswers.get(ExportedPlasticPackagingWeightPage)
      .getOrElse(throw new IllegalStateException("Invalid exported recycled weight for Plastic Packaging"))
  }
}
