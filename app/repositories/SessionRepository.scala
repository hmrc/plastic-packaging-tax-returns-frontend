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

package repositories

import config.FrontendAppConfig
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model._
import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}
import play.api.libs.json.{Format, JsPath, Json}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.{Clock, Instant}
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

final case class Entry(id: String, data: Option[String], lastUpdated: Instant = Instant.now())

object Entry {
  implicit val format: Format[Entry] = Format(
    (
      (JsPath \ "_id").read[String] and
        (JsPath \ "data").readNullable[String] and
        (JsPath \ "lastUpdated").read[Instant](MongoJavatimeFormats.instantFormat)
      )(Entry.apply _),
    (
      (JsPath \ "_id").write[String] and
        (JsPath \ "data").writeNullable[String] and
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

  def get(id: String): Future[Option[Entry]] =
    keepAlive(id).flatMap {
      _ =>
        collection
          .find(byId(id))
          .headOption
    }

  def set(entry: Entry): Future[Boolean] = {

    val updatedAnswers = entry copy (lastUpdated = Instant.now(clock))

    collection
      .replaceOne(filter = byId(updatedAnswers.id),
                  replacement = updatedAnswers,
                  options = ReplaceOptions().upsert(true)
      )
      .toFuture
      .map(_ => true)
  }

  def clear(id: String): Future[Boolean] =
    collection
      .deleteOne(byId(id))
      .toFuture
      .map(_ => true)

}
