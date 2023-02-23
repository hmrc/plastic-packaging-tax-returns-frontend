
import models.amends.AmendSummaryRow
import models.returns.{AmendsCalculations, Calculations}
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import viewmodels.PrintLong

val t = Seq("q", "v", "f")

val c = t.drop(1) :+ "g"

val m = t.dropRight(1)
val n = t.last

println(c)

val calculations = AmendsCalculations(
  Calculations(1,2,3,4, true, 5),
  Calculations(1,2,3,4, true, 5)
)

val g = Seq(
  AmendSummaryRow(
    "AmendsCheckYourAnswers.deductionsTotal1",
    calculations.original.deductionsTotal.asKg,
    Some(calculations.amend.deductionsTotal.asKg),
    None
  ),
  AmendSummaryRow(
    "AmendsCheckYourAnswers.deductionsTotal2",
    calculations.original.deductionsTotal.asKg,
    None,
    None
  ),
  AmendSummaryRow(
    "AmendsCheckYourAnswers.deductionsTotal3",
    calculations.original.deductionsTotal.asKg,
    Some(calculations.amend.deductionsTotal.asKg),
    None,
    Some("tis is a test")
  )
)

g.zipWithIndex.map((a,i) => {
  if(i == g.size -1)
})
val y = g.map(row => {
  val u = row.newAnswer.fold("hidden")(Text(_).value)
  println(u)
  val o = row.hiddenText.fold(row.newAnswer.fold("hidden")(Text(_).value))(Text(_).value)
  println(o)
  val h = row.hiddenText.fold(row.newAnswer.fold("hidden")(Text(_).value))(Text(_).value)
  println(h)
  h
}

)

y.foreach(o => println(o))