/*
 * Copyright 2023 HM Revenue & Customs
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

package generators

import models.changeGroupLead.NewGroupLeadAddressDetails
import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbitrary

trait ModelGenerators {

  implicit lazy val arbitraryNewGroupLeadEnterContactAddress: Arbitrary[NewGroupLeadAddressDetails] =
    Arbitrary {
      for {
        addressLine1 <- arbitrary[String]
        addressLine2 <- arbitrary[String]
        addressLine3 <- arbitrary[Option[String]]
        addressLine4 <- arbitrary[Option[String]]
        postalCode <- arbitrary[Option[String]]
        countryCode <- arbitrary[String]

      } yield NewGroupLeadAddressDetails(addressLine1, addressLine2, addressLine3, addressLine4, postalCode, countryCode)
    }
}
