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

import config.FrontendAppConfig
import models.returns.CreditRangeOption
import org.mockito.ArgumentMatchersSugar.any
import org.mockito.MockitoSugar
import org.mockito.integrations.scalatest.ResetMocksAfterEachTest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers.{a, convertToAnyMustWrapper, thrownBy}
import org.scalatest.wordspec.AnyWordSpec
import play.api.http.Status
import play.api.libs.json.Json
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, Upstream5xxResponse}

import java.time.LocalDate
import scala.collection.Seq
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AvailableCreditYearsConnectorSpec extends AnyWordSpec with MockitoSugar with ResetMocksAfterEachTest {

  private val frontendAppConfig = mock[FrontendAppConfig]
  private val httpClient = mock[HttpClient]
  private val connector = new AvailableCreditYearsConnector(httpClient, frontendAppConfig)
  implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

  "Get" should {
    "return list of dates" in {
      val availableYears = Seq(
        CreditRangeOption(LocalDate.of(2022, 4, 1), LocalDate.of(2023, 3, 31)),
        CreditRangeOption(LocalDate.of(2023, 4, 1), LocalDate.of(2024, 3, 31)),
      )

      when(httpClient.GET[Any](any, any, any)(any, any, any)) thenReturn Future.successful(availableYears)

      val result = await(connector.get("ppt-reference"))

      result mustBe availableYears
    }

    "return an error" in {

      when(httpClient.GET[Any](any, any, any)(any, any, any)) thenReturn Future.failed(Upstream5xxResponse("message", 500, 500))

      a [DownstreamServiceError] mustBe thrownBy(await(connector.get("ppt-reference")))
    }
  }

}
