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

package repositories

import config.FrontendAppConfig
import models.RichJsObject
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model._
import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}
import play.api.libs.json._
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.{Clock, Instant}
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

final case class Entry(
                        id: String,
                        data: JsObject = JsObject.empty,
                        lastUpdated: Instant = Instant.now()
                      )

object Entry {
  implicit val format: Format[Entry] = Format(
    (
      (JsPath \ "_id").read[String] and
        (JsPath \ "data").read[JsObject] and
        (JsPath \ "lastUpdated").read[Instant](MongoJavatimeFormats.instantFormat)
      )(Entry.apply _),
    (
      (JsPath \ "_id").write[String] and
        (JsPath \ "data").write[JsObject] and
        (JsPath \ "lastUpdated").write[Instant](MongoJavatimeFormats.instantFormat)
      )(unlift(Entry.unapply))
  )
}

@Singleton
class SessionRepository @Inject()(
                                   mongoComponent: MongoComponent,
                                   appConfig: FrontendAppConfig,
                                   clock: Clock
)(implicit ec: ExecutionContext)
    extends PlayMongoRepository[Entry](
      collectionName = "session-cache",
      mongoComponent = mongoComponent,
      domainFormat = Entry.format,
      indexes = Seq(
        IndexModel(Indexes.ascending("lastUpdated"),
                   IndexOptions()
                     .name("lastUpdatedIdx")
                     .expireAfter(appConfig.cacheTtl, TimeUnit.SECONDS)
        )
      ),
      replaceIndexes = true
    ) {

  implicit val instantFormat: Format[Instant] = MongoJavatimeFormats.instantFormat

  private def byId(id: String): Bson = Filters.equal("_id", id)

  def keepAlive(id: String): Future[Boolean] =
    collection
      .updateOne(filter = byId(id), update = Updates.set("lastUpdated", Instant.now(clock)))
      .toFuture
      .map(_ => true)

  private def get(id: String): Future[Option[Entry]] =
    collection.find(byId(id)).headOption

  def get[A](id: String, path: JsPath)(implicit rds: Reads[A]): Future[Option[A]] =
    get(id).map{_.flatMap{ entry =>
        Reads.optionNoError(Reads.at(path)(rds)).reads(entry.data).getOrElse(None)
    }}

  private def set(entry: Entry): Future[Boolean] =
    collection
      .replaceOne(filter = byId(entry.id),
                  replacement = entry,
                  options = ReplaceOptions().upsert(true)
      )
      .toFuture
      .map(_ => true)

  def set[A](id: String, path: JsPath, value: A)(implicit writes: Writes[A]): Future[Boolean] =
      get(id).map{_.fold(JsObject.empty)(_.data)}.flatMap {
        _.setObject(path, Json.toJson(value)) match {
            case JsSuccess(jsValue, _) => set(Entry(id, jsValue))
            case JsError(errors) => Future.failed(JsResultException(errors))
          }
      }

}

object SessionRepository {

  object Paths {
    val AmendChargeRef: JsPath = JsPath \ "AmendChargeRef"
    val ReturnChargeRef: JsPath = JsPath \ "ReturnChargeRef"
    val TaxReturnObligation: JsPath = JsPath \ "TaxReturnObligation"
    val SubscriptionIsActive: JsPath = JsPath \ "SubscriptionIsActive"
    val AgentSelectedPPTRef: JsPath = JsPath \ "AgentSelectedPPTRef"
  }

}
