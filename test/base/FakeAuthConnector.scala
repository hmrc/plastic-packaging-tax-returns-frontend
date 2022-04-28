/*
 * Copyright 2022 HM Revenue & Customs
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

package base

import com.google.inject.Inject
import models.SignedInUser
import support.AuthHelper
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.Retrieval
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

object FakeAuthConnector {

  class FakeFailingAuthConnector @Inject() (exceptionToReturn: Throwable) extends AuthConnector {
    val serviceUrl: String = ""

    override def authorise[A](predicate: Predicate, retrieval: Retrieval[A])(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext
    ): Future[A] =
      Future.failed(exceptionToReturn)

  }

  class FakeSuccessfulAuthConnector(user: SignedInUser) extends AuthConnector {

    var predicate: Option[Predicate] = None

    override def authorise[A](predicate: Predicate, retrieval: Retrieval[A])(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext
    ): Future[A] = {
      this.predicate = Some(predicate)

      AuthHelper.createCredentialForUser(user).asInstanceOf[Future[A]]
    }

  }

  def createFailingAuthConnector(exceptionToReturn: Throwable): FakeFailingAuthConnector =
    new FakeFailingAuthConnector(exceptionToReturn)

  def createSuccessAuthConnector(user: SignedInUser): FakeSuccessfulAuthConnector =
    new FakeSuccessfulAuthConnector(user)

}
