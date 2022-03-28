package generators

import org.scalacheck.Arbitrary
import pages._

trait PageGenerators {

  implicit lazy val arbitraryAmendRecycledPlasticPackagingPage: Arbitrary[AmendRecycledPlasticPackagingPage.type] =
    Arbitrary(AmendRecycledPlasticPackagingPage)

  implicit lazy val arbitraryAmendManufacturedPlasticPackagingPage: Arbitrary[AmendManufacturedPlasticPackagingPage.type] =
    Arbitrary(AmendManufacturedPlasticPackagingPage)

  implicit lazy val arbitraryAmendImportedPlasticPackagingPage: Arbitrary[AmendImportedPlasticPackagingPage.type] =
    Arbitrary(AmendImportedPlasticPackagingPage)

  implicit lazy val arbitraryAmendHumanMedicinePlasticPackagingPage: Arbitrary[AmendHumanMedicinePlasticPackagingPage.type] =
    Arbitrary(AmendHumanMedicinePlasticPackagingPage)

  implicit lazy val arbitraryAmendDirectExportPlasticPackagingPage: Arbitrary[AmendDirectExportPlasticPackagingPage.type] =
    Arbitrary(AmendDirectExportPlasticPackagingPage)
}
