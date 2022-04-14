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

import controllers.actions._
import controllers.helpers.TaxReturnHelper
import models.returns.ReturnDisplayApi
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.ViewReturnSummaryViewModel
import views.html.ViewReturnSummaryView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ViewReturnSummaryController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  val controllerComponents: MessagesControllerComponents,
  view: ViewReturnSummaryView,
  taxReturnHelper: TaxReturnHelper
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  def onPageLoad(periodKey: String): Action[AnyContent] =
    identify.async {
      implicit request =>
        val enrolmentId = request.enrolmentId.getOrElse(throw new IllegalArgumentException("Make this not optional?")) // TODO
        val submittedReturn: Future[ReturnDisplayApi] =
          taxReturnHelper.fetchTaxReturn(enrolmentId, periodKey.toUpperCase())
        submittedReturn.map {
          val returnPeriod = "April to June 2022" // TODO
          subRet => Ok(view(returnPeriod, ViewReturnSummaryViewModel(subRet)))
        }
    }

}
