package pages

import play.api.libs.json.JsPath

case object $className$Page extends QuestionPage[Long] {
  
  override def path: JsPath = JsPath \ toString
  
  override def toString: String = "$className;format="decap"$"
}
