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

import com.codahale.metrics.Timer
import com.kenshoo.play.metrics.Metrics
import config.FrontendAppConfig
import models.returns.CreditRangeOption
import org.mockito.ArgumentMatchersSugar.any
import org.mockito.MockitoSugar
import org.mockito.integrations.scalatest.ResetMocksAfterEachTest
import org.mockito.stubbing.ReturnsDeepStubs
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.must.Matchers.{a, convertToAnyMustWrapper, theSameInstanceAs, thrownBy}
import org.scalatest.wordspec.AnyWordSpec
import play.api.Logger
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, Upstream5xxResponse}

import scala.collection.Seq
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AvailableCreditYearsConnectorSpec extends AnyWordSpec 
  with MockitoSugar with ResetMocksAfterEachTest with BeforeAndAfterEach {

  private val frontendAppConfig = mock[FrontendAppConfig]
  private val httpClient = mock[HttpClient]
  private val metrics = mock[Metrics](ReturnsDeepStubs)
  private val timer = mock[Timer.Context]
  private val availableYears = mock[Seq[CreditRangeOption]]

  implicit val headerCarrier: HeaderCarrier = HeaderCarrier()
  
  private val connector = new AvailableCreditYearsConnector(httpClient, frontendAppConfig, metrics) {
    override protected val logger: Logger = mock[Logger]
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    when(metrics.defaultRegistry.timer(any).time()) thenReturn timer
    when(httpClient.GET[Any](any, any, any)(any, any, any)) thenReturn Future.successful(availableYears)
  }
  
  "get" should {
    
    "start the timer" in {
      await(connector.get("ppt-reference"))
      verify(metrics.defaultRegistry).timer("ppt.availableCreditYears.get.timer")
      verify(metrics.defaultRegistry.timer(any)).time()
    }
    
    "return list of dates" in {
      await(connector.get("ppt-reference")) mustBe theSameInstanceAs(availableYears)
      withClue("stop the timer") {
        verify(timer).stop
      }
    }

    "return an error" in {
      when(httpClient.GET[Any](any, any, any)(any, any, any)) thenReturn Future.failed(Upstream5xxResponse("message", 500, 500))
      a [DownstreamServiceError] mustBe thrownBy(await(connector.get("ppt-reference")))
      withClue("stop the timer") {
        verify(timer).stop
      }
    }

  }

}
