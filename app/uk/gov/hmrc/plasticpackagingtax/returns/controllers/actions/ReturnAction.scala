package uk.gov.hmrc.plasticpackagingtax.returns.controllers.actions

import com.google.inject.Inject
import play.api.mvc.{ActionBuilder, ActionFunction, ActionTransformer, AnyContent, BodyParser, Request, Result}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.plasticpackagingtax.returns.connectors.ObligationsConnector
import uk.gov.hmrc.plasticpackagingtax.returns.models.obligations.Obligation
import uk.gov.hmrc.plasticpackagingtax.returns.models.request.JourneyRequest
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

case class ReturnRequest[+A](nextReturnToPay: Obligation, request: JourneyRequest[A])

class ReturnAction @Inject()(
                              override val parser: BodyParser[AnyContent],
                              obligations: ObligationsConnector,
                            )(implicit
                              override val executionContext: ExecutionContext
) extends ActionTransformer[JourneyRequest, ReturnRequest]
  with ActionBuilder[JourneyRequest, AnyContent]{

  override protected def transform[A](request: JourneyRequest[A]): Future[ReturnRequest[A]] = {
    implicit val hc: HeaderCarrier =
      HeaderCarrierConverter.fromRequestAndSession(request, request.session)
    obligations.get(request.pptReference).map{obligations =>
      val nextReturnToPay = obligations.nextToReturn.getOrElse(throw new IllegalStateException("No open obligations that need returned."))
      ReturnRequest(nextReturnToPay, request)
    }
  }

}
