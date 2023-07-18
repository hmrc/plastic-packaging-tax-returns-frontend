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
import connectors.TaxReturnsConnector.StatusCode
import models.returns.{AmendsCalculations, Calculations, DDInProgressApi, IdDetails, ReturnDisplayApi, ReturnDisplayDetails}
import org.apache.http.HttpException
import org.mockito.ArgumentMatchers.{eq => meq}
import org.mockito.ArgumentMatchersSugar.any
import org.mockito.MockitoSugar.{mock, reset, verify, when}
import org.mockito.stubbing.ReturnsDeepStubs
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.must.Matchers.{a, convertToAnyMustWrapper, thrownBy}
import org.scalatest.wordspec.AnyWordSpec
import play.api.http.Status.OK
import play.api.libs.json.{JsObject, JsValue}
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse, UpstreamErrorResponse}

import scala.concurrent.{ExecutionContext, Future}


class TaxReturnsConnectorSpec extends AnyWordSpec with BeforeAndAfterEach {
  private val frontendAppConfig = mock[FrontendAppConfig]
  private val metrics2 = mock[Metrics](ReturnsDeepStubs)
  private val timerContext = mock[Timer.Context]
  private val httpClient2 = mock[HttpClient]

  protected implicit val ec2: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  protected implicit val hc2: HeaderCarrier = mock[HeaderCarrier]

  private val connector = new TaxReturnsConnector(httpClient2, frontendAppConfig, metrics2)

  private val aReturnDisplayApi: ReturnDisplayApi = {
    val bd: BigDecimal = 0.1
    val l: Long = 1
    ReturnDisplayApi(
      "", IdDetails("a-ppt-ref", "subId"), None, ReturnDisplayDetails(l, l, l, l, l, l, bd, bd, l, bd)
    )
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(httpClient2, frontendAppConfig, metrics2, timerContext)
    when(frontendAppConfig.pptReturnSubmissionUrl(any)) thenReturn "return-submission-url"
    when(metrics2.defaultRegistry.timer(any).time()) thenReturn timerContext
    when(httpClient2.GET[Any](any, any, any)(any, any, any)) thenReturn Future.successful(JsObject.empty)
    when(httpClient2.POSTEmpty[Any](any, any)(any, any, any)) thenReturn Future.successful(HttpResponse(OK, JsObject.empty.toString()))
  }

  "Tax Returns Connector" should {
    
    "use the correct timer" when {
      "getting a return" in {
        await(connector.get("id", "period"))
        verify(metrics2.defaultRegistry).timer("ppt.returns.get.timer")
        verify(timerContext).stop()
      }
      "submitting a return" in {
        await(connector.submit("id"))
        verify(metrics2.defaultRegistry).timer("ppt.returns.submit.timer")
        verify(timerContext).stop()
      }
      "amending a return" in {
        when(httpClient2.POSTEmpty[HttpResponse](any, any)(any, any, any)) thenReturn Future.successful(mock[HttpResponse])
        await(connector.amend("id"))
        verify(metrics2.defaultRegistry).timer("ppt.returns.submit.timer")
        verify(timerContext).stop()
      }
    }

    "get correctly" in {
      when(httpClient2.GET[ReturnDisplayApi](any, any, any)(any, any, any)) thenReturn Future.successful(aReturnDisplayApi)
      await(connector.get("ppt-reference", "period-key")) mustBe aReturnDisplayApi
      verify(frontendAppConfig).pptReturnSubmissionUrl("ppt-reference")
      verify(httpClient2).GET(meq("return-submission-url/period-key"), any, any)(any, any, any)
    }

    "submit" when {
      "in all cases" in {
        await(connector.submit("ppt-reference"))
        verify(frontendAppConfig).pptReturnSubmissionUrl("ppt-reference")
        verify(httpClient2).POSTEmpty(meq("return-submission-url"), any)(any, any, any)
      }
      
      "there is a charge reference" in {
        when(httpClient2.POSTEmpty[HttpResponse](any, any)(any, any, any)) thenReturn Future.successful(
          HttpResponse(OK, """{"chargeDetails": {"chargeReference": "PANTESTPAN"}}""")
        )
        await(connector.submit("ppt-reference")) mustBe Right(Some("PANTESTPAN"))
      }

      "there is no charge reference" in {
        when(httpClient2.GET[HttpResponse](any, any, any)(any, any, any)) thenReturn Future.successful(
          HttpResponse(OK, """{"chargeDetails": {}}""")
        )
        await(connector.submit("ppt-reference")) mustBe Right(None)
      }
      
      "the return obligation is already fulfilled" in {
        when(httpClient2.POSTEmpty[HttpResponse](any, any)(any, any, any)) thenReturn Future.successful(
          HttpResponse(StatusCode.RETURN_ALREADY_SUBMITTED, """{"returnAlreadyReceived": "12A3"}""")
        )
        await(connector.submit("ppt-reference")) mustBe Left(AlreadySubmitted)
      }

      "Something goes wrong" in {
        when(httpClient2.POSTEmpty[HttpResponse](any, any)(any, any, any)) thenReturn Future.failed(new HttpException(
          "exception-message", new Exception("Something went wrong.")
        ))
        intercept[DownstreamServiceError](await(connector.submit("ppt-reference")))
      }
    }

    "Amend" when {
      
      "in all cases" in {
        when(httpClient2.POSTEmpty[HttpResponse](any, any)(any, any, any)) thenReturn Future.successful(mock[HttpResponse])
        when(frontendAppConfig.pptReturnAmendUrl(any)) thenReturn "return-amend-url"
        await(connector.amend("ppt-reference"))
        verify(frontendAppConfig).pptReturnAmendUrl("ppt-reference")
        verify(httpClient2).POSTEmpty(meq("return-amend-url"), any)(any, any, any)
      }

      "there is a charge reference" in {
        when(httpClient2.POSTEmpty[HttpResponse](any, any)(any, any, any)) thenReturn Future.successful(
          HttpResponse(200, """{"chargeDetails": {"chargeReference": "SOMEREF"}}""")
        )
        await(connector.amend("ppt-reference")) mustBe Some("SOMEREF")
      }

      "there is no charge reference" in {
        when(httpClient2.POSTEmpty[HttpResponse](any, any)(any, any, any)) thenReturn
          Future.successful(HttpResponse(200, """{"chargeDetails": null}"""))
        await(connector.amend("ppt-reference")) mustBe None
      }
    }

    "throw" when {
      "get request fails" in {
        when(httpClient2.GET[JsValue](any, any, any)(any, any, any)) thenReturn Future.failed(UpstreamErrorResponse(
          message = "exception-message", statusCode = 500, reportAs = 500
        ))
        a[DownstreamServiceError] mustBe thrownBy(await(connector.get("ppt-reference", "period-key")))
      }

      "submit response cannot be parsed" in {
        when(httpClient2.POSTEmpty[JsValue](any, any)(any, any, any)) thenReturn Future.failed(UpstreamErrorResponse(
          message = "exception-message", statusCode = 500, reportAs = 500
        ))
        a[DownstreamServiceError] mustBe thrownBy(await(connector.submit("ppt-reference")))
      }

      "amend response cannot be parsed" in {
        when(httpClient2.POSTEmpty[HttpResponse](any, any)(any, any, any)) thenReturn Future.failed(UpstreamErrorResponse(
          message = "exception-message", statusCode = 500, reportAs = 500
        ))
        a[DownstreamServiceError] mustBe thrownBy(await(connector.amend("ppt-reference")))
      }
    }
  }

