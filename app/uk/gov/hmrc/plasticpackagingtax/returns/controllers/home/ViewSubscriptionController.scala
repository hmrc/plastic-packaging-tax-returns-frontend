/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.plasticpackagingtax.returns.controllers.home

import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.auth.core.InsufficientEnrolments
import uk.gov.hmrc.plasticpackagingtax.returns.connectors.SubscriptionConnector
import uk.gov.hmrc.plasticpackagingtax.returns.controllers.actions.AuthAction
import uk.gov.hmrc.plasticpackagingtax.returns.controllers.home.{routes => homeRoutes}
import uk.gov.hmrc.plasticpackagingtax.returns.models.request.JourneyAction
import uk.gov.hmrc.plasticpackagingtax.returns.views.html.home.view_subscription_page
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ViewSubscriptionController @Inject() (
  authenticate: AuthAction,
  journeyAction: JourneyAction,
  subscriptionConnector: SubscriptionConnector,
  mcc: MessagesControllerComponents,
  page: view_subscription_page
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  def displayPage(): Action[AnyContent] =
    (authenticate andThen journeyAction).async { implicit request =>
      request.enrolmentId match {
        case Some(id) =>
          subscriptionConnector.get(id)
            .map { subscription =>
              Ok(page(subscription))
            }

        case _ =>
          throw InsufficientEnrolments("Enrolment id not found on request")
      }
    }

  def submit(): Action[AnyContent] =
    (authenticate andThen journeyAction).async { _ =>
      Future.successful(Redirect(homeRoutes.HomeController.displayPage()))
    }

}
