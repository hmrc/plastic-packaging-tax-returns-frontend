package uk.gov.hmrc.plasticpackagingtax.returns.connectors

import com.kenshoo.play.metrics.Metrics
import play.api.Logging
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import uk.gov.hmrc.plasticpackagingtax.returns.config.AppConfig
import uk.gov.hmrc.plasticpackagingtax.returns.models.obligations.PPTObligations

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ObligationsConnector @Inject() (
                                       httpClient: HttpClient,
                                       appConfig: AppConfig,
                                       metrics: Metrics
                                     )(implicit ec: ExecutionContext) extends Logging {

  def get(
           pptReferenceNumber: String
         )(implicit hc: HeaderCarrier): Future[PPTObligations] = {
    val timer = metrics.defaultRegistry.timer("ppt.obligations.open.get.timer").time()
    httpClient.GET[PPTObligations](appConfig.pptObligationUrl(pptReferenceNumber))
      .map {
        response =>
          logger.info(s"Retrieved open obligations for ppt reference number [$pptReferenceNumber]")
          response
      }
      .andThen { case _ => timer.stop() }
      .recover {
        case exception: Exception =>
          throw DownstreamServiceError(
            s"Failed to retrieve open obligations for PPTReference: [$pptReferenceNumber], error: [${exception.getMessage}]",
            exception
          )
      }
  }

}
