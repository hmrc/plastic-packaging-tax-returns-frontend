package controllers.helpers

import models.UserAnswers
import models.returns._
import pages._

class TaxReturnHelper {

  def getTaxReturn(pptReference: String, userAnswers: UserAnswers): TaxReturn =
    TaxReturn(id = pptReference,
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
