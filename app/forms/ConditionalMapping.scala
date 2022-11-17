package forms

import play.api.data.{FormError, Mapping}
import play.api.data.validation.Constraint
import uk.gov.voa.play.form.Condition

case class ConditionalMapping[T](
  condition: Condition,
  wrapped: Mapping[T],
  opWrapped: Mapping[T],
  constraints: Seq[Constraint[T]] = Nil,
  keys: Set[String] = Set()) extends Mapping[T] {

  override val format: Option[(String, Seq[Any])] = wrapped.format

  val key = wrapped.key

  override def verifying(addConstraints: Constraint[T]*): Mapping[T] =
    this.copy(constraints = constraints ++ addConstraints.toSeq)

  def bind(data: Map[String, String]): Either[Seq[FormError], T] =
    if (condition(data)) wrapped.bind(data)
    else opWrapped.bind(data)

  def unbind(value: T): Map[String, String] =
    wrapped.unbind(value) ++
    opWrapped.unbind(value)

  def unbindAndValidate(value: T): (Map[String, String], Seq[FormError]) = {
    val w = wrapped.unbindAndValidate(value)
    val op = opWrapped.unbindAndValidate(value)
    (w._1 ++ op._1, w._2 ++ op._2)
  }

  def withPrefix(prefix: String): Mapping[T] =
    copy(wrapped = wrapped.withPrefix(prefix), opWrapped = opWrapped.withPrefix(prefix))

  val mappings: Seq[Mapping[_]] = wrapped.mappings :+ opWrapped :+ this
}
