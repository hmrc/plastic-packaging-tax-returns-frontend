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

package models.changeGroupLead

import org.scalatestplus.play.PlaySpec

class NewGroupLeadAddressDetailsSpec extends PlaySpec {


  "NewGroupLeadAddressDetailsFormBuffer.toNewGroupLeadAddressDetails" must {
    "shuffle up townOrCity" when {
      "addressLine3 is not defined" in {
        val result = NewGroupLeadAddressDetailsFormBuffer(
          "addressLine1", Some("addressLine2"), None, "townOrCity", Some("postcode"), "countryCode"
        ).toNewGroupLeadAddressDetails

        result.addressLine1 mustBe "addressLine1"
        result.addressLine2 mustBe "addressLine2"
        result.addressLine3 mustBe Some("townOrCity")
        result.addressLine4 mustBe None
      }
      "addressLine2 and addressLine3 are not defined" in {
        val result = NewGroupLeadAddressDetailsFormBuffer(
          "addressLine1", None, None, "townOrCity", Some("postcode"), "countryCode"
        ).toNewGroupLeadAddressDetails

        result.addressLine1 mustBe "addressLine1"
        result.addressLine2 mustBe "townOrCity"
        result.addressLine3 mustBe None
        result.addressLine4 mustBe None
      }
    }
    "be cyclic" in {
      val sut = NewGroupLeadAddressDetailsFormBuffer(
        "addressLine1", Some("addressLine2"), None, "townOrCity", Some("postcode"), "countryCode"
      )
      sut.toNewGroupLeadAddressDetails.toBuffer mustBe sut
    }
  }

  "NewGroupLeadAddressDetails.toBuffer" must {

    "maintain the order" when {
      "there are 2 address lines" in {
        val result = NewGroupLeadAddressDetails(
          "addressLine1", "addressLine2", None, None, Some("postcode"), "countryCode"
        ).toBuffer

        result.addressLine1 mustBe "addressLine1"
        result.addressLine2 mustBe None
        result.addressLine3 mustBe None
        result.townOrCity mustBe "addressLine2"
      }
      "there are 3 address lines" in {
        val result = NewGroupLeadAddressDetails(
          "addressLine1", "addressLine2", Some("addressLine3"), None, Some("postcode"), "countryCode"
        ).toBuffer

        result.addressLine1 mustBe "addressLine1"
        result.addressLine2 mustBe Some("addressLine2")
        result.addressLine3 mustBe None
        result.townOrCity mustBe "addressLine3"
      }
      "there are 4 address lines" in {
        val result = NewGroupLeadAddressDetails(
          "addressLine1", "addressLine2", Some("addressLine3"), Some("addressLine4"), Some("postcode"), "countryCode"
        ).toBuffer

        result.addressLine1 mustBe "addressLine1"
        result.addressLine2 mustBe Some("addressLine2")
        result.addressLine3 mustBe Some("addressLine3")
        result.townOrCity mustBe "addressLine4"
      }
    }

    "be cyclic" in {
      val sut = NewGroupLeadAddressDetails(
        "addressLine1", "addressLine2", Some("addressLine3"), None, Some("postcode"), "countryCode"
      )
      sut.toBuffer.toNewGroupLeadAddressDetails mustBe sut
    }
  }

}
