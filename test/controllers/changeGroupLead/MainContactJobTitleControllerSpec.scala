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

package controllers.changeGroupLead

import connectors.CacheConnector
import controllers.BetterMockActionSyntax
import controllers.actions.JourneyAction
import controllers.actions.JourneyAction.RequestFunction
import forms.changeGroupLead.MainContactJobTitleFormProvider
import models.requests.DataRequest
import navigation.Navigator
import org.mockito.Answers
import org.mockito.ArgumentMatchersSugar.any
import org.mockito.Mockito.reset
import org.mockito.MockitoSugar.{mock, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent}
import play.api.test.FakeRequest
import play.api.test.Helpers.{await, stubMessagesControllerComponents}
import play.twirl.api.Html
import views.html.changeGroupLead.MainContactJobTitleView

import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future
import models.Mode.NormalMode
import queries.Gettable


class MainContactJobTitleControllerSpec extends PlaySpec with BeforeAndAfterEach {
  private val mockMessagesApi: MessagesApi = mock[MessagesApi]
  private val controllerComponents = stubMessagesControllerComponents()
  private val mockView = mock[MainContactJobTitleView]
  private val mockFormProvider = mock[MainContactJobTitleFormProvider]
  private val mockCache = mock[CacheConnector]
  private val journeyAction = mock[JourneyAction]
  private val featureGuard = mock[FeatureGuard]
  private val dataRequest = mock[DataRequest[AnyContent]](Answers.RETURNS_DEEP_STUBS)
  private val form = mock[Form[String]]
  private val mockNavigator =  mock[Navigator]

  val sut = new MainContactJobTitleController(
    mockMessagesApi,
    mockCache,
    mockNavigator,
    journeyAction,
    featureGuard,
    mockFormProvider,
    controllerComponents,
    mockView
  )(global)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockView, journeyAction, featureGuard, dataRequest, form)

    when(mockView.apply(any, any)(any, any)).thenReturn(Html("correct view"))
    when(dataRequest.userAnswers.fill(any[Gettable[String]], any)(any)) thenReturn form
  }

  def byConvertingFunctionArgumentsToAction: (RequestFunction) => Action[AnyContent] = (function: RequestFunction) =>
    when(mock[Action[AnyContent]].apply(any))
      .thenAnswer((request: DataRequest[AnyContent]) =>
        Future.successful(function(request)))
      .getMock[Action[AnyContent]]

  "onPageLoad" must {
    "invoke the journey action" in {

      when(journeyAction.apply(any)) thenReturn mock[Action[AnyContent]]
      sut.onPageLoad(NormalMode)
      verify(journeyAction).apply(any)
    }

  }





}
