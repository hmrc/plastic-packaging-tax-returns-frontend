package pages

import play.api.libs.json.JsPath

case object ConfirmAmendReturnPage extends QuestionPage[Boolean] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "confirmAmendReturn"
}
