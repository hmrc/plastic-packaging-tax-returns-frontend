package generators

import org.scalacheck.Arbitrary
import pages._

trait PageGenerators {

  implicit lazy val arbitraryConfirmAmendReturnPage: Arbitrary[ConfirmAmendReturnPage.type] =
    Arbitrary(ConfirmAmendReturnPage)
}
