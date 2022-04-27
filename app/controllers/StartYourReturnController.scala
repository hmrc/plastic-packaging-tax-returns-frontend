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

import cacheables.ObligationCacheable
import connectors.CacheConnector
import controllers.ViewReturnSummaryController.AmendSelectedPeriodKey
import controllers.actions._
import controllers.helpers.TaxReturnHelper
import forms.StartYourReturnFormProvider

import javax.inject.Inject
import models.{Mode, UserAnswers}
import navigation.Navigator
import pages.StartYourReturnPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.StartYourReturnView

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

      val pptId: String = request.request.enrolmentId.getOrElse(
        throw new IllegalStateException("no enrolmentId, all users at this point should have one")
      )

      val preparedForm = request.userAnswers.getOrElse(UserAnswers(request.userId)).get(StartYourReturnPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      taxReturnHelper.nextObligation(pptId) flatMap { taxReturnObligation =>

        Future.fromTry(request.userAnswers.getOrElse(UserAnswers(request.request.user.identityData.internalId)).
          set(ObligationCacheable, taxReturnObligation)).map { ans => cacheConnector.set(pptId, ans) }

        Future.successful(Ok(view(preparedForm, mode, taxReturnObligation)))

      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData).async {
    implicit request =>

      val pptId: String = request.request.enrolmentId.getOrElse(
        throw new IllegalStateException("no enrolmentId, all users at this point should have one")
      )

      taxReturnHelper.nextObligation(pptId) flatMap { taxReturnObligation =>
        form.bindFromRequest().fold(
          formWithErrors =>
            Future.successful(BadRequest(view(formWithErrors, mode, taxReturnObligation))),

          value =>
            taxReturnHelper.nextObligation(pptId) flatMap { taxReturnObligation =>
              for {
                updatedAnswers <- Future.fromTry(
                  request.userAnswers.getOrElse(UserAnswers(request.request.user.identityData.internalId)).set(StartYourReturnPage, value)
                )
                _ <- cacheConnector.set(pptId, updatedAnswers)
              } yield Redirect(navigator.nextPage(StartYourReturnPage, mode, updatedAnswers))
            }
        )
      }
  }
}
