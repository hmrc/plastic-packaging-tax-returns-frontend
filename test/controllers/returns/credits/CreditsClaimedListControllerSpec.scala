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

package controllers.returns.credits

import base.utils.JourneyActionAnswer
import connectors.CacheConnector
import controllers.actions.JourneyAction
import forms.returns.credits.CreditsClaimedListFormProvider
import models.Mode.NormalMode
import models.requests.DataRequest
import navigation.Navigator
import org.mockito.Answers
import org.mockito.ArgumentMatchersSugar.{any, eqTo}
import org.mockito.MockitoSugar.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{AnyContent, Call, MessagesControllerComponents}
import play.api.test.Helpers._
import play.twirl.api.Html
import queries.Gettable
import uk.gov.hmrc.govukfrontend.views.Aliases.{Text, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{ActionItem, Actions, Key, SummaryListRow}
import views.html.returns.credits.CreditsClaimedListView

import scala.concurrent.ExecutionContext.Implicits.global

class CreditsClaimedListControllerSpec
  extends PlaySpec
    with JourneyActionAnswer
    with MockitoSugar
    with BeforeAndAfterEach {

  private val request = mock[DataRequest[AnyContent]](Answers.RETURNS_DEEP_STUBS)

  private val messagesApi = mock[MessagesApi]
  private val cacheConnector = mock[CacheConnector]
  private val navigator = mock[Navigator]
  private val journeyAction = mock[JourneyAction]
  private val formProvider = mock[CreditsClaimedListFormProvider]
  private val view = mock[CreditsClaimedListView]


  private val sut = new CreditsClaimedListController(
    messagesApi,
    cacheConnector,
    navigator,
    journeyAction,
    formProvider,
    stubMessagesControllerComponents(),
    view
  )

  override def beforeEach(): Unit = {
    super.beforeEach()

    reset(view, request)

    when(view.apply(any, any, any)(any, any)).thenReturn(Html("correct view"))
    when(journeyAction.apply(any)).thenAnswer(byConvertingFunctionArgumentsToAction)
    when(journeyAction.async(any)).thenAnswer(byConvertingFunctionArgumentsToFutureAction)
  }

  "onPageLoad" should {

    "use the journey action" in {
      sut.onPageLoad(NormalMode)
      verify(journeyAction).apply(any)
    }

    "return 200" in {
      when(request.userAnswers.fill(any[Gettable[Boolean]], any)(any)).thenReturn(mock[Form[Boolean]])

      val result = sut.onPageLoad(NormalMode)(request)

      status(result) mustBe OK
    }

    "return a view" in {
      val boundForm = mock[Form[Boolean]]
      when(request.userAnswers.fill(any[Gettable[Boolean]], any)(any)).thenReturn(boundForm)

      await(sut.onPageLoad(NormalMode)(request))

      verify(view).apply(eqTo(boundForm), eqTo(Seq.empty), eqTo(NormalMode))(any,any)
    }

    "getting the claims from UserAnswer" in {

      val rows = Seq(
        SummaryListRow(
          key = Key(Text("exported")),
          value = Value(Text("answer")),
          actions = Some(Actions(items = Seq(
            ActionItem("/foo", Text("change")),
            ActionItem("/remove", Text("remove"))
          ))))
      )
      val boundForm = mock[Form[Boolean]]
      when(request.userAnswers.fill(any[Gettable[Boolean]], any)(any)).thenReturn(boundForm)

      await(sut.onPageLoad(NormalMode)(request))

      verify(view).apply(eqTo(boundForm), eqTo(rows), eqTo(NormalMode))(any,any)
    }
  }
}
