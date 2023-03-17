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

package config

import com.typesafe.config.{Config, ConfigFactory}
import org.scalatestplus.play.PlaySpec
import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.time.LocalDateTime

class FrontendAppConfigSpec extends PlaySpec {

  private val validAppConfig: Config =
    ConfigFactory.parseString("""
    |override-system-date-time = 2023-03-31T23:59:59
    """.stripMargin)

  private val validServicesConfiguration = Configuration(validAppConfig)

  private def servicesConfig(conf: Configuration) = new ServicesConfig(conf)
  private def appConfig(conf: Configuration) = new FrontendAppConfig(conf, servicesConfig(conf))

  "configuration" should {

    "return override-system-date-time" in {
      val configService: FrontendAppConfig = appConfig(validServicesConfiguration)

      configService.overrideSystemDateTime mustBe
        LocalDateTime.of(2023, 3, 31, 23, 59, 59)
    }
  }

}
