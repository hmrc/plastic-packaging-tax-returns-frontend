package generators

import models._
import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbitrary
import pages._
import play.api.libs.json.{JsValue, Json}

trait UserAnswersEntryGenerators extends PageGenerators with ModelGenerators {

  implicit lazy val arbitraryAmendRecycledPlasticPackagingUserAnswersEntry: Arbitrary[(AmendRecycledPlasticPackagingPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[AmendRecycledPlasticPackagingPage.type]
        value <- arbitrary[Int].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryAmendManufacturedPlasticPackagingUserAnswersEntry: Arbitrary[(AmendManufacturedPlasticPackagingPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[AmendManufacturedPlasticPackagingPage.type]
        value <- arbitrary[Int].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryAmendImportedPlasticPackagingUserAnswersEntry: Arbitrary[(AmendImportedPlasticPackagingPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[AmendImportedPlasticPackagingPage.type]
        value <- arbitrary[Int].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryAmendHumanMedicinePlasticPackagingUserAnswersEntry: Arbitrary[(AmendHumanMedicinePlasticPackagingPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[AmendHumanMedicinePlasticPackagingPage.type]
        value <- arbitrary[Int].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryAmendDirectExportPlasticPackagingUserAnswersEntry: Arbitrary[(AmendDirectExportPlasticPackagingPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[AmendDirectExportPlasticPackagingPage.type]
        value <- arbitrary[Int].map(Json.toJson(_))
      } yield (page, value)
    }
}
