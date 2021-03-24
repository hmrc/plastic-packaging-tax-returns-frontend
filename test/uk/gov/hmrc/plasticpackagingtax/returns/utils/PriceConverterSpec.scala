/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.plasticpackagingtax.returns.utils

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class PriceConverterSpec extends AnyWordSpec with Matchers {

  "Price Converter " should {

    class TestImplClass extends PriceConverter
    val testImplClass = new TestImplClass()

    "convert from decimal Representation (String) to pences (Long) correctly " in {
      testImplClass.convertDecimalRepresentationToPences("0.10") mustBe 10L
      testImplClass.convertDecimalRepresentationToPences("0.0") mustBe 0L
      testImplClass.convertDecimalRepresentationToPences("0.29") mustBe 29L
      testImplClass.convertDecimalRepresentationToPences("5.7666666") mustBe 576L
      testImplClass.convertDecimalRepresentationToPences("99999999.99") mustBe 9999999999L
    }

    "convert from pences (Long) to decimal Representation (String) correctly " in {
      testImplClass.convertPencesToDecimalRepresentation(10L) mustBe "0.10"
      testImplClass.convertPencesToDecimalRepresentation(29L) mustBe "0.29"
      testImplClass.convertPencesToDecimalRepresentation(576L) mustBe "5.76"
      testImplClass.convertPencesToDecimalRepresentation(0L) mustBe "0.00"
      testImplClass.convertPencesToDecimalRepresentation(9999999999L) mustBe "99999999.99"
    }
  }
}
