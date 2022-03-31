package controllers.helpers

import models.UserAnswers
import models.returns._
import pages._

class TaxReturnHelper {

  def getTaxReturn(pptReference: String, userAnswers: UserAnswers): TaxReturn ={

    val manufacturedPlasticWeight: Option[Int] = userAnswers.get(AmendManufacturedPlasticPackagingPage)
    val importedPlasticWeight: Option[Int] = userAnswers.get(AmendImportedPlasticPackagingPage)
    val humanMedicinesPlasticWeight: Option[Int] = userAnswers.get(AmendHumanMedicinePlasticPackagingPage)
    val exportedPlasticWeight: Option[Int] = userAnswers.get(AmendDirectExportPlasticPackagingPage)
    val recycledPlasticWeight:Option[Int] = userAnswers.get(AmendRecycledPlasticPackagingPage)

    val mpw: Option[ManufacturedPlasticWeight] = manufacturedPlasticWeight match {
      case Some(value) => Some(ManufacturedPlasticWeight(value))
      case _ => None
    }

    val ipw: Option[ImportedPlasticWeight] = importedPlasticWeight match {
      case Some(value) => Some(ImportedPlasticWeight(value))
      case None => None
    }

    val hmpw: Option[HumanMedicinesPlasticWeight] = humanMedicinesPlasticWeight match {
      case Some(value) => Some(HumanMedicinesPlasticWeight(value))
      case None => None
    }

    val epw: Option[ExportedPlasticWeight] = exportedPlasticWeight match {
      case Some(value) => Some(ExportedPlasticWeight(value))
      case None => None
    }

    val rpw: Option[RecycledPlasticWeight] = recycledPlasticWeight match {
      case Some(value) => Some(RecycledPlasticWeight(value))
      case _ => None
    }

    TaxReturn(id = pptReference,
      manufacturedPlasticWeight = mpw,
      importedPlasticWeight = ipw,
      humanMedicinesPlasticWeight = hmpw,
      exportedPlasticWeight = epw,
      recycledPlasticWeight = rpw
    )

  }

}
