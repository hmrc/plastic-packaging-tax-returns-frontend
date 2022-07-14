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

import controllers.actions._
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.returns.ReturnConfirmationView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class ReturnConfirmationController @Inject()(
                                              val controllerComponents: MessagesControllerComponents,
                                              identify: IdentifierAction,
                                              sessionRepository: SessionRepository,
                                              view: ReturnConfirmationView
                                            )(
  implicit ec: ExecutionContext
) extends FrontendBaseController with I18nSupport {

  def onPageLoad: Action[AnyContent] =
    identify.async { implicit request =>
      sessionRepository.get(request.cacheKey).map{
        entry =>
          val chargeRef = entry.flatMap(_.data)
          Ok(view(chargeRef))
      }
    }
}
