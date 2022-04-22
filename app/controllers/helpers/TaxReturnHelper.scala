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
import models.returns.ReturnType.{AMEND, NEW, ReturnType}
import models.returns._
import pages._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TaxReturnHelper @Inject()(
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
      case Left(error) => throw error
    }
  }



   def getTaxReturn(pptReference: String, userAnswers: UserAnswers, returnType: ReturnType): TaxReturn = {
    returnType match {
      case NEW =>
        TaxReturn(id = pptReference,
          returnType = Some(returnType),
          obligation = Some(defaultObligation),
                    manufacturedPlastic = userAnswers.get(ManufacturedPlasticPackagingPage),
          manufacturedPlasticWeight =
            userAnswers.get(ManufacturedPlasticPackagingWeightPage).map(
              value => ManufacturedPlasticWeight(value)
            ),
              importedPlastic = userAnswers.get (ImportedPlasticPackagingPage),
          importedPlasticWeight =
            userAnswers.get(ImportedPlasticPackagingWeightPage).map(
              value => ImportedPlasticWeight(value)
            ),
          humanMedicinesPlasticWeight =
            userAnswers.get(HumanMedicinesPlasticPackagingWeightPage).map(
              value => HumanMedicinesPlasticWeight(value)
            ),
          exportedPlasticWeight =
            userAnswers.get(ExportedPlasticPackagingWeightPage).map(
              value => ExportedPlasticWeight(value)
            ),
          convertedPackagingCredit =
            userAnswers.get(ConvertedPackagingCreditPage).map(
              value => ConvertedPackagingCredit(value)
            ),
          recycledPlasticWeight = userAnswers.get(RecycledPlasticPackagingWeightPage).map(
            value => RecycledPlasticWeight(value)
          )
        )


      case AMEND =>
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

}
