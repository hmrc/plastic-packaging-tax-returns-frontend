/*
 * Copyright 2023 HM Revenue & Customs
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

import connectors.CacheConnector
import controllers.actions._
import forms.returns.credits.RemoveCreditFormProvider
import navigation.Navigator
import pages.returns.credits.WhatDoYouWantToDoPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.JsPath
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.returns.credits.RemoveCreditView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RemoveCreditController @Inject()( //todo name better this will remove ALL years of credit
                                         override val messagesApi: MessagesApi,
                                         cacheConnector: CacheConnector,
                                         navigator: Navigator,
                                         identify: IdentifierAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         formProvider: RemoveCreditFormProvider,
                                         val controllerComponents: MessagesControllerComponents,
                                         view: RemoveCreditView
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      Ok(view(formProvider()))
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      formProvider().bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors))),
        remove =>
          (if (remove){
            request.userAnswers
              .removePath(JsPath \ "credit" )
              .setOrFail(WhatDoYouWantToDoPage, false)
              .save(cacheConnector.saveUserAnswerFunc(request.pptReference))
          } else Future.unit).map(_ =>
            Redirect(controllers.returns.routes.ReturnsCheckYourAnswersController.onPageLoad())
          )

      )
  }
}
