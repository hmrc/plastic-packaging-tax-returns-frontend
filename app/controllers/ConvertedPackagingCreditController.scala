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

import cacheables.ObligationCacheable
import connectors.{CacheConnector, ExportCreditsConnector}
import controllers.actions._
import forms.ConvertedPackagingCreditFormProvider
import models.Mode
import models.requests.DataRequest
import models.returns.TaxReturnObligation
import navigation.Navigator
import pages.ConvertedPackagingCreditPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.PrintBigDecimal
import views.html.ConvertedPackagingCreditView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ConvertedPackagingCreditController @Inject() (
  override val messagesApi: MessagesApi,
  cacheConnector: CacheConnector,
  exportCreditsConnector: ExportCreditsConnector,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: ConvertedPackagingCreditFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: ConvertedPackagingCreditView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  private val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = {
    (identify andThen getData andThen requireData).async {
      implicit request =>
        val preparedForm = request.userAnswers.get(ConvertedPackagingCreditPage) match {
          case None => form
          case Some(value) => form.fill(value)
        }

        val futureCreditBalanceAvailable: Future[Option[String]] = exportCreditBalanceAvailable(request)
        futureCreditBalanceAvailable.map {
          possibleCreditBalanceAvailable => Ok(view(preparedForm, mode, possibleCreditBalanceAvailable))
        }

    }
  }

  private def exportCreditBalanceAvailable(request: DataRequest[AnyContent])(implicit hc: HeaderCarrier): Future[Option[String]] = {

    val obligation = request.userAnswers.get[TaxReturnObligation](ObligationCacheable).getOrElse(
      throw new IllegalStateException("Obligation not found in user-answers")
    )
    val futureExportCredits = exportCreditsConnector.get(request.request.pptReference,
      obligation.fromDate.minusYears(2),
      obligation.fromDate.minusDays(1)
    )
    futureExportCredits.map {
      case Right(balance) => Some(balance.totalExportCreditAvailable.asPounds)
      case Left(_) => None
    }
  }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async {
      implicit request =>
        val pptId: String = request.request.enrolmentId.getOrElse(throw new IllegalStateException("no enrolmentId, all users at this point should have one"))

        val futureCreditBalanceAvailable: Future[Option[String]] = exportCreditBalanceAvailable(request)

        form.bindFromRequest().fold(
          formWithErrors => {
            futureCreditBalanceAvailable.map { possibleCreditBalanceAvailable =>
              BadRequest(view(formWithErrors, mode, possibleCreditBalanceAvailable))
            }
          },
          value =>
            for {
              updatedAnswers <- Future.fromTry(
                request.userAnswers.set(ConvertedPackagingCreditPage, value)
              )
              _ <- cacheConnector.set(pptId, updatedAnswers)
            } yield Redirect(navigator.nextPage(ConvertedPackagingCreditPage, mode, updatedAnswers))
        )
    }

  }
