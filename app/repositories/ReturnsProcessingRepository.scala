/*
 * Copyright 2025 HM Revenue & Customs
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
import org.mongodb.scala.model.{Filters, IndexModel, IndexOptions, Indexes, ReplaceOptions}
import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}
import play.api.libs.json._
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.Instant
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

sealed trait ProcessingStatus
object ProcessingStatus {
  case object Processing       extends ProcessingStatus
  case object AlreadySubmitted extends ProcessingStatus
  case object Complete         extends ProcessingStatus
  case object Failed           extends ProcessingStatus

  import play.api.libs.json._
  implicit val format: Format[ProcessingStatus] = new Format[ProcessingStatus] {
    override def reads(json: JsValue): JsResult[ProcessingStatus] = json match {
      case JsString("Processing")       => JsSuccess(Processing)
      case JsString("AlreadySubmitted") => JsSuccess(AlreadySubmitted)
      case JsString("Complete")         => JsSuccess(Complete)
      case JsString("Failed")           => JsSuccess(Failed)
      case _                            => JsError("Invalid ProcessingStatus")
    }

    override def writes(o: ProcessingStatus): JsValue = o match {
      case Processing       => JsString("Processing")
      case AlreadySubmitted => JsString("AlreadySubmitted")
      case Complete         => JsString("Complete")
      case Failed           => JsString("Failed")
    }
  }
}

final case class ProcessingEntry(
  id: String,
  status: ProcessingStatus = ProcessingStatus.Processing,
  message: Option[String] = None,
  lastUpdated: Instant = Instant.now()
)

object ProcessingEntry {
  implicit val format: Format[ProcessingEntry] = Format(
    (
      (JsPath \ "_id").read[String] and
        (JsPath \ "status").read[ProcessingStatus](ProcessingStatus.format) and
        (JsPath \ "message").readNullable[String] and
        (JsPath \ "lastUpdated").read[Instant](MongoJavatimeFormats.instantFormat)
    )(ProcessingEntry.apply _),
    (
      (JsPath \ "_id").write[String] and
        (JsPath \ "status").write[ProcessingStatus](ProcessingStatus.format) and
        (JsPath \ "message").writeNullable[String] and
        (JsPath \ "lastUpdated").write[Instant](MongoJavatimeFormats.instantFormat)
    )(unlift(ProcessingEntry.unapply))
  )
}

@Singleton
class ReturnsProcessingRepository @Inject() (
  mongoComponent: MongoComponent,
  appConfig: FrontendAppConfig
)(implicit ec: ExecutionContext)
    extends PlayMongoRepository[ProcessingEntry](
      collectionName = "returns-processing",
      mongoComponent = mongoComponent,
      domainFormat = ProcessingEntry.format,
      indexes = Seq(
        IndexModel(
          Indexes.ascending("lastUpdated"),
          IndexOptions()
            .name("lastUpdatedIdx")
            .expireAfter(appConfig.cacheTtl, TimeUnit.SECONDS)
        )
      ),
      replaceIndexes = true
    ) {

  def set(entry: ProcessingEntry): Future[Unit] =
    collection
      .replaceOne(filter = Filters.equal("_id", entry.id), replacement = entry, options = ReplaceOptions().upsert(true))
      .toFuture()
      .map(_ => ())

  def get(id: String): Future[Option[ProcessingEntry]] =
    collection.find(Filters.equal("_id", id)).headOption()

}
