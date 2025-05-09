/*
 * Copyright 2025 HM Revenue & Customs
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

import connectors.{AvailableCreditYearsConnector, CacheConnector}
import controllers.actions.JourneyAction
import forms.returns.credits.ClaimForWhichYearFormProvider
import models.Mode
import models.requests.DataRequest
import models.requests.DataRequest.headerCarrier
import models.returns.CreditRangeOption
import navigation.ReturnsJourneyNavigator
import pages.returns.credits.{ClaimForWhichYearPage, ConvertedCreditsPage, ExportedCreditsPage}
import play.api.data.FormBinding.Implicits.formBinding
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsObject, JsPath}
import play.api.mvc.Results.{Ok, Redirect}
import play.api.mvc._
import uk.gov.hmrc.http.HeaderCarrier
import views.html.returns.credits.ClaimForWhichYearView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ClaimForWhichYearController @Inject() (
  override val messagesApi: MessagesApi,
  journeyAction: JourneyAction,
  val controllerComponents: MessagesControllerComponents,
  view: ClaimForWhichYearView,
  formProvider: ClaimForWhichYearFormProvider,
  navigator: ReturnsJourneyNavigator,
  cacheConnector: CacheConnector,
  availableCreditYearsConnector: AvailableCreditYearsConnector
)(implicit ec: ExecutionContext)
    extends I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = {
    journeyAction.async { implicit request =>
      availableRemainingOptions.flatMap {
        case Nil =>
          Future.successful(Redirect(controllers.returns.credits.routes.CreditsClaimedListController.onPageLoad(mode)))
        case Seq(onlyOption) => selectDateRange(onlyOption, mode)
        case options =>
          val preparedValue: Option[CreditRangeOption] = options.find { option =>
            val maybeClaimYear = request.userAnswers.get(ClaimForWhichYearPage(option.key))
            maybeClaimYear.exists(_.createCreditRangeOption().key == option.key)
          }

          val preparedForm = preparedValue match {
            case None        => formProvider(options)
            case Some(value) => formProvider(options).fill(value)
          }
          Future.successful(Ok(view(preparedForm, options, mode)))
      }

    }
  }

  def onSubmit(mode: Mode): Action[AnyContent] =
    journeyAction.async { implicit request =>
      availableRemainingOptions.flatMap { options =>
        formProvider(options)
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(Results.BadRequest(view(formWithErrors, options, mode))),
            selectedRange => selectDateRange(selectedRange, mode)
          )
      }
    }

  private def selectDateRange(selectedRange: CreditRangeOption, mode: Mode)(implicit
    request: DataRequest[AnyContent]
  ): Future[Result] =
    request.userAnswers
      .setOrFail(JsPath \ "credit" \ selectedRange.key \ "toDate", selectedRange.to)
      .setOrFail(JsPath \ "credit" \ selectedRange.key \ "fromDate", selectedRange.from)
      .save(cacheConnector.saveUserAnswerFunc(request.pptReference)).map(_ =>
        Results.Redirect(navigator.claimForWhichYear(selectedRange, mode))
      )

  private def availableRemainingOptions(implicit
    request: DataRequest[AnyContent],
    headerCarrier: HeaderCarrier
  ): Future[Seq[CreditRangeOption]] =
    availableCreditYearsConnector.get(request.pptReference).map { availableYears =>
      val alreadyUsedYears =
        request.userAnswers.get[Map[String, JsObject]](JsPath \ "credit").getOrElse(Map.empty).keySet

      val completedJ = alreadyUsedYears.foldLeft(Seq.empty[String]) { (x, key) =>
        val flag = request.userAnswers.get(ExportedCreditsPage(key)).isDefined && request.userAnswers.get(
          ConvertedCreditsPage(key)
        ).isDefined
        if (flag) x :+ key else x
      }

      availableYears.filterNot(y => completedJ.contains(y.key))

    }

}
