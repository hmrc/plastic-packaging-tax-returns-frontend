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

package connectors

import base.utils.ConnectorISpec

class DirectDebitConnectorSpec extends ConnectorISpec {

  lazy val connector: DirectDebitConnector = app.injector.instanceOf[DirectDebitConnector]

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    stopWireMockServer
  }

  override protected def afterAll(): Unit = {
    stopWireMockServer
    super.afterAll()
  }

  //TODO: Reimplement unit tests for DD

}
