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
import models.returns.TaxReturnObligation
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.SubmittedReturnsView

import java.time.LocalDate
import javax.inject.Inject

class SubmittedReturnsController @Inject()(
                                            override val messagesApi: MessagesApi,
                                            identify: IdentifierAction,
                                            val controllerComponents: MessagesControllerComponents,
                                            view: SubmittedReturnsView
                                          ) extends FrontendBaseController with I18nSupport {


  def onPageLoad: Action[AnyContent] = identify {

    implicit request =>

      val obligations0: Option[Seq[TaxReturnObligation]] = {
        Some(Seq.empty)
      }

      val obligations1: Option[Seq[TaxReturnObligation]] = {
        Some(Seq(TaxReturnObligation(LocalDate.now(),
          LocalDate.now(),
          LocalDate.now(),
          "PK1")))
      }
      val obligations2: Option[Seq[TaxReturnObligation]] = {
        Some(Seq(TaxReturnObligation(LocalDate.now(),
          LocalDate.now().plusMonths(3),
          LocalDate.now().plusMonths(3),
          "PK1"),
          TaxReturnObligation(LocalDate.now().plusMonths(3),
            LocalDate.now().plusMonths(6),
            LocalDate.now().plusMonths(6),
            "PK2")
        ))
      }

      Ok(view(obligations2))
  }
}