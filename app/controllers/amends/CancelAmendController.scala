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

package controllers.amends

import cacheables.{AmendSelectedPeriodKey, ObligationCacheable}
import controllers.actions._
import forms.amends.CancelAmendFormProvider

import javax.inject.Inject
import models.Mode
import navigation.Navigator
import pages.amends.CancelAmendPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import connectors.CacheConnector
import models.returns.TaxReturnObligation
import org.bouncycastle.asn1.ocsp.ResponseData
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.amends.CancelAmendView

import scala.concurrent.{ExecutionContext, Future}

class CancelAmendController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       cacheConnector: CacheConnector,
                                       identify: IdentifierAction,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       formProvider: CancelAmendFormProvider,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: CancelAmendView
                                     )() extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val obligation = request.userAnswers.get[TaxReturnObligation](ObligationCacheable).getOrElse(
        throw new IllegalStateException("Must have an obligation to Submit against")
      )

      Ok(view(form, obligation))
  }

  def onSubmit: Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      formProvider().bindFromRequest().get match {
        case true => Redirect(routes.CheckYourAnswersController.cancel())
        case _ => Redirect(routes.CheckYourAnswersController.onPageLoad())
      }

  }

}
