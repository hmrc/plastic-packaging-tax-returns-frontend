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

package services

import com.google.inject.Singleton
import play.api.libs.json.{Json, OFormat}
import play.api.mvc.Request
import uk.gov.hmrc.play.language.LanguageUtils

import javax.inject.Inject
import scala.collection.immutable.ListMap

case class FcoCountry(name: String)

object FcoCountry {
  implicit val format: OFormat[FcoCountry] = Json.format[FcoCountry]
}

@Singleton
class CountryService @Inject() (languageUtils: LanguageUtils) {

  private val countriesEn = parseCountriesResource("EN")
  private val countriesCy = parseCountriesResource("CY")

  def tryLookupCountryName(code: String) (implicit request: Request[_]): String =
    getAll.getOrElse(code, code)

  def getAll(implicit request: Request[_]): Map[String, String] = {
    languageUtils.getCurrentLang(request).code.take(2) match {
      case "cy" => countriesCy
      case _    => countriesEn
    }
  }

  private def parseCountriesResource(languageCode: String): Map[String, String] = {

    val stream = getClass.getResourceAsStream(s"/resources/countries$languageCode.json")
    val countryMap = Json.parse(stream).as[Map[String, FcoCountry]]
      .map { entry =>
        entry._1 -> entry._2.name
      }

    ListMap(countryMap.toSeq.sortBy(_._2): _*)
  }
}

