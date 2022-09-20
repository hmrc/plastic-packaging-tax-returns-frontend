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

package controllers.amends

import cacheables.AmendObligationCacheable
import connectors.CacheConnector
import controllers.actions._
import forms.amends.AmendImportedPlasticPackagingFormProvider
import models.returns.TaxReturnObligation
import pages.amends.AmendImportedPlasticPackagingPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.amends.AmendImportedPlasticPackagingView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AmendImportedPlasticPackagingController @Inject() (
  override val messagesApi: MessagesApi,
  cacheConnector: CacheConnector,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  form: AmendImportedPlasticPackagingFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: AmendImportedPlasticPackagingView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  def onPageLoad: Action[AnyContent] =
    (identify andThen getData andThen requireData) {
      implicit request =>
        val preparedForm = request.userAnswers.get(AmendImportedPlasticPackagingPage) match {
          case None => form()
          case Some(value) => form().fill(value)
        }
        if (request.userAnswers.get[TaxReturnObligation](AmendObligationCacheable).isDefined) {
          Ok(view(preparedForm))
        } else {
          Redirect(routes.SubmittedReturnsController.onPageLoad())
        }

    }

  def onSubmit: Action[AnyContent] =
    (identify andThen getData andThen requireData).async {
      implicit request =>
        val pptId: String = request.pptReference

        form().bindFromRequest().fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(
                request.userAnswers.set(AmendImportedPlasticPackagingPage, value)
              )
              _ <- cacheConnector.set(pptId, updatedAnswers)
            } yield Redirect(
              controllers.amends.routes.CheckYourAnswersController.onPageLoad)
        )
    }

}
