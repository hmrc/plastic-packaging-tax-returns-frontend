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

package forms.returns.credits

import forms.mappings.Mappings
import forms.returns.credits.ClaimForWhichYearFormProvider.YearOption
import play.api.data.Form

import java.time.LocalDate


class ClaimForWhichYearFormProvider extends Mappings {

  def apply(options: Seq[YearOption]): Form[YearOption] =
    Form("value" ->
      text("claim-for-which-year.error.required")
        .verifying("claim-for-which-year.error.required", key => options.exists(_.key == key))
        .transform[YearOption](key => options.find(_.key == key).get, _.key)
    )
}


object ClaimForWhichYearFormProvider {
  final case class YearOption(from: LocalDate, to: LocalDate){
    def key: String = from.toString + "-" + to.toString //todo how do we want to represnt these?
  }
}