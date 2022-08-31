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

import connectors.CacheConnector
import controllers.actions._
import forms.returns.WhatDoYouWantToDoFormProvider
import models.{Mode, UserAnswers}
import navigation.ReturnsJourneyNavigator
import pages.returns.WhatDoYouWantToDoPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.returns.WhatDoYouWantToDoView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class WhatDoYouWantToDoController @Inject() (
  override val messagesApi: MessagesApi,
  cacheConnector: CacheConnector,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: WhatDoYouWantToDoFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: WhatDoYouWantToDoView,
  returnsNavigator: ReturnsJourneyNavigator
) (implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData) {
      implicit request =>
        val preparedForm = request.userAnswers.fill(WhatDoYouWantToDoPage, formProvider())
        Ok(view(preparedForm, mode))
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async {
      implicit request =>
        formProvider().bindFromRequest().fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
          newAnswer => updateAnswersAndGotoNextPage(mode, request.pptReference, request.userAnswers, newAnswer)
        )
    }

  private def updateAnswersAndGotoNextPage(mode: Mode, pptReference: String, previousAnswers: UserAnswers, newAnswer: Boolean) 
    (implicit hc: HeaderCarrier) = 
    previousAnswers
      .change(WhatDoYouWantToDoPage, newAnswer, cacheConnector.saveUserAnswerFunc(pptReference))
      .map(_ => Redirect(returnsNavigator.whatDoYouWantDoRoute(mode, newAnswer)))
  
}
