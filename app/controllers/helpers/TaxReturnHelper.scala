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

package controllers.helpers

import connectors.{ServiceError, TaxReturnsConnector}
import models.UserAnswers
import models.returns.ReturnType.ReturnType
import models.returns._
import pages._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TaxReturnHelper @Inject() (
  override val messagesApi: MessagesApi,
  val controllerComponents: MessagesControllerComponents,
  returnsConnector: TaxReturnsConnector
)(implicit ec: ExecutionContext)
    extends FrontendBaseController with I18nSupport {

  // TODO - where do we get this obligation from? A GET on the return?
  private val defaultObligation: TaxReturnObligation = TaxReturnObligation(
    fromDate = LocalDate.parse("2022-04-01"),
    toDate = LocalDate.parse("2022-06-30"),
    dueDate = LocalDate.parse("2022-09-30"),
    periodKey = "22AC"
  )

  //todo why is this here?
  def fetchTaxReturn(userId: String, periodKey: String)(implicit
    hc: HeaderCarrier
  ): Future[ReturnDisplayApi] = {
    val future: Future[Either[ServiceError, ReturnDisplayApi]] =
      returnsConnector.get(userId, periodKey)
    future.map {
      case Right(taxReturn) => taxReturn
      case Left(error)      => throw error
    }
  }

  def getAmendment(pptReference: String, userAnswers: UserAnswers): TaxReturn =
    taxReturnBase(pptReference, userAnswers, ReturnType.AMEND)

  def getTaxReturn(pptReference: String, userAnswers: UserAnswers): TaxReturn = {
    val returnBase = taxReturnBase(pptReference, userAnswers, ReturnType.NEW)
    // TODO add additional data items (return) to the base object
    returnBase
  }

  private def taxReturnBase(pptReference: String, userAnswers: UserAnswers, returnType: ReturnType): TaxReturn = {
    TaxReturn(id = pptReference,
      returnType = Some(returnType),
      obligation = Some(defaultObligation),
      manufacturedPlasticWeight =
        userAnswers.get(AmendManufacturedPlasticPackagingPage).map(
          value => ManufacturedPlasticWeight(value)
        ),
      importedPlasticWeight =
        userAnswers.get(AmendImportedPlasticPackagingPage).map(
          value => ImportedPlasticWeight(value)
        ),
      humanMedicinesPlasticWeight =
        userAnswers.get(AmendHumanMedicinePlasticPackagingPage).map(
          value => HumanMedicinesPlasticWeight(value)
        ),
      exportedPlasticWeight =
        userAnswers.get(AmendDirectExportPlasticPackagingPage).map(
          value => ExportedPlasticWeight(value)
        ),
      recycledPlasticWeight = userAnswers.get(AmendRecycledPlasticPackagingPage).map(
        value => RecycledPlasticWeight(value)
      )
    )
  }

}
