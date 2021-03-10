/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.plasticpackagingtax.returns.controllers

import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.mvc._
import play.api.test.Helpers.contentAsString
import play.api.test.{DefaultAwaitTimeout, FakeRequest}
import play.twirl.api.Html
import uk.gov.hmrc.plasticpackagingtax.returns.config.AppConfig

import scala.concurrent.{ExecutionContext, Future}

trait ControllerSpec
    extends AnyWordSpecLike with MockitoSugar with Matchers with GuiceOneAppPerSuite
    with BeforeAndAfterEach with DefaultAwaitTimeout {

  implicit val ec: ExecutionContext = ExecutionContext.global

  implicit val config: AppConfig = mock[AppConfig]

  def getRequest(session: (String, String) = "" -> ""): Request[AnyContentAsEmpty.type] =
    FakeRequest("GET", "").withSession(session)

  protected def viewOf(result: Future[Result]): Html = Html(contentAsString(result))

}
