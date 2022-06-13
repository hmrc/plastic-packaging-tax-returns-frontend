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

package viewmodels

import org.scalatestplus.play.PlaySpec

class ViewmodelsSpec extends PlaySpec {

  "asPounds" must {
    "add the pound sign" in {
      BigDecimal(1).asPounds mustBe "£1.00"
    }
    "add the comma" in {
      BigDecimal(1000).asPounds mustBe "£1,000.00"
    }
    "work with non zero pence" in {
      BigDecimal(1.99).asPounds mustBe "£1.99"
    }
    "work with 0 pounds" in {
      BigDecimal(0).asPounds mustBe "£0.00"
    }
    "work with 0 pounds and pence" in {
      BigDecimal(0.50).asPounds mustBe "£0.50"
    }
    "work with 1dp pence" in {
      BigDecimal(1.5).asPounds mustBe "£1.50"
    }
    "round up fractional pence" in {
      BigDecimal(1.995).asPounds mustBe "£2.00"
    }
    "round down fractional pence" in {
      BigDecimal(1.994).asPounds mustBe "£1.99"
    }
  }

  "asKgs" must {
    "add the 'kg' sign" in {
      BigDecimal(1).asKgs mustBe "1kg"
    }
    "add the comma" in {
      BigDecimal(1000).asKgs mustBe "1,000kg"
    }
    "add the comma twice" in {
      BigDecimal(1000000).asKgs mustBe "1,000,000kg"
    }
    "remove decimals" in {
      BigDecimal(1.01).asKgs mustBe "1kg"
    }
    "round up decimal" in {
      BigDecimal(1.5).asKgs mustBe "2kg"
    }
    "round down decimal" in {
      BigDecimal(1.4).asKgs mustBe "1kg"
    }
    "work with 0" in {
      BigDecimal(0).asKgs mustBe "0kg"
    }
  }

}
