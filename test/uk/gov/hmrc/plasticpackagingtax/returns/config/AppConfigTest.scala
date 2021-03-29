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

package uk.gov.hmrc.plasticpackagingtax.returns.config

import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

class AppConfigTest extends AnyWordSpec with Matchers with MockitoSugar {

  private val validConfig: Config =
    ConfigFactory.parseString(
      """
        |urls.feedback.authenticatedLink="http://localhost:9250/contact/beta-feedback"
        |urls.feedback.unauthenticatedLink="http://localhost:9250/contact/beta-feedback-unauthenticated"
      """.stripMargin
    )

  private val validServicesConfiguration = Configuration(validConfig)
  private val validAppConfig: AppConfig  = appConfig(validServicesConfiguration)

  private def appConfig(conf: Configuration) =
    new AppConfig(conf, servicesConfig(conf))

  private def servicesConfig(conf: Configuration) = new ServicesConfig(conf)

  "The config" should {

    "have 'authenticatedFeedbackUrl' defined" in {
      validAppConfig.authenticatedFeedbackUrl() must be(
        "http://localhost:9250/contact/beta-feedback?service=plastic-packaging-tax"
      )
    }

    "have 'unauthenticatedFeedbackUrl' defined" in {
      validAppConfig.unauthenticatedFeedbackUrl() must be(
        "http://localhost:9250/contact/beta-feedback-unauthenticated?service=plastic-packaging-tax"
      )
    }
  }
}
