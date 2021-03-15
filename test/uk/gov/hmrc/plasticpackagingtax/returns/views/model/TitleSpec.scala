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

package uk.gov.hmrc.plasticpackagingtax.returns.views.model

import org.scalatest.matchers.must.Matchers
import uk.gov.hmrc.plasticpackagingtax.returns.base.unit.UnitViewSpec

class TitleSpec extends UnitViewSpec with Matchers {

  "Title" should {
    "display title without section header" in {

      Title(headingKey =
        "startPage.title"
      ).toString mustBe "Submit a return - Plastic Packaging Tax - GOV.UK"
    }

    "display title with section header" in {

      Title(headingKey = "startPage.title",
            sectionKey = "startPage.whatIsLiable.header"
      ).toString mustBe "Submit a return - What is liable? - Plastic Packaging Tax - GOV.UK"
    }
  }

}
