package uk.gov.hmrc.plasticpackagingtax.returns.controllers.returns

import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.plasticpackagingtax.returns.connectors.{ObligationsConnector, TaxReturnsConnector}
import uk.gov.hmrc.plasticpackagingtax.returns.controllers.actions.{AuthAction, ReturnAction, ReturnRequest}
import uk.gov.hmrc.plasticpackagingtax.returns.models.domain.Cacheable
import uk.gov.hmrc.plasticpackagingtax.returns.models.request.{JourneyAction, JourneyRequest}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class StartDateReturnController @Inject() (
                                            authenticate: AuthAction,
                                            journeyAction: JourneyAction,
                                            returns: ReturnAction,
                                            mcc: MessagesControllerComponents
                                          )(implicit ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport {

  def displayPage(): Action[AnyContent] = (authenticate andThen journeyAction andThen returns) {
    implicit request: ReturnRequest[AnyContent] =>
        NotImplemented(request.nextReturnToPay.toString)
    }

  def submit(): Action[AnyContent] =
    (authenticate andThen journeyAction) { implicit request: JourneyRequest[AnyContent] =>
      NotImplemented("uh oh")
  }
}