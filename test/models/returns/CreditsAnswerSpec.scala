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

package models.returns

import models.UserAnswers
import org.mockito.ArgumentMatchersSugar.any
import org.mockito.MockitoSugar
import org.mockito.scalatest.ResetMocksAfterEachTest
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import pages.QuestionPage
import play.api.data.Form
import play.api.libs.json.JsPath

class CreditsAnswerSpec extends PlaySpec with BeforeAndAfterEach with MockitoSugar with ResetMocksAfterEachTest {

  val userAnswers      = mock[UserAnswers]
  val yesNoForm        = mock[Form[Boolean]]("unfilled-yes-no-form")
  val weightForm       = mock[Form[Long]]("unfilled-weight-form")
  val filledYesNoForm  = mock[Form[Boolean]]("filled-yes-no-form")
  val filledWeightForm = mock[Form[Long]]("filled-weight-form")

  case class TestPage() extends QuestionPage[CreditsAnswer] {
    override def path: JsPath = JsPath \ "test-path"
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    when(yesNoForm.fill(any)) thenReturn filledYesNoForm
    when(weightForm.fill(any)) thenReturn filledWeightForm
  }

  "it" should {

    "look up the correct message key" in {
      CreditsAnswer(false, None).yesNoMsgKey mustBe "site.no"
      CreditsAnswer(true, None).yesNoMsgKey mustBe "site.yes"
    }

    "always give the inferred weight value" in {
      CreditsAnswer(false, None).weightValue mustBe 0L
      CreditsAnswer(false, Some(1L)).weightValue mustBe 0L
      CreditsAnswer(true, None).weightValue mustBe 0L
      CreditsAnswer(true, Some(1L)).weightValue mustBe 1L
    }

    "change its answer from yes to no" when {

      // Rule - if user answers no, then implied weight is zero

      "current weight is None" in {
        val newAnswer = CreditsAnswer(true, None).changeYesNoTo(isYes = false)
        newAnswer.asTuple mustEqual false -> 0L
      }
      "current weight is something" in {
        val newAnswer = CreditsAnswer(true, Some(2L)).changeYesNoTo(isYes = false)
        newAnswer.asTuple mustBe false -> 0L
      }
    }

    "change its answer from no to yes" when {
      "current weight is None" in {
        val newAnswer = CreditsAnswer(false, None).changeYesNoTo(isYes = true)
        newAnswer.asTuple mustBe true -> 0L
      }
      "current weight is something" in {
        val newAnswer = CreditsAnswer(false, Some(2L)).changeYesNoTo(isYes = true)
        newAnswer.asTuple mustBe true -> 0L
      }
      "weight should be zero after changing answer to no, then to yes again" in {
        val firstAnswer  = CreditsAnswer.answerWeightWith(11L)
        val secondAnswer = firstAnswer.changeYesNoTo(false)
        val thirdAnswer  = secondAnswer.changeYesNoTo(true)
        thirdAnswer.weightValue mustBe 0L
        thirdAnswer.weightForForm mustBe Some(0L)
      }
    }

    "defer changing yes-no answer" when {

      "changing to no" in {
        val changeToNoFrom: Option[CreditsAnswer] => CreditsAnswer = CreditsAnswer.changeYesNoTo(isYes = false)

        withClue("no previous answer") {
          changeToNoFrom(None).asTuple mustBe false -> 0L
        }
        withClue("previous answer was no") {
          val previousAnswer = Some(CreditsAnswer(false, None))
          changeToNoFrom(previousAnswer).asTuple mustBe false -> 0L
        }
        withClue("previous answer was yes, 10") {
          val previousAnswer = Some(CreditsAnswer(true, Some(10L)))
          changeToNoFrom(previousAnswer).asTuple mustBe false -> 0L
        }
      }

      "changing to yes" in {
        val changeToYesFrom: Option[CreditsAnswer] => CreditsAnswer = CreditsAnswer.changeYesNoTo(isYes = true)

        withClue("no previous answer") {
          changeToYesFrom(None).asTuple mustBe true -> 0L
        }
        withClue("previous answer was no") {
          val previousAnswer = Some(CreditsAnswer(false, None))
          changeToYesFrom(previousAnswer).asTuple mustBe true -> 0L
        }
        withClue("previous answer was yes, 10") {
          val previousAnswer = Some(CreditsAnswer(true, Some(10L)))
          changeToYesFrom(previousAnswer).asTuple mustBe true -> 10L
        }
      }

    }

    "create a CreditAnswer" when {
      "the answer is 'no-claim'" in {
        CreditsAnswer.noClaim.asTuple mustBe false -> 0L
      }
      "the answer is 11" in {
        CreditsAnswer.answerWeightWith(11L).asTuple mustBe true -> 11L
      }
    }

    "fill a yes-no form" in {
      val creditsAnswer = mock[CreditsAnswer]
      when(creditsAnswer.yesNo) thenReturn true
      CreditsAnswer.fillFormYesNo(creditsAnswer) mustBe Some(true)
      verify(creditsAnswer).yesNo
    }

    "fill a weight form" in {
      val creditsAnswer = mock[CreditsAnswer]
      when(creditsAnswer.weightForForm) thenReturn Some(7L)
      CreditsAnswer.fillFormWeight(creditsAnswer) mustBe Some(7L)
      verify(creditsAnswer).weightForForm
    }

    "provide a weight value for display" in {
      CreditsAnswer(false, None).weightForForm mustBe None
      CreditsAnswer(true, None).weightForForm mustBe None
      CreditsAnswer(false, Some(7L)).weightForForm mustBe Some(0L)
      CreditsAnswer(true, Some(7L)).weightForForm mustBe Some(7L)
    }

  }
}
