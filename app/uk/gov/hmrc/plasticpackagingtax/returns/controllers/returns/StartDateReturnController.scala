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
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.plasticpackagingtax.returns.controllers.actions.AuthAction
import uk.gov.hmrc.plasticpackagingtax.returns.forms.returns.StartDateReturnForm
import uk.gov.hmrc.plasticpackagingtax.returns.models.request.{JourneyAction, JourneyRequest}
import uk.gov.hmrc.plasticpackagingtax.returns.views.html.returns.start_date_returns_page
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class StartDateReturnController @Inject() (
  authenticate: AuthAction,
  journeyAction: JourneyAction,
  mcc: MessagesControllerComponents,
  view: start_date_returns_page
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  def displayPage(): Action[AnyContent] = //TODO: which dates to populate title message?
    (authenticate andThen journeyAction) {
      implicit request: JourneyRequest[AnyContent] =>
        val obligation = request.taxReturn.obligation.getOrElse(
          throw new IllegalStateException(s"No Obligation for return id:${request.enrolmentId}")
        )
        Ok(view(StartDateReturnForm.form(), obligation))
    }

  def submit(): Action[AnyContent] =
    (authenticate andThen journeyAction) {
      implicit request: JourneyRequest[AnyContent] =>
        val obligation = request.taxReturn.obligation.getOrElse(
          throw new IllegalStateException(s"No Obligation for return id:${request.enrolmentId}")
        )
        StartDateReturnForm.form().bindFromRequest()
          .fold((formWithErrors: Form[Boolean]) => BadRequest(view(formWithErrors, obligation)),
                (startReturn: Boolean) =>
                  if (startReturn)
                    Redirect(routes.ManufacturedPlasticController.contribution())
                  else
                    Redirect(
                      routes.StartDateReturnController.displayPage()
                    ) //todo: implement redirect to /no-other-periods BUILD THIS eventually
          )
    }

}
