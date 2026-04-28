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

import com.codahale.metrics.Timer
import config.FrontendAppConfig
import connectors.TaxReturnsConnector.StatusCode
import models.returns.*
import org.apache.http.HttpException
import org.mockito.ArgumentMatchers.{any, eq as meq}
import org.mockito.Mockito.{RETURNS_DEEP_STUBS, reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.must.Matchers.{a, mustBe, thrownBy}
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.http.Status.OK
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse, StringContextOps, UpstreamErrorResponse}
import uk.gov.hmrc.play.bootstrap.metrics.Metrics

import java.net.URL
import scala.concurrent.{ExecutionContext, Future}

class TaxReturnsConnectorSpec extends AnyWordSpec with BeforeAndAfterEach {
  private val frontendAppConfig = mock[FrontendAppConfig]
  private val metrics2          = mock[Metrics](RETURNS_DEEP_STUBS)
  private val timerContext      = mock[Timer.Context]
  private val httpClient2       = mock[HttpClientV2]
  private val requestBuilder    = mock[RequestBuilder]

  protected implicit val ec2: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  protected implicit val hc2: HeaderCarrier    = mock[HeaderCarrier]

  private val connector = new TaxReturnsConnector(httpClient2, frontendAppConfig, metrics2)

  private val aReturnDisplayApi: ReturnDisplayApi = {
    val bd: BigDecimal = 0.1
    val l: Long        = 1
    ReturnDisplayApi(
      "",
      IdDetails("a-ppt-ref", "subId"),
      None,
      ReturnDisplayDetails(l, l, l, l, l, l, bd, bd, l, bd)
    )
  }

  val validJsonResponse: JsObject = Json.obj(
    "chargeDetails" -> Json.obj(
      "chargeReference" -> "PANTESTPAN"
    )
  )

