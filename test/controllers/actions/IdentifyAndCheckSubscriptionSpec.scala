/*
 * Copyright 2025 HM Revenue & Customs
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

import org.apache.pekko.stream.testkit.NoMaterializer
import models.requests.IdentifiedRequest
import org.mockito.MockitoSugar.{verify, when}
import org.scalatestplus.mockito.MockitoSugar.mock
import org.scalatestplus.play.PlaySpec
import play.api.mvc.{ActionBuilder, AnyContent}
import play.api.mvc.BodyParsers.Default
import play.api.mvc.Results.Ok
import play.api.test.FakeRequest

import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future
import scala.util.Try

class IdentifyAndCheckSubscriptionSpec extends PlaySpec {

  val mockAuthAction         = mock[AuthAction]
  val mockSubscriptionFilter = mock[SubscriptionFilter]

  val sut = new IdentifyAndCheckSubscription(
    new Default()(NoMaterializer),
    mockAuthAction,
    mockSubscriptionFilter
  )(global)

  "invokeBlock" must {
    "compose actions" in {
      val resultingAction = mock[ActionBuilder[IdentifiedRequest, AnyContent]]
      when(mockAuthAction.andThen(mockSubscriptionFilter)).thenReturn(resultingAction)
      val request = FakeRequest()
      val block   = { _: IdentifiedRequest[_] => Future.successful(Ok("test")) }

      Try(sut.invokeBlock(request, block))

      verify(mockAuthAction).andThen(mockSubscriptionFilter)
      verify(resultingAction).invokeBlock(request, block)
    }
  }

}
