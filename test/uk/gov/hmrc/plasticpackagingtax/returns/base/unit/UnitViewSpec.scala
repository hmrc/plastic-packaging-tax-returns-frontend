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

package uk.gov.hmrc.plasticpackagingtax.returns.base.unit

import com.codahale.metrics.SharedMetricRegistries
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{AnyContent, Request}
import play.api.test.FakeRequest
import uk.gov.hmrc.plasticpackagingtax.returns.base.PptTestData
import uk.gov.hmrc.plasticpackagingtax.returns.builders.TaxReturnBuilder
import uk.gov.hmrc.plasticpackagingtax.returns.models.request.{AuthenticatedRequest, JourneyRequest}
import uk.gov.hmrc.plasticpackagingtax.returns.spec.ViewMatchers
import utils.FakeRequestCSRFSupport._

abstract class UnitViewSpec
    extends AnyWordSpec with MockTaxReturnsConnector with ViewMatchers with Injector
    with GuiceOneAppPerSuite with ViewAssertions with MessagesSupport with TaxReturnBuilder {

  val journeyRequest: JourneyRequest[AnyContent] = new JourneyRequest(
    new AuthenticatedRequest(FakeRequest().withCSRFToken, PptTestData.newUser(), Some("123")),
    aTaxReturn(),
    Some("XMPPT0000000001")
  )

  override def fakeApplication(): Application = {
    SharedMetricRegistries.clear()
    new GuiceApplicationBuilder().build()
  }

  "Exercise generated rendering methods" in {
    exerciseGeneratedRenderingMethods()
  }

  def exerciseGeneratedRenderingMethods(): Unit

}

trait MessagesSupport {

  implicit val request: Request[AnyContent] =
    new AuthenticatedRequest(FakeRequest().withCSRFToken, PptTestData.newUser(), Some("123"))

  protected implicit def messages(implicit request: Request[_]): Messages =
    new AllMessageKeysAreMandatoryMessages(realMessagesApi.preferred(request))

  protected def messages(key: String, args: Any*)(implicit request: Request[_]): String =
    messages(request)(key, args: _*)

  private val realMessagesApi: MessagesApi = MessagesSupport.realMessagesApi
}

object MessagesSupport extends Injector {
  val realMessagesApi: MessagesApi = instanceOf[MessagesApi]
}

private class AllMessageKeysAreMandatoryMessages(msg: Messages) extends Messages {

  override def asJava: play.i18n.Messages = msg.asJava

  override def messages: Messages = msg.messages

  override def lang: Lang = msg.lang

  override def apply(key: String, args: Any*): String =
    if (msg.isDefinedAt(key))
      msg.apply(key, args: _*)
    else throw new AssertionError(s"Message Key is not configured for {$key}")

  override def apply(keys: Seq[String], args: Any*): String =
    if (keys.exists(key => !msg.isDefinedAt(key)))
      msg.apply(keys, args)
    else throw new AssertionError(s"Message Key is not configured for {$keys}")

  override def translate(key: String, args: Seq[Any]): Option[String] = msg.translate(key, args)

  override def isDefinedAt(key: String): Boolean = msg.isDefinedAt(key)
}
