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

import connectors.SubscriptionConnector
import models.PPTSubscriptionDetails
import models.requests.IdentifiedRequest
import org.mockito.ArgumentMatchersSugar.{any, eqTo}
import org.mockito.MockitoSugar.{mock, reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.JsPath
import play.api.mvc.AnyContent
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import repositories.SessionRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SubscriptionFilterSpec extends PlaySpec with BeforeAndAfterEach {

  private val subscriptionConnector = mock[SubscriptionConnector]
  private val sessionRepository = mock[SessionRepository]
  
  private val subscriptionFilter = new SubscriptionFilter(subscriptionConnector, sessionRepository)
  val request = mock[IdentifiedRequest[AnyContent]]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(subscriptionConnector, sessionRepository, request)
    when(sessionRepository.get[PPTSubscriptionDetails](any, any) (any)) thenReturn 
      Future.successful(Some(mock[PPTSubscriptionDetails]))
    when(request.cacheKey) thenReturn "cache-key"
  }

  "it" should {
    
    "check the session repo for current subscription" in {
      await { subscriptionFilter.filter(request) } mustBe None
      verify(sessionRepository).get[PPTSubscriptionDetails](eqTo("cache-key"), eqTo(JsPath \ "SubscriptionIsActive")) (any)
    }
    
  }

}
