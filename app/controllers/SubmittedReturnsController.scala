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

import connectors.ObligationsConnector
import controllers.actions._
import models.returns.TaxReturnObligation
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.SubmittedReturnsView

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class SubmittedReturnsController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  val controllerComponents: MessagesControllerComponents,
  view: SubmittedReturnsView,
  obligationsConnector: ObligationsConnector
) extends FrontendBaseController with I18nSupport {

  def onPageLoad: Action[AnyContent] =
    identify.async {

      implicit request =>
        val pptReference =
          request.enrolmentId.getOrElse(throw new IllegalStateException("no enrolmentId"))

        obligationsConnector.getFulfilled(pptReference).flatMap { taxReturnObligations =>
          Future.successful(Ok(view(taxReturnObligations.obligations)))
        }

//        val obligations1: Option[Seq[TaxReturnObligation]] =
//          Some(Seq(TaxReturnObligation(LocalDate.now(), LocalDate.now(), LocalDate.now(), "00xx")))
//        val obligations2: Option[Seq[TaxReturnObligation]] =
//          Some(
//            Seq(
//              TaxReturnObligation(
//                LocalDate.now(),
//                LocalDate.now().plusMonths(3),
//                LocalDate.now().plusMonths(3),
//                "00xx"
//              ),
//              TaxReturnObligation(
//                LocalDate.now().plusMonths(3),
//                LocalDate.now().plusMonths(6),
//                LocalDate.now().plusMonths(6),
//                "00xx"
//              )
//            )
//          )
//        obligations1.get.head.periodKey
//        Ok(view(obligations2))
    }
}
