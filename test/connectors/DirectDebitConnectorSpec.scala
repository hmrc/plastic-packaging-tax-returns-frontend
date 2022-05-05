package connectors

import base.utils.ConnectorISpec
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import play.api.http.Status.OK
import play.api.test.Helpers.{await, status}

class DirectDebitConnectorSpec extends ConnectorISpec {

  lazy val connector: DirectDebitConnector = app.injector.instanceOf[DirectDebitConnector]

  "getDirectDebitMandate" should {
    "return 200" in {
      stubEndPointForDirectDebit(200, "123", "/blah")

      val res = await(connector.getDirectDebitMandate("123"))

      res mustBe "/blah"
    }
  }

  private def stubEndPointForDirectDebit
  (
    status: Int,
    pptReference: String,
    body: String = ""
  ) =
    stubFor(
      WireMock.get(s"/direct-debit/$pptReference")
        .willReturn(
          aResponse()
            .withStatus(status)
            .withBody(body)
        )
    )

}
