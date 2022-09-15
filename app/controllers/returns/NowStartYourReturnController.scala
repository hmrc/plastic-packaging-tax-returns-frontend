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

import cacheables.ReturnObligationCacheable
import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.TaxReturnsConnector
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.returns.TaxReturnObligation
import navigation.ReturnsJourneyNavigator
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.returns.NowStartYourReturnView

import scala.concurrent.ExecutionContext

class NowStartYourReturnController @Inject()(
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  returnsConnector: TaxReturnsConnector,
  sessionRepository: SessionRepository,
  val controllerComponents: MessagesControllerComponents,
  view: NowStartYourReturnView,
  appConfig: FrontendAppConfig,
  returnsNavigator: ReturnsJourneyNavigator
)(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {

  def onPageLoad(): Action[AnyContent] =
    (identify andThen getData andThen requireData) {
      implicit request =>
        request.userAnswers.get[TaxReturnObligation](ReturnObligationCacheable) match {
          case Some(obligation) => 
            val returnQuarter = obligation.toReturnQuarter
            val nextPage = returnsNavigator.nowStartYourReturnRoute
            Ok(view(returnQuarter, nextPage))
          case None => Redirect(controllers.routes.IndexController.onPageLoad)
        }
    }

}
