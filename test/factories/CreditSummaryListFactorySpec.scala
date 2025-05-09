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

package factories

import models.returns.CreditsAnswer
import models.{TaxablePlastic, UserAnswers}
import org.mockito.ArgumentMatchers.{eq => meq}
import org.mockito.ArgumentMatchersSugar.any
import org.mockito.MockitoSugar.{mock, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import pages.returns.credits.{ConvertedCreditsPage, ExportedCreditsPage}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text

class CreditSummaryListFactorySpec extends PlaySpec with BeforeAndAfterEach {

  private val sut         = new CreditSummaryListFactory()
  private val userAnswers = mock[UserAnswers]
  private val messages    = mock[Messages]

  override protected def beforeEach(): Unit =
    super.beforeEach()

  "createSummaryList" should {
    "a summary list containing credit details" in {
      when(messages.apply(meq("confirmPackagingCredit.taxRate"))).thenReturn("Tax Rate")
      when(messages.apply(meq("confirmPackagingCredit.exported.answer"))).thenReturn("exported")
      when(messages.apply(meq("confirmPackagingCredit.exported.weight"))).thenReturn("exported weight")
      when(messages.apply(meq("confirmPackagingCredit.converted.answer"))).thenReturn("converted")
      when(messages.apply(meq("confirmPackagingCredit.converted.weight"))).thenReturn("converted weight")
      when(messages.apply(meq("confirmPackagingCredit.totalPlastic"))).thenReturn("total plastic")
      when(messages.apply(meq("confirmPackagingCredit.creditAmount"))).thenReturn("credit amount")
      when(userAnswers.get(meq(ExportedCreditsPage("a-key")))(any)).thenReturn(
        Some(CreditsAnswer.answerWeightWith(10L))
      )
      when(userAnswers.get(meq(ConvertedCreditsPage("a-key")))(any)).thenReturn(
        Some(CreditsAnswer.answerWeightWith(20L))
      )

      val res = sut.createSummaryList(TaxablePlastic(1, 2, 0.30), "a-key", userAnswers)(messages)

      res(0).key.content.asInstanceOf[Text].value mustBe "Tax Rate"
      res(1).key.content.asInstanceOf[Text].value mustBe "exported"
      res(2).key.content.asInstanceOf[Text].value mustBe "exported weight"
      res(3).key.content.asInstanceOf[Text].value mustBe "converted"
      res(4).key.content.asInstanceOf[Text].value mustBe "converted weight"
      res(5).key.content.asInstanceOf[Text].value mustBe "total plastic"
      res(6).key.content.asInstanceOf[Text].value mustBe "credit amount"
    }
  }
}
