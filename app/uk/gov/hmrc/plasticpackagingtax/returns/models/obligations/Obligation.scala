package uk.gov.hmrc.plasticpackagingtax.returns.models.obligations

import play.api.libs.json.{Json, OFormat}

import java.time.LocalDate

final case class Obligation(
                             fromDate: LocalDate,
                             toDate: LocalDate,
                             dueDate: LocalDate,
                             periodKey: String
                           )

object Obligation {
  implicit val format: OFormat[Obligation] = Json.format[Obligation]
}
