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

package util

import config.FrontendAppConfig
import org.mockito.MockitoSugar
import org.mockito.integrations.scalatest.ResetMocksAfterEachTest
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec

import java.time.LocalDateTime

class EdgeOfSystemSpec extends PlaySpec
  with MockitoSugar
  with BeforeAndAfterEach
  with ResetMocksAfterEachTest {

  private val frontendAppConfig = mock[FrontendAppConfig]
  private val edgeOfSystem = new EdgeOfSystem(frontendAppConfig)
  

  "localDateTimeNow" should {
    
    "test" in {
      Numeric.Implicits
    }
    
    "use the override time if it's there" in {
      val exampleDateTime = LocalDateTime.of(2023, 4, 1, 12, 11, 10)
      when(frontendAppConfig.overrideSystemDateTime) thenReturn Some(exampleDateTime)
      edgeOfSystem.localDateTimeNow mustBe exampleDateTime
    }
    
    "use the system time if override not present" ignore { // todo
      when(frontendAppConfig.overrideSystemDateTime) thenReturn None
      edgeOfSystem.localDateTimeNow mustBe (10 +- 1)
    }
    
  }
  
}

