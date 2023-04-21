package models.returns

import models.UserAnswers
import org.mockito.ArgumentMatchersSugar.any
import org.mockito.MockitoSugar
import org.mockito.integrations.scalatest.ResetMocksAfterEachTest
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import pages.QuestionPage
import play.api.data.Form
import play.api.libs.json.JsPath
import queries.Gettable

class CreditsAnswerSpec extends PlaySpec 
  with BeforeAndAfterEach with MockitoSugar with ResetMocksAfterEachTest {

  val userAnswers = mock[UserAnswers]
  val yesNoForm = mock[Form[Boolean]]("unfilled-yes-no-form")
  val weightForm = mock[Form[Long]]("unfilled-weight-form")
  val filledYesNoForm = mock[Form[Boolean]]("filled-yes-no-form")
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
      CreditsAnswer(false, None).weight mustBe 0L
      CreditsAnswer(false, Some(1L)).weight mustBe 0L
      CreditsAnswer(true, None).weight mustBe 0L
      CreditsAnswer(true, Some(1L)).weight mustBe 1L
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
        newAnswer.asTuple mustBe true -> 2L
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
    
    "fill a yes-no form" when {
      "user has a previous answer" in {
        when(userAnswers.get(any[Gettable[CreditsAnswer]]) (any)) thenReturn Some(CreditsAnswer(true, None))
        CreditsAnswer.fillFormYesNo(userAnswers, TestPage(), yesNoForm) mustBe filledYesNoForm
        verify(userAnswers).get(TestPage())
        verify(yesNoForm).fill(value = true)
      }
      "there is no previous answer" in {
        when(userAnswers.get(any[Gettable[CreditsAnswer]]) (any)) thenReturn None
        CreditsAnswer.fillFormYesNo(userAnswers, TestPage(), yesNoForm) mustBe yesNoForm
        verify(userAnswers).get(TestPage())
        verify(yesNoForm, never).fill(any)
      }
    }
    
    "fill a weight form" when {
      "user has a previous answer" in {
        when(userAnswers.get(any[Gettable[CreditsAnswer]]) (any)) thenReturn Some(CreditsAnswer(true, Some(314L)))
        CreditsAnswer.fillFormWeight(userAnswers, TestPage(), weightForm) mustBe filledWeightForm
        verify(userAnswers).get(TestPage())
        verify(weightForm).fill(value = 314L)
      }
      "there is no previous answer" in {
        when(userAnswers.get(any[Gettable[CreditsAnswer]]) (any)) thenReturn None
        CreditsAnswer.fillFormWeight(userAnswers, TestPage(), weightForm) mustBe weightForm
        verify(userAnswers).get(TestPage())
        verify(weightForm, never).fill(any)
      }
    }
    
    
  }

}
