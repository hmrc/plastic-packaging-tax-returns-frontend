/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package base.utils

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.{MappingBuilder, WireMock}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar

trait WiremockTestServer
    extends AnyWordSpec with Matchers with MockitoSugar with BeforeAndAfterAll {

  val wireHost = "localhost"

  lazy val wirePort       = wireMockServer.port()
  private val wireMockServer = new WireMockServer(0)

  protected def stubFor(mappingBuilder: MappingBuilder): StubMapping =
    wireMockServer.stubFor(mappingBuilder)

  protected def startWireMockServer = {
    if(!wireMockServer.isRunning) wireMockServer.start

    WireMock.configureFor(wireHost, wirePort)
  }

  protected def stopWireMockServer = wireMockServer.stop()

  protected def resetWireMockServer = wireMockServer.resetAll()
}
