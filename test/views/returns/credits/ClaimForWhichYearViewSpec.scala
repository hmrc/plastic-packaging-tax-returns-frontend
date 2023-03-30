package views.returns.credits

import base.ViewSpecBase
import forms.returns.credits.WhatDoYouWantToDoFormProvider
import play.api.mvc.Call
import play.twirl.api.Html
import support.{ViewAssertions, ViewMatchers}
import views.html.returns.credits.ClaimForWhichYearView

class ClaimForWhichYearViewSpec extends ViewSpecBase with ViewAssertions with ViewMatchers{

  val page = inject[ClaimForWhichYearView]
  val form = new WhatDoYouWantToDoFormProvider()()
  private val call = Call("a", "b")

  private def createView: Html = page(form,call)(request, messages)

  "ClaimForWhichYearView" should {
    val view = createView

    "have a title" in { //todo dont have title yet
      view.select("title").text must include( "claim-for-which-year.title")
    }

    "have heading" in {
      view.select("h1").text mustBe "Which year do you want to claim tax back as credit for?"
    }

    "have paragraph content" in {
      val paragraph = view.getElementsByClass("govuk-body").text()

      paragraph must include("Plastic Packaging Tax rates have changed. We need to make sure you claim tax back at the correct rate.")
      paragraph must include("If you need to claim tax back as credit for more than one year you will be given the option to do this later.")
    }

    "have radio options" in {
      view.select(".govuk-radios__item").get(0).text mustBe "1 April 2022 to 31 March 2023"
    }

    "have a save and continue button" in {
      view.getElementsByClass("govuk-button").text() mustBe "Save and continue"
    }

  }

}
