/*
 * Copyright 2025 HM Revenue & Customs
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
import org.mockito.MockitoSugar
import org.mockito.scalatest.ResetMocksAfterEachTest
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

class FrontendAppConfigSpec extends PlaySpec with MockitoSugar with BeforeAndAfterEach with ResetMocksAfterEachTest {

  private val validAppConfig: Config =
    ConfigFactory.parseString(s"""
    |override-system-date-time = "whatEver"
    """.stripMargin)

  private val validServicesConfiguration = Configuration(validAppConfig)

  private def servicesConfig(conf: Configuration) = new ServicesConfig(conf)
  private def appConfig(conf: Configuration)      = new FrontendAppConfig(conf, servicesConfig(conf))

  "configuration" should {

    "return override-system-date-time" when {

      "value is present" in {
        val configService: FrontendAppConfig = appConfig(validServicesConfiguration)
        configService.overrideSystemDateTime mustBe Some("whatEver")
      }

      "value is missing" in {
        val configService = appConfig(Configuration(ConfigFactory.empty))
        configService.overrideSystemDateTime mustBe None
      }

      "content is boolean" in {
        val config: Config    = ConfigFactory.parseString("""override-system-date-time = false""")
        val frontendAppConfig = appConfig(Configuration(config))
        frontendAppConfig.overrideSystemDateTime mustBe Some("false")
      }

    }
  }

}
