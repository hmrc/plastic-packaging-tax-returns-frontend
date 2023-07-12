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
import org.mockito.scalatest.ResetMocksAfterEachTest
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class EdgeOfSystemSpec extends PlaySpec
  with MockitoSugar
  with BeforeAndAfterEach
  with ResetMocksAfterEachTest {

  private val frontendAppConfig = mock[FrontendAppConfig]
  private val edgeOfSystem = new EdgeOfSystem(frontendAppConfig)
  

  "localDateTimeNow" should {

    "use the override time if it's there" in {
      when(frontendAppConfig.overrideSystemDateTime) thenReturn Some("2023-04-01T12:11:10")
      edgeOfSystem.localDateTimeNow mustBe LocalDateTime.of(2023, 4, 1, 12, 11, 10)
    }
    
    "use the system time if override not present" in {
      when(frontendAppConfig.overrideSystemDateTime) thenReturn None
      edgeOfSystem.localDateTimeNow.truncatedTo(ChronoUnit.MINUTES) mustBe
        LocalDateTime.now.truncatedTo(ChronoUnit.MINUTES)
    }

    "use the system time if override is invalid date" in {
      when(frontendAppConfig.overrideSystemDateTime) thenReturn Some("false")
      edgeOfSystem.localDateTimeNow.truncatedTo(ChronoUnit.MINUTES) mustBe 
        LocalDateTime.now.truncatedTo(ChronoUnit.MINUTES)
    }
    
  }
  
}

