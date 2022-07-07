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

import connectors.{ObligationsConnector, ServiceError, TaxReturnsConnector}
import models.UserAnswers
import models.returns.ReturnType.{AMEND, NEW, ReturnType}
import models.returns._
import pages.amends._
import pages.returns._
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
//TODO: Multi services in here, break it down now, funk soul brother
class TaxReturnHelper @Inject()(
                                 returnsConnector: TaxReturnsConnector,
                                 obligationsConnector: ObligationsConnector
                               )(implicit ec: ExecutionContext){

  def nextOpenObligationAndIfFirst(pptId: String)(implicit hc: HeaderCarrier): Future[Option[(TaxReturnObligation, Boolean)]] = {
    obligationsConnector.getOpen(pptId) flatMap  { obligations =>

      obligations.nextObligationToReturn.fold[Future[Option[(TaxReturnObligation, Boolean)]]](
        Future.successful(None)
      )( nextObligation =>
        obligationsConnector.getFulfilled(pptId).map{
          fulfilledObs =>
            Some((nextObligation, fulfilledObs.isEmpty))
        }
      )
    }
  }

  def getObligation(pptId: String, periodKey: String)(implicit hc: HeaderCarrier): Future[Seq[TaxReturnObligation]] = {
    obligationsConnector.getFulfilled(pptId) map { obligations =>
      obligations.filter(o => o.periodKey == periodKey)
    }
  }

  def fetchTaxReturn(userId: String, periodKey: String)(implicit hc: HeaderCarrier): Future[ReturnDisplayApi] = {
    val future: Future[Either[ServiceError, ReturnDisplayApi]] = returnsConnector.get(userId, periodKey)
    future.map {
      case Right(taxReturn) => taxReturn
      case Left(error) => throw error
    }
  }

  def getTaxReturn(pptReference: String, userAnswers: UserAnswers, periodKey: String, returnType: ReturnType): TaxReturn = {
    returnType match {
      case NEW =>
        TaxReturn(id = pptReference,
          returnType = Some(returnType),
          periodKey = periodKey,
          manufacturedPlastic = userAnswers.get(ManufacturedPlasticPackagingPage),
          manufacturedPlasticWeight =
            userAnswers.get(ManufacturedPlasticPackagingWeightPage).map(
              value => ManufacturedPlasticWeight(value)
            ),
          importedPlastic = userAnswers.get(ImportedPlasticPackagingPage),
          importedPlasticWeight =
            userAnswers.get(ImportedPlasticPackagingWeightPage).map(
              value => ImportedPlasticWeight(value)
            ),
          exportedPlastic = userAnswers.get(DirectlyExportedComponentsPage),
          exportedPlasticWeight =
            userAnswers.get(ExportedPlasticPackagingWeightPage).map(
              value => ExportedPlasticWeight(value)
            ),
          humanMedicinesPlastic = userAnswers.get(NonExportedHumanMedicinesPlasticPackagingPage),
          humanMedicinesPlasticWeight =
            userAnswers.get(NonExportedHumanMedicinesPlasticPackagingWeightPage).map(
              value => HumanMedicinesPlasticWeight(value)
            ),
          recycledPlastic = userAnswers.get(NonExportedRecycledPlasticPackagingPage),
          recycledPlasticWeight = userAnswers.get(NonExportedRecycledPlasticPackagingWeightPage).map(
            value => RecycledPlasticWeight(value)),
          convertedPackagingCredit =
            userAnswers.get(ConvertedPackagingCreditPage).map(
              value => ConvertedPackagingCredit(value)
            ).orElse(Some(ConvertedPackagingCredit(0))) //todo only for before credit is used, revisit

        )
      //todo: credits in amends
      case AMEND =>
        TaxReturn(id = pptReference,
          returnType = Some(returnType),
          periodKey = periodKey,
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
          ),
          convertedPackagingCredit = Some(ConvertedPackagingCredit(0))
          //TODO: Amends journey is missing credits
        )
    }
  }

}
