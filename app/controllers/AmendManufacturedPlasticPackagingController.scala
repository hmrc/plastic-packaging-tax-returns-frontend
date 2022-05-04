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

import cacheables.ReturnDisplayApiCacheable
import connectors.CacheConnector
import controllers.actions._
import forms.AmendManufacturedPlasticPackagingFormProvider
import models.Mode
import models.returns.ReturnDisplayApi
import navigation.Navigator
import pages.AmendManufacturedPlasticPackagingPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.AmendManufacturedPlasticPackagingView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AmendManufacturedPlasticPackagingController @Inject() (
  override val messagesApi: MessagesApi,
  cacheConnector: CacheConnector,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: AmendManufacturedPlasticPackagingFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: AmendManufacturedPlasticPackagingView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData) {
      implicit request =>
        val preparedForm =
          request.userAnswers.get(AmendManufacturedPlasticPackagingPage) match {
            case None        => form
            case Some(value) => form.fill(value)
          }

        request.userAnswers.get[ReturnDisplayApi](ReturnDisplayApiCacheable) match {
          case Some(displayApi) => Ok(view(preparedForm, mode, displayApi))
          case None             => Redirect(routes.SubmittedReturnsController.onPageLoad())
        }

    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async {
      implicit request =>
        val pptId: String = request.request.enrolmentId.getOrElse(throw new IllegalStateException("no enrolmentId, all users at this point should have one"))

        val submittedReturn = request.userAnswers.get[ReturnDisplayApi](ReturnDisplayApiCacheable).getOrElse(
          throw new IllegalStateException("Must have a tax return against which to amend")
        )

        form.bindFromRequest().fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, submittedReturn))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(
                request.userAnswers.set(AmendManufacturedPlasticPackagingPage, value)
              )
              _ <- cacheConnector.set(pptId, updatedAnswers)
            } yield Redirect(
              navigator.nextPage(AmendManufacturedPlasticPackagingPage, mode, updatedAnswers)
            )
        )
    }

}
