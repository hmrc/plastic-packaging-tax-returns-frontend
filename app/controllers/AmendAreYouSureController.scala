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

import controllers.ViewReturnSummaryController.{AmendReturnPreviousReturn, AmendSelectedPeriodKey}
import controllers.actions._
import controllers.helpers.TaxReturnHelper
import forms.AmendAreYouSureFormProvider
import models.Mode
import navigation.Navigator
import pages.{AmendAreYouSurePage, AmendDirectExportPlasticPackagingPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.AmendAreYouSureView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AmendAreYouSureController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: AmendAreYouSureFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: AmendAreYouSureView,
  taxReturnHelper: TaxReturnHelper
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  private val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async {
      implicit request =>
        val pptId: String = request.request.enrolmentId.getOrElse(throw new IllegalStateException("no enrolmentId, all users at this point should have one"))
        val userAnswers = request.userAnswers

        val preparedForm = userAnswers.fill(AmendAreYouSurePage, form)

        val periodKey = userAnswers.get(AmendSelectedPeriodKey)
        periodKey.fold {
          Future.successful(Redirect("/go-and-select-a-year")) //todo carls page
        }{ period =>
          taxReturnHelper.fetchTaxReturn(pptId, period).map{ submittedReturn =>
            Ok(view(preparedForm, mode, submittedReturn))
          }
        }
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async {
      implicit request =>
        val pptId: String = request.request.enrolmentId.getOrElse(throw new IllegalStateException("no enrolmentId, all users at this point should have one"))
        val userAnswers = request.userAnswers
        val periodKey = userAnswers.get(AmendSelectedPeriodKey).getOrElse(throw new IllegalStateException("no period key to amend with"))

        taxReturnHelper.fetchTaxReturn(pptId, periodKey).flatMap{submittedReturn =>
          form.bindFromRequest().fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, submittedReturn))),
            amend => {
                for {
                updatedAnswers <- Future.fromTry(
                  userAnswers.set(AmendAreYouSurePage, amend)
                    .flatMap(_.set(AmendReturnPreviousReturn, submittedReturn)(AmendReturnPreviousReturn.returnDisplayApiWrites))
                )
                _ <- sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(AmendAreYouSurePage, mode, updatedAnswers))
            }
          )
      }
    }

}
