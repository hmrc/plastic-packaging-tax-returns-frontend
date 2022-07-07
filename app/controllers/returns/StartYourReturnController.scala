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
import models.Mode
import navigation.Navigator
import pages.returns.StartYourReturnPage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.returns.StartYourReturnView
import config.{Features, FrontendAppConfig}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class StartYourReturnController @Inject()(
                                           override val messagesApi: MessagesApi,
                                           appConfig: FrontendAppConfig,
                                           cacheConnector: CacheConnector,
                                           navigator: Navigator,
                                           identify: IdentifierAction,
                                           getData: DataRetrievalAction,
                                           formProvider: StartYourReturnFormProvider,
                                           val controllerComponents: MessagesControllerComponents,
                                           view: StartYourReturnView,
                                           taxReturnHelper: TaxReturnHelper
                                         )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData).async {
    implicit request =>
      if (!appConfig.isFeatureEnabled(Features.returnsEnabled)){
        logger.info("Returns disabled. Redirecting to account homepage.")
        Future.successful(Redirect(controllers.routes.IndexController.onPageLoad))
      } else {
        val pptId: String = request.pptReference

        val preparedForm = request.userAnswers.get(StartYourReturnPage) match {
          case None => form
          case Some(value) => form.fill(value)
        }

        taxReturnHelper.nextOpenObligationAndIfFirst(pptId).flatMap {
          case Some((taxReturnObligation, isFirst)) =>
            for {
              ans <- Future.fromTry(request.userAnswers.set(ObligationCacheable, taxReturnObligation))
              _ <- cacheConnector.set(pptId, ans)
            } yield
              Ok(view(preparedForm, mode, taxReturnObligation, isFirst))
          case None =>
            logger.info("Trying to start return with no obligation. Redirecting to account homepage.")
            Future.successful(Redirect(controllers.routes.IndexController.onPageLoad))
        }
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData).async {
    implicit request =>

      val pptId: String = request.pptReference

      form.bindFromRequest().fold(
        formWithErrors =>
          taxReturnHelper.nextOpenObligationAndIfFirst(pptId).map {
            case Some((taxReturnObligation, isFirst)) =>
              BadRequest(view(formWithErrors, mode, taxReturnObligation, isFirst))
            case None =>
              logger.info("Trying to start return with no obligation. Redirecting to account homepage.")
              Redirect(controllers.routes.IndexController.onPageLoad)
          },
        value =>
          for {
            updatedAnswers <- Future.fromTry(
              request.userAnswers.set(StartYourReturnPage, value)
            )
            _ <- cacheConnector.set(pptId, updatedAnswers)
          } yield Redirect(navigator.nextPage(StartYourReturnPage, mode, updatedAnswers))
      )
  }
}
