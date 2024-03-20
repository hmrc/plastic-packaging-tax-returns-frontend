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

package connectors

import base.utils.ConnectorISpec
import config.FrontendAppConfig
import models.UserAnswers
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.Mockito
import org.mockito.MockitoSugar.{atLeastOnce, verify, when}
import org.scalatest.concurrent.ScalaFutures
import uk.gov.hmrc.http.HttpClient

import java.time.LocalDateTime
import scala.concurrent.Future

class CacheConnectorSpec extends ConnectorISpec with ScalaFutures {

  val mockConfig = mock[FrontendAppConfig]
  val connector  = new CacheConnector(config = mockConfig, httpClient = mock[HttpClient])
  val dateVal    = LocalDateTime.now
  val answers    = UserAnswers("id")

  "GET" must {
    "successfully fetch cache" in {

      when {
        connector.httpClient.GET[Option[UserAnswers]](any(), any(), any())(any(), any(), any())
      } thenReturn Future.successful(Some(answers))

      whenReady(connector.get("someref")) {
        _ mustBe Some(answers)
      }
    }
  }

  "POST" must {
    "successfully write cache" in {
      Mockito.reset(connector.httpClient)

      val putUrl = s"/cache/set/someref"

      when(mockConfig.pptCacheSetUrl(any())).thenReturn("/cache/set/someref")

      connector.set("someref", answers)
      verify(connector.httpClient, atLeastOnce).POST(eqTo(putUrl), eqTo(answers), any())(any(), any(), any(), any())
    }
  }
}
