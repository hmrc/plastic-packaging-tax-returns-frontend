/*
 * Copyright 2022 HM Revenue & Customs
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

package controllers

import base.SpecBase
import connectors.CacheConnector
import org.mockito.ArgumentMatchers.{any, refEq}
import org.mockito.Mockito.{atLeastOnce, never, times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future

class KeepAliveControllerSpec extends SpecBase with MockitoSugar {

  "keepAlive" - {

    "when the user has answered some questions" - {

      "must keep the answers alive and return OK" in {

        val mockCacheConnector = mock[CacheConnector]
        when(mockCacheConnector.get(refEq(emptyUserAnswers.id), any())(any())) thenReturn Future.successful(Some(emptyUserAnswers))

        val application =
          applicationBuilder(Some(emptyUserAnswers))
            .overrides(bind[CacheConnector].toInstance(mockCacheConnector))
            .build()

        running(application) {

          val request = FakeRequest(GET, routes.KeepAliveController.keepAlive.url)

          val result = route(application, request).value

          status(result) mustEqual OK
          // TODO verify(mockCacheConnector, atLeastOnce()).get(refEq(emptyUserAnswers.id), any())(any())
        }
      }
    }

    "when the user has not answered any questions" - {

      "must return OK" in {

        val mockCacheConnector = mock[CacheConnector]

        val application =
          applicationBuilder(None)
            .overrides(bind[CacheConnector].toInstance(mockCacheConnector))
            .build()

        running(application) {

          val request = FakeRequest(GET, routes.KeepAliveController.keepAlive.url)

          val result = route(application, request).value

          status(result) mustEqual OK
          verify(mockCacheConnector, never()).get(any(), any())(any())
        }
      }
    }
  }
}
