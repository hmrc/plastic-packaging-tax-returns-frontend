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

import connectors.CacheConnector
import controllers.ViewReturnSummaryController.AmendSelectedPeriodKey
import controllers.actions._
import controllers.helpers.TaxReturnHelper
import models.UserAnswers
import models.returns.ReturnDisplayApi
import pages.{AmendAreYouSurePage, AmendDirectExportPlasticPackagingPage, AmendHumanMedicinePlasticPackagingPage, AmendImportedPlasticPackagingPage, AmendManufacturedPlasticPackagingPage, AmendRecycledPlasticPackagingPage, Page}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsObject, JsPath, Json, OWrites, Writes}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import queries.{Gettable, Settable}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.ViewReturnSummaryViewModel
import views.html.ViewReturnSummaryView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ViewReturnSummaryController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  cacheConnector: CacheConnector,
  getData: DataRetrievalAction,
  val controllerComponents: MessagesControllerComponents,
  view: ViewReturnSummaryView,
  taxReturnHelper: TaxReturnHelper
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  def onPageLoad(periodKey: String): Action[AnyContent] =
    (identify andThen getData).async {
      implicit request =>
        val pptId: String = request.request.enrolmentId.getOrElse(throw new IllegalStateException("no enrolmentId, all users at this point should have one")) // TODO Make this not optional?
        val submittedReturnF: Future[ReturnDisplayApi] = taxReturnHelper.fetchTaxReturn(pptId, periodKey.toUpperCase())

        for {
          updatedAnswers <- Future.fromTry(
            request.userAnswers.getOrElse(UserAnswers(request.userId)).set(AmendSelectedPeriodKey, periodKey)
          )
          _ <- cacheConnector.set(pptId, updatedAnswers)
          submittedReturn <- submittedReturnF
        } yield {
          val returnPeriod = views.ViewUtils.displayReturnQuarter(submittedReturn)
          Ok(view(returnPeriod, ViewReturnSummaryViewModel(submittedReturn)))
        }
    }

}

object ViewReturnSummaryController {

//Cacheables?
  //todo move this to pages? or models? what is it :thinking:
  case object AmendSelectedPeriodKey extends Gettable[String] with Settable[String] {
    override def path: JsPath = JsPath \ toString

    override def toString: String = "amendSelectedPeriodKey"
  }

  case object AmendReturnPreviousReturn extends Gettable[ReturnDisplayApi] with Settable[ReturnDisplayApi] {
    override def path: JsPath = JsPath \ "amend"

    override def toString: String = "amendReturnPreviousReturn"

    val returnDisplayApiWrites: Writes[ReturnDisplayApi] = new Writes[ReturnDisplayApi] {
      def writes(display: ReturnDisplayApi): JsObject = Json.obj(
        AmendManufacturedPlasticPackagingPage.toString -> display.returnDetails.manufacturedWeight,
        AmendImportedPlasticPackagingPage.toString -> display.returnDetails.importedWeight,
        AmendHumanMedicinePlasticPackagingPage.toString -> display.returnDetails.humanMedicines,
        AmendDirectExportPlasticPackagingPage.toString -> display.returnDetails.directExports,
        AmendRecycledPlasticPackagingPage.toString -> display.returnDetails.recycledPlastic
      )
    }

  }
}