  val returnSubmissionURL: String   = url"http://localhost/return-submission-url".toString
  val returnsCalculationUrl: String = url"http://localhost/return-calculate".toString
  val returnAmendUrl: String        = url"http://localhost/return-amend-url".toString

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(httpClient2, frontendAppConfig, metrics2, timerContext, requestBuilder)
    when(frontendAppConfig.pptReturnSubmissionUrl(any)) thenReturn returnSubmissionURL
    when(frontendAppConfig.pptReturnsCalculationUrl(any)) thenReturn returnsCalculationUrl
    when(frontendAppConfig.pptReturnAmendUrl(any)) thenReturn returnAmendUrl
    when(metrics2.defaultRegistry.timer(any).time()) thenReturn timerContext
    when(httpClient2.get(any[URL])(any)).thenReturn(requestBuilder)
    when(httpClient2.post(any[URL])(any)).thenReturn(requestBuilder)
    when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)

    when(
      requestBuilder.execute[SubmittedReturn](
        any[HttpReads[SubmittedReturn]],
        any[ExecutionContext]
      )
    ).thenReturn(Future.successful(mock[SubmittedReturn]))
  }

  "Tax Returns Connector" should {

    "use the correct timer" when {
      "getting a return" in {
        await(connector.get("id", "period"))
        verify(metrics2.defaultRegistry).timer("ppt.returns.get.timer")
        verify(timerContext).stop()
      }
      "submitting a return" in {
        when(requestBuilder.execute[HttpResponse](any, any)).thenReturn(
          Future.successful(HttpResponse(OK, json = validJsonResponse, headers = Map.empty))
        )
        await(connector.submit("id"))
        verify(metrics2.defaultRegistry).timer("ppt.returns.submit.timer")
        verify(timerContext).stop()
      }
      "amending a return" in {
        when(requestBuilder.execute[HttpResponse](any, any)).thenReturn(
          Future.successful(HttpResponse(OK, json = validJsonResponse, headers = Map.empty))
        )
        await(connector.amend("id"))
        verify(metrics2.defaultRegistry).timer("ppt.returns.submit.timer")
        verify(timerContext).stop()
      }
    }

    "get correctly" in {
      when(requestBuilder.execute[ReturnDisplayApi](any, any)).thenReturn(Future.successful(aReturnDisplayApi))
      await(connector.get("ppt-reference", "period-key")) mustBe aReturnDisplayApi
      verify(frontendAppConfig).pptReturnSubmissionUrl("ppt-reference")
      verify(httpClient2).get(meq(url"$returnSubmissionURL/period-key"))(any)
    }

    "submit" when {
      "in all cases" in {
        when(requestBuilder.execute[HttpResponse](any, any)).thenReturn(
          Future.successful(HttpResponse(OK, json = validJsonResponse, headers = Map.empty))
        )
        await(connector.submit("ppt-reference"))
        verify(frontendAppConfig).pptReturnSubmissionUrl("ppt-reference")
        verify(httpClient2).post(meq(url"$returnSubmissionURL"))(any)
      }

      "there is a charge reference" in {
        when(requestBuilder.execute[HttpResponse](any, any)).thenReturn(
          Future.successful(
            HttpResponse(OK, """{"chargeDetails": {"chargeReference": "PANTESTPAN"}}""")
          )
        )
        await(connector.submit("ppt-reference")) mustBe Right(Some("PANTESTPAN"))
      }

      "there is no charge reference" in {
        when(requestBuilder.execute[HttpResponse](any, any)).thenReturn(
          Future.successful(HttpResponse(OK, """{"chargeDetails": {}}"""))
        )
        await(connector.submit("ppt-reference")) mustBe Right(None)
      }

      "the return obligation is already fulfilled" in {
        when(requestBuilder.execute[HttpResponse](any, any)).thenReturn(
          Future.successful(
            HttpResponse(StatusCode.RETURN_ALREADY_SUBMITTED, """{"returnAlreadyReceived": "12A3"}""")
          )
        )
        await(connector.submit("ppt-reference")) mustBe Left(AlreadySubmitted)
      }

      "Something goes wrong" in {
        when(requestBuilder.execute[HttpResponse](any, any)).thenReturn(
          Future.failed(
            new HttpException(
              "exception-message",
              new Exception("Something went wrong.")
            )
          )
        )
        intercept[DownstreamServiceError](await(connector.submit("ppt-reference")))
      }
    }

    "Amend" when {

      "in all cases" in {
        when(requestBuilder.execute[HttpResponse](any, any)).thenReturn(
          Future.successful(
            HttpResponse(StatusCode.RETURN_ALREADY_SUBMITTED, """{"returnAlreadyReceived": "12A3"}""")
          )
        )

        when(requestBuilder.execute[HttpResponse]).thenReturn(
          Future.successful(
            mock[HttpResponse]
          )
        )
        when(frontendAppConfig.pptReturnAmendUrl(any)) thenReturn returnAmendUrl
        await(connector.amend("ppt-reference"))
        verify(frontendAppConfig).pptReturnAmendUrl("ppt-reference")
        verify(httpClient2).post(meq(url"$returnAmendUrl"))(any)
      }

      "there is a charge reference" in {
        when(requestBuilder.execute[HttpResponse](any, any)).thenReturn(
          Future.successful(
            HttpResponse(200, """{"chargeDetails": {"chargeReference": "SOMEREF"}}""")
          )
        )
        await(connector.amend("ppt-reference")) mustBe Some("SOMEREF")
      }

      "there is no charge reference" in {
        when(requestBuilder.execute[HttpResponse](any, any)).thenReturn(
          Future.successful(HttpResponse(200, """{"chargeDetails": null}"""))
        )
        await(connector.amend("ppt-reference")) mustBe None
      }
    }

    "throw" when {
      "get request fails" in {
        when(requestBuilder.execute[JsValue](any, any)).thenReturn(
          Future.failed(
            UpstreamErrorResponse(
              message = "exception-message",
              statusCode = 500,
              reportAs = 500
            )
          )
        )
        a[DownstreamServiceError] mustBe thrownBy(await(connector.get("ppt-reference", "period-key")))
      }

      "submit response cannot be parsed" in {
        when(requestBuilder.execute[JsValue](any, any)).thenReturn(
          Future.failed(
            UpstreamErrorResponse(
              message = "exception-message",
              statusCode = 500,
              reportAs = 500
            )
          )
        )
        a[DownstreamServiceError] mustBe thrownBy(await(connector.submit("ppt-reference")))
      }

      "amend response cannot be parsed" in {
        when(requestBuilder.execute[HttpResponse]).thenReturn(
          Future.failed(
            UpstreamErrorResponse(
              message = "exception-message",
              statusCode = 500,
              reportAs = 500
            )
          )
        )
        a[DownstreamServiceError] mustBe thrownBy(await(connector.amend("ppt-reference")))
      }
    }
  }

  "ddInProgress" should {
    "call the backend" when {
      "Ok" in {
        when(frontendAppConfig.pptDDInProgress(any, any)).thenReturn("http://localhost/pptDDInProgress")
        val mockResult = mock[DDInProgressApi]
        when(requestBuilder.execute[DDInProgressApi](any, any)).thenReturn(Future.successful(mockResult))

        val result = await(connector.ddInProgress("ppt-ref", "periodKey"))

        result mustBe mockResult
      }
      "error" in {
        when(frontendAppConfig.pptDDInProgress(any, any)).thenReturn("http://localhost/pptDDInProgress")
        object TestException extends RuntimeException
        when(requestBuilder.execute[DDInProgressApi](any, any)).thenReturn(Future.failed(TestException))

        intercept[TestException.type](await(connector.ddInProgress("ppt-ref", "periodKey")))
      }
    }
  }

  "getCalculationAmends" should {
    "call the backend" when {
      "Ok" in {
        when(frontendAppConfig.pptAmendsCalculationUrl(any)).thenReturn(returnsCalculationUrl)
        val mockResult = mock[AmendsCalculations]
        when(requestBuilder.execute[AmendsCalculations](any, any)).thenReturn(Future.successful(mockResult))

        val result = await(connector.getCalculationAmends("ppt-ref"))

        result mustBe Right(mockResult)
      }
      "error" in {
        when(frontendAppConfig.pptAmendsCalculationUrl(any)).thenReturn(returnsCalculationUrl)
        object TestException extends RuntimeException("boom")
        when(requestBuilder.execute[AmendsCalculations](any, any)).thenReturn(Future.failed(TestException))

        val result = await(connector.getCalculationAmends("ppt-ref"))

        result mustBe Left(DownstreamServiceError("Failed to get calculations, error: boom", TestException))
      }
    }
  }
  "getCalculationReturns" should {
    "call the backend" when {
      "Ok" in {
        when(frontendAppConfig.pptReturnsCalculationUrl(any)).thenReturn(returnsCalculationUrl)
        val mockResult = mock[Calculations]
        when(requestBuilder.execute[Calculations](any, any)).thenReturn(Future.successful(mockResult))

        val result = await(connector.getCalculationReturns("ppt-ref"))

        result mustBe Right(mockResult)
      }
      "error" in {
        when(frontendAppConfig.pptDDInProgress(any, any)).thenReturn(returnsCalculationUrl)
        object TestException extends RuntimeException("boom")
        when(requestBuilder.execute[Calculations](any, any)).thenReturn(Future.failed(TestException))

        val result = await(connector.getCalculationReturns("ppt-ref"))

        result mustBe Left(DownstreamServiceError("Failed to get calculations, error: boom", TestException))
      }
    }
  }

}
