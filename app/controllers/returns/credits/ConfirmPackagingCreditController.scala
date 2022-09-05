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

package controllers.returns.credits

import connectors.ExportCreditsConnector
import controllers.actions._
import models.Mode
import models.Mode.NormalMode
import navigation.ReturnsJourneyNavigator
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.returns.credits.ConfirmPackagingCreditView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ConfirmPackagingCreditController @Inject()(
                                                  override val messagesApi: MessagesApi,
                                                  exportCreditsConnector: ExportCreditsConnector,
                                                  identify: IdentifierAction,
                                                  getData: DataRetrievalAction,
                                                  requireData: DataRequiredAction,
                                                  val controllerComponents: MessagesControllerComponents,
                                                  view: ConfirmPackagingCreditView,
  returnsNavigator: ReturnsJourneyNavigator
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = {

    (identify andThen getData andThen requireData).async {
      implicit request =>
        val buttonLink = returnsNavigator.confirmCreditRoute(NormalMode)
        Future.successful(Ok(view(Some("200"), buttonLink)))
    }
  }
}
