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

import org.mockito.ArgumentMatchersSugar.any
import org.mockito.MockitoSugar
import org.mockito.integrations.scalatest.ResetMocksAfterEachTest
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.time.LocalDateTime

class FrontendAppConfigSpec extends PlaySpec
  with MockitoSugar
  with BeforeAndAfterEach
  with ResetMocksAfterEachTest {

  private val configuration = mock[Configuration]
  private val servicesConfig = mock[ServicesConfig]

  val frontendAppConfig = new FrontendAppConfig(configuration, servicesConfig)

  "overrideSystemDateTime" should {

    "parse the override if it's there" in {
      when(configuration.getOptional[String](any)(any)) thenReturn Some("2023-03-31T23:59:58")
      frontendAppConfig.overrideSystemDateTime.value mustBe LocalDateTime.of(2023, 3, 31, 23, 59, 58)
    }

    "return None if override not present" in {
      when(configuration.getOptional[String](any)(any)) thenReturn None
      frontendAppConfig.overrideSystemDateTime mustBe empty
    }

  }
}
