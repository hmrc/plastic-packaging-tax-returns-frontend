package connectors

import com.kenshoo.play.metrics.Metrics
import config.FrontendAppConfig
import play.api.Logging
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DirectDebitConnector @Inject()
(
  httpClient: HttpClient,
  appConfig: FrontendAppConfig,
  metrics: Metrics
)(implicit ec: ExecutionContext)
  extends Logging {
  def getDirectDebitMandate(pptReferenceNumber: String)(implicit hc: HeaderCarrier): Future[String] = {
    val timer = metrics.defaultRegistry.timer("ppt.financials.open.get.timer").time()
    httpClient.GET[String](pptReferenceNumber)
      .map {
        response =>
          logger.info(s"Retrieved direct debit mandate for ppt reference number [$pptReferenceNumber]")
          response
      }
      .andThen { case _ => timer.stop() }
  }
}

