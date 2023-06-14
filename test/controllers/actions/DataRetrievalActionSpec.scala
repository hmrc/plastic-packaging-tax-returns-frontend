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

import base.SpecBase
import connectors.CacheConnector
import models.requests.{IdentifiedRequest, OptionalDataRequest}
import models.{SignedInUser, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.FakeRequest
import support.PptTestData
import support.PptTestData.pptEnrolment

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DataRetrievalActionSpec extends SpecBase with MockitoSugar {

  val testUser: SignedInUser    = PptTestData.newUser("123", Some(pptEnrolment("333")))

  class Harness(cacheConnector: CacheConnector)
      extends DataRetrievalActionImpl(cacheConnector) {

    def callTransform[A](request: IdentifiedRequest[A]): Future[OptionalDataRequest[A]] =
      transform(request)

  }

  "Data Retrieval Action" - {

    "when there is no data in the cache" - {

      "must build a userAnswers object and add it to the request" in {

        val cacheConnector = mock[CacheConnector]
        when(cacheConnector.get(any())(any())) thenReturn Future(None)
        val action = new Harness(cacheConnector)

        val result =
          action.callTransform(IdentifiedRequest(FakeRequest(), testUser, "12345")).futureValue

        result.userAnswers.id mustBe "Int-ba17b467-90f3-42b6-9570-73be7b78eb2b-12345"

      }
    }

    "when there is data in the cache" - {

      "must build a userAnswers object and add it to the request" in {

        val cacheConnector = mock[CacheConnector]
        when(cacheConnector.get(any())(any())) thenReturn Future(
          Some(UserAnswers("Int-ba17b467-90f3-42b6-9570-73be7b78eb2b-12345"))
        )
        val action = new Harness(cacheConnector)

        val result =
          action.callTransform(IdentifiedRequest(FakeRequest(), testUser, "12345")).futureValue

        result.userAnswers.id mustBe "Int-ba17b467-90f3-42b6-9570-73be7b78eb2b-12345"

      }
    }
  }
}
