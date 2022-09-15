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

import cacheables.ReturnObligationCacheable
import connectors.CacheConnector
import controllers.actions._
import controllers.helpers.TaxReturnHelper
import forms.returns.credits.WhatDoYouWantToDoFormProvider
import models.{Mode, UserAnswers}
import navigation.ReturnsJourneyNavigator
import pages.returns.credits.WhatDoYouWantToDoPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.returns.credits.WhatDoYouWantToDoView

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
  taxReturnHelper: TaxReturnHelper,
  returnsNavigator: ReturnsJourneyNavigator
) (implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData) {
      implicit request =>
        val obligation = request.userAnswers.get(ReturnObligationCacheable)
          .getOrElse(throw new IllegalStateException("Trying to submit return with no obligation"))

        val preparedForm = request.userAnswers.fill(WhatDoYouWantToDoPage, formProvider())
        Ok(view(preparedForm, obligation, mode))
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async {
      implicit request =>
        val obligation = request.userAnswers.get(ReturnObligationCacheable)
          .getOrElse(throw new IllegalStateException("Trying to submit return with no obligation"))

        formProvider().bindFromRequest().fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, obligation, mode))),
          newAnswer => updateAnswersAndGotoNextPage(mode, request.pptReference, request.userAnswers, newAnswer)
        )
    }

  private def updateAnswersAndGotoNextPage(mode: Mode, pptReference: String, previousAnswers: UserAnswers, newAnswer: Boolean) 
    (implicit hc: HeaderCarrier) = 
    previousAnswers
      .change(WhatDoYouWantToDoPage, newAnswer, cacheConnector.saveUserAnswerFunc(pptReference))
      .map(_ => Redirect(returnsNavigator.whatDoYouWantDoRoute(mode, newAnswer)))
  
}
