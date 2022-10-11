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
import controllers.actions.IdentifierActionOld
import models.SignedInUser
import models.requests.{IdentifiedRequest, IdentityData}
import play.api.inject.bind
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.Enrolments
import views.html.DeregisteredView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

//todo mess.
class FakeIdentifierActionOld @Inject() (bodyParsers: PlayBodyParsers) extends IdentifierActionOld {

  override def invokeBlock[A](
                               request: Request[A],
                               block: IdentifiedRequest[A] => Future[Result]
                             ): Future[Result] = {
    val pptLoggedInUser = SignedInUser(Enrolments(Set.empty), IdentityData(internalId = "SomeId"))
    block(IdentifiedRequest(request, pptLoggedInUser, None))
  }

  override def parser: BodyParser[AnyContent] =
    bodyParsers.default
  override protected def executionContext: ExecutionContext =
    scala.concurrent.ExecutionContext.Implicits.global
}

class DeregisteredControllerSpec extends SpecBase {

  "Deregistered Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).overrides(
        bind[IdentifierActionOld].to[FakeIdentifierActionOld]
      ).build()

      running(application) {
        val request = FakeRequest(GET, routes.DeregisteredController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[DeregisteredView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(request, messages(application)).toString
      }
    }
  }
}
