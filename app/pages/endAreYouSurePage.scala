package pages

import play.api.libs.json.JsPath

case object endAreYouSurePage extends QuestionPage[Boolean] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "endAreYouSure"
}