  "ddInProgress" should {
    "call the backend" when {
      "Ok" in {
        when(frontendAppConfig.pptDDInProgress(any, any)).thenReturn("/pptDDInProgress")
        val mockResult = mock[DDInProgressApi]
        when(httpClient2.GET[DDInProgressApi](any, any, any)(any, any, any)) thenReturn Future.successful(mockResult)

        val result = await(connector.ddInProgress("ppt-ref", "periodKey"))

        result mustBe mockResult
      }
      "error" in {
        when(frontendAppConfig.pptDDInProgress(any, any)).thenReturn("/pptDDInProgress")
        object TestException extends Exception
        when(httpClient2.GET[DDInProgressApi](any, any, any)(any, any, any)) thenReturn Future.failed(TestException)

        intercept[TestException.type](await(connector.ddInProgress("ppt-ref", "periodKey")))
      }
    }
  }

  "getCalculationAmends" should {
    "call the backend" when {
      "Ok" in {
        when(frontendAppConfig.pptAmendsCalculationUrl(any)).thenReturn("/getCalculationAmends")
        val mockResult = mock[AmendsCalculations]
        when(httpClient2.GET[AmendsCalculations](any, any, any)(any, any, any)) thenReturn Future.successful(mockResult)

        val result = await(connector.getCalculationAmends("ppt-ref"))

        result mustBe Right(mockResult)
      }
      "error" in {
        when(frontendAppConfig.pptDDInProgress(any, any)).thenReturn("/getCalculationAmends")
        object TestException extends Exception("boom")
        when(httpClient2.GET[DDInProgressApi](any, any, any)(any, any, any)) thenReturn Future.failed(TestException)

        val result = await(connector.getCalculationAmends("ppt-ref"))

        result mustBe Left(DownstreamServiceError("Failed to get calculations, error: boom", TestException))
      }
    }
  }
  "getCalculationReturns" should {
    "call the backend" when {
      "Ok" in {
        when(frontendAppConfig.pptReturnsCalculationUrl(any)).thenReturn("/pptReturnsCalculationUrl")
        val mockResult = mock[Calculations]
        when(httpClient2.GET[Calculations](any, any, any)(any, any, any)) thenReturn Future.successful(mockResult)

        val result = await(connector.getCalculationReturns("ppt-ref"))

        result mustBe Right(mockResult)
      }
      "error" in {
        when(frontendAppConfig.pptDDInProgress(any, any)).thenReturn("/pptReturnsCalculationUrl")
        object TestException extends Exception("boom")
        when(httpClient2.GET[Calculations](any, any, any)(any, any, any)) thenReturn Future.failed(TestException)

        val result = await(connector.getCalculationReturns("ppt-ref"))

        result mustBe Left(DownstreamServiceError("Failed to get calculations, error: boom", TestException))
      }
    }
  }


}