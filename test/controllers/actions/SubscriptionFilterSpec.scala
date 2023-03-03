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

package controllers.actions

import app.RoutesPrefix
import connectors.{DownstreamServiceError, SubscriptionConnector}
import models.requests.IdentifiedRequest
import models.subscription.subscriptionDisplay.SubscriptionDisplayResponse
import models.{EisError, EisFailure, PPTSubscriptionDetails}
import org.mockito.ArgumentMatchersSugar.{any, eqTo}
import org.mockito.MockitoSugar.{mock, never, reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.Inside.inside
import org.scalatest.enablers.Messaging
import org.scalatestplus.play.PlaySpec
import play.api.http.Status
import play.api.libs.json.JsPath
import play.api.mvc.{AnyContent, RequestHeader, Result, Session}
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import repositories.SessionRepository
import uk.gov.hmrc.http.{HeaderCarrier, ServiceUnavailableException}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SubscriptionFilterSpec extends PlaySpec with BeforeAndAfterEach {

  private val subscriptionConnector = mock[SubscriptionConnector]
  private val sessionRepository = mock[SessionRepository]
  
  private val subscriptionFilter = new SubscriptionFilter(subscriptionConnector, sessionRepository) {
    override def fromRequestAndSession(request: RequestHeader, session: Session): HeaderCarrier =
      mock[HeaderCarrier] // todo can we do better?
  }
  
  private val request = mock[IdentifiedRequest[AnyContent]]
  private val eisFailure = mock[EisFailure]
  private implicit val resolveImplicitAmbiguity: Messaging[DownstreamServiceError] = Messaging.messagingNatureOfThrowable
  
  object RandoError extends RuntimeException

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(subscriptionConnector, sessionRepository, request, eisFailure)
    when(sessionRepository.get[PPTSubscriptionDetails](any, any) (any)) thenReturn Future.successful(Some(mock[PPTSubscriptionDetails]))
    when(sessionRepository.set(any, any, any) (any)) thenReturn Future.successful(true)
    when(request.cacheKey) thenReturn "cache-key"
    when(request.pptReference) thenReturn "ppt-ref"
    when(subscriptionConnector.get(any)(any)) thenReturn Future.successful(Right(mock[SubscriptionDisplayResponse]))
    
    // Routes behaves differently for play running states, Test vs Production
    RoutesPrefix.setPrefix("/")
  }

  private def callFilter = await {
    subscriptionFilter.filter(request)
  }

  "it" should {
    
    "check the session repo for current subscription" in {
      callFilter
      verify(sessionRepository).get[PPTSubscriptionDetails](eqTo("cache-key"), eqTo(JsPath \ "SubscriptionIsActive")) (any)
    }
    
    "handle" when {
      
      "subscription is still in sessions repo" in {
        callFilter mustBe None
        verify(subscriptionConnector, never).get(any) (any)
        verify(sessionRepository, never).set(any, any, any) (any)
      }
      
      "subscription cache expired / not present" in {
        when(sessionRepository.get[PPTSubscriptionDetails](any, any) (any)) thenReturn Future.successful(None)
        callFilter mustBe None
        verify(subscriptionConnector).get(eqTo("ppt-ref")) (any) // TODO check for header carrier
        verify(sessionRepository).set(eqTo("cache-key"), eqTo(JsPath \ "SubscriptionIsActive"), any) (any) // todo check value
      }

      "subscription is de-registered" in { // todo stop this one logging!
        when(eisFailure.isDeregistered) thenReturn true
        when(sessionRepository.get[PPTSubscriptionDetails](any, any) (any)) thenReturn Future.successful(None)
        when(subscriptionConnector.get(any)(any)) thenReturn Future.successful(Left(eisFailure))

        inside (callFilter.value) {
          case Result(header, _, _, _, _) => 
            header must have ('status (Status.SEE_OTHER))
            header.headers must contain ("Location" -> "/deregistered")
        }
      }

      "downstream unreliability" in {
        when(eisFailure.isDependentSystemsNotResponding) thenReturn true
        when(eisFailure.failures) thenReturn Some(Seq(EisError("code", "reason")))
        when(sessionRepository.get[PPTSubscriptionDetails](any, any) (any)) thenReturn Future.successful(None)
        when(subscriptionConnector.get(any)(any)) thenReturn Future.successful(Left(eisFailure))

        val thrown = the [DownstreamServiceError] thrownBy { callFilter }
        thrown must have message "Dependent systems are currently not responding." 
        thrown.getCause mustBe a[ServiceUnavailableException]
        thrown.getCause must have message "Some(List(EisError(code,reason)))"
      }

      "some other downstream error" in {
        when(eisFailure.failures) thenReturn Some(Seq(EisError("code", "reason")))
        when(sessionRepository.get[PPTSubscriptionDetails](any, any) (any)) thenReturn Future.successful(None)
        when(subscriptionConnector.get(any)(any)) thenReturn Future.successful(Left(eisFailure))
        // todo should we log the entire payload like above
        the [RuntimeException] thrownBy callFilter must have message "Failed to get subscription - Some(reason)"
      }

      "session repo get fails" in {
        when(sessionRepository.get[PPTSubscriptionDetails](any, any) (any)) thenReturn Future.failed(RandoError)
        an [RandoError.type] must be thrownBy callFilter
      }

      "session repo set fails" in {
        when(sessionRepository.get[PPTSubscriptionDetails](any, any) (any)) thenReturn Future.successful(None)
        when(sessionRepository.set(any, any, any) (any)) thenReturn Future.failed(RandoError)
        an [RandoError.type] must be thrownBy callFilter
      }
      
      "subscription connector get fails" in {
        when(sessionRepository.get[PPTSubscriptionDetails](any, any) (any)) thenReturn Future.successful(None)
        when(subscriptionConnector.get(any)(any)) thenReturn Future.failed(RandoError)
        an [RandoError.type] must be thrownBy callFilter
      }
      
    }
    
  }

}
