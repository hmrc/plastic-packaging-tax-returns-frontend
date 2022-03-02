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

package uk.gov.hmrc.plasticpackagingtax.returns.controllers.returns

import play.api.data.Form
import play.api.data.Forms.{localDate, mapping, optional, text}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.plasticpackagingtax.returns.controllers.actions.{
  AuthAction,
  OpenObligationsRequest,
  ReturnAction
}
import uk.gov.hmrc.plasticpackagingtax.returns.models.request.{JourneyAction, JourneyRequest}
import uk.gov.hmrc.plasticpackagingtax.returns.views.html.returns.start_date_returns_page
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import java.time.LocalDate
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class StartDateReturnController @Inject() (
  authenticate: AuthAction,
  journeyAction: JourneyAction,
  returns: ReturnAction,
  mcc: MessagesControllerComponents,
  view: start_date_returns_page
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  def displayPage(): Action[AnyContent] = //TODO: which dates to populate title message?
    (authenticate andThen journeyAction andThen returns) {
      implicit request: OpenObligationsRequest[AnyContent] =>
        Ok(view(form(), request.nextObligationToPay))
    }

  def submit(): Action[AnyContent] =
    (authenticate andThen journeyAction andThen returns) {
      implicit request: OpenObligationsRequest[AnyContent] =>
        Ok(view(form().bindFromRequest(), request.nextObligationToPay))

    }

  def form(): Form[Boolean] =
    Form(
      mapping(
        "startPanReturns" -> optional(text)
          .verifying("err", _.nonEmpty)
          .transform[String](_.get, Some.apply)
          .transform[Boolean](_ == "yes", _.toString)
      )(identity)(Some.apply)
    )

}
