package uk.gov.hmrc.plasticpackagingtax.returns.models.obligations

import play.api.libs.json.{Json, OFormat}

final case class PPTObligations(
                                 nextObligation: Option[Obligation],
                                 oldestOverdueObligation: Option[Obligation],
                                 overdueObligationCount: Int,
                                 isNextObligationDue: Boolean,
                                 displaySubmitReturnsLink: Boolean
                               ) {

  //todo confirm this? what happens if there isnt one? illegal state?
  def nextToReturn: Option[Obligation] = oldestOverdueObligation.orElse {
    if (isNextObligationDue) nextObligation else None
  }

}

object PPTObligations {
  implicit val format: OFormat[PPTObligations] = Json.format[PPTObligations]
}
