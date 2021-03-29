package uk.gov.hmrc.plasticpackagingtax.returns.config

import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

class AppConfigTest extends AnyWordSpec with Matchers with MockitoSugar {

  private val validConfig: Config =
    ConfigFactory.parseString(
      """
        |urls.feedback.authenticatedLink="http://localhost:9250/contact/beta-feedback"
        |urls.feedback.unauthenticatedLink="http://localhost:9250/contact/beta-feedback-unauthenticated"
      """.stripMargin
    )


  private val validServicesConfiguration = Configuration(validConfig)
  private val validAppConfig: AppConfig  = appConfig(validServicesConfiguration)

  private def appConfig(conf: Configuration) =
    new AppConfig(conf, servicesConfig(conf))

  private def servicesConfig(conf: Configuration) = new ServicesConfig(conf)

  "The config" should {

    "have 'authenticatedFeedbackUrl' defined" in {
      validAppConfig.authenticatedFeedbackUrl() must be(
        "http://localhost:9250/contact/beta-feedback?service=plastic-packaging-tax"
      )
    }

    "have 'unauthenticatedFeedbackUrl' defined" in {
      validAppConfig.unauthenticatedFeedbackUrl() must be(
        "http://localhost:9250/contact/beta-feedback-unauthenticated?service=plastic-packaging-tax"
      )
    }
  }
}
