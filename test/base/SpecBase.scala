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

package base

import cacheables.{ReturnDisplayApiCacheable, ReturnObligationCacheable}
import config.FrontendAppConfig
import connectors.{CacheConnector, TaxReturnsConnector}
import controllers.actions._
import models.UserAnswers
import models.returns._
import org.mockito.MockitoSugar.mock
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{OptionValues, TryValues}
import play.api.Application
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.FakeRequest
import repositories.SessionRepository
import uk.gov.hmrc.http.HttpResponse

import java.time.LocalDate

trait SpecBase
    extends AnyFreeSpec
    with Matchers
    with TryValues
    with OptionValues
    with ScalaFutures
    with IntegrationPatience {

  val userAnswersId: String = "123"

  implicit val config: FrontendAppConfig                   = mock[FrontendAppConfig]
  implicit val cacheConnector: CacheConnector              = mock[CacheConnector]
  implicit val mockSessionRepo: SessionRepository          = mock[SessionRepository]
  implicit val mockTaxReturnConnector: TaxReturnsConnector = mock[TaxReturnsConnector]

  def userAnswers: UserAnswers = UserAnswers(userAnswersId)
    .set(ReturnDisplayApiCacheable, retDisApi).get
    .set(ReturnObligationCacheable, taxReturnOb).get

  val taxReturnOb: TaxReturnObligation = TaxReturnObligation(
    LocalDate.parse("2022-04-01"),
    LocalDate.parse("2022-06-30"),
    LocalDate.parse("2022-06-30").plusWeeks(8),
    "00XX"
  )

  val mockResponse: HttpResponse = mock[HttpResponse]

  val charge: ReturnDisplayChargeDetails = ReturnDisplayChargeDetails(
    periodFrom = "2022-04-01",
    periodTo = "2022-06-30",
    periodKey = "22AC",
    chargeReference = Some("pan"),
    receiptDate = "2022-06-30T00:00:00Z",
    returnType = "TYPE"
  )
  val retDisApi: ReturnDisplayApi = ReturnDisplayApi(
    "",
    IdDetails("", ""),
    Some(charge),
    ReturnDisplayDetails(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)
  )

  def getRequest(session: (String, String) = "" -> ""): Request[AnyContentAsEmpty.type] =
    FakeRequest("GET", "").withSession(session)

  def emptyUserAnswers: UserAnswers = UserAnswers(userAnswersId)

  def messages(app: Application): Messages =
    app.injector.instanceOf[MessagesApi].preferred(FakeRequest())

  def messages(app: Application, message: String): String =
    app.injector.instanceOf[MessagesApi].preferred(FakeRequest()).apply(message)

  protected def applicationBuilder(
    userAnswers: Option[UserAnswers] = None
  ): GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .overrides(
        bind[DataRequiredAction].to[DataRequiredActionImpl],
        bind[IdentifierAction].to[FakeIdentifierActionWithEnrolment],
        bind[DataRetrievalAction].toInstance(new FakeDataRetrievalAction(userAnswers)),
        bind[SessionRepository].toInstance(mockSessionRepo)
      )

  protected def applicationBuilderNotEnrolled(
    userAnswers: Option[UserAnswers] = None
  ): GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .overrides(
        bind[DataRequiredAction].to[DataRequiredActionImpl],
        bind[AuthLoggedInAction].to[FakeAuthActionNotEnrolled],
        bind[DataRetrievalAction].toInstance(new FakeDataRetrievalAction(userAnswers)),
        bind[SessionRepository].toInstance(mock[SessionRepository])
      )

  protected def applicationBuilderFailedAuth(
    userAnswers: Option[UserAnswers] = None
  ): GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .overrides(
        bind[DataRequiredAction].to[DataRequiredActionImpl],
        bind[IdentifierAction].to[FakeIdentifierActionFailed],
        bind[DataRetrievalAction].toInstance(new FakeDataRetrievalAction(userAnswers)),
        bind[SessionRepository].toInstance(mock[SessionRepository])
      )

}
