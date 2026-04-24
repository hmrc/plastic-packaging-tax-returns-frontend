/*
 * Copyright 2026 HM Revenue & Customs
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
import org.mockito.Mockito
import org.mockito.Mockito.{atLeastOnce, verify, when}
import org.scalatest.concurrent.ScalaFutures
import play.api.http.Status.OK
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import uk.gov.hmrc.http.{HttpResponse, StringContextOps}

import java.time.LocalDateTime
import scala.concurrent.Future

class CacheConnectorSpec extends ConnectorISpec with ScalaFutures {

  val mockConfig: FrontendAppConfig  = mock[FrontendAppConfig]
  val connector                      = new CacheConnector(config = mockConfig, httpClient = mock[HttpClientV2])
  val dateVal: LocalDateTime         = LocalDateTime.now
  val answers                        = UserAnswers("id")
  val requestBuilder: RequestBuilder = mock[RequestBuilder]

  "GET" must {
    "successfully fetch cache" in {

      when(connector.httpClient.get(any())(any())).thenReturn(requestBuilder)
      when(requestBuilder.execute[Option[UserAnswers]](any(), any())).thenReturn(Future.successful(Some(answers)))
      when(mockConfig.pptCacheGetUrl(any())).thenReturn(s"http://localhost/test-url")

      whenReady(connector.get("someref")) {
        _ mustBe Some(answers)
      }
    }
  }

  "POST" must {
    "successfully write cache" in {
      Mockito.reset(connector.httpClient)

      val putUrl = s"http://localhost/cache/set/someref"

      when(connector.httpClient.post(any())(any())).thenReturn(requestBuilder)
      when(requestBuilder.execute[HttpResponse](any(), any())).thenReturn(Future.successful(HttpResponse(OK)))
      when(requestBuilder.setHeader(any())).thenReturn(requestBuilder)
      when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)
      when(mockConfig.pptCacheSetUrl(any())).thenReturn("http://localhost/cache/set/someref")

      connector.set("someref", answers)
      verify(connector.httpClient, atLeastOnce).post(url"$putUrl")
    }
  }
}
