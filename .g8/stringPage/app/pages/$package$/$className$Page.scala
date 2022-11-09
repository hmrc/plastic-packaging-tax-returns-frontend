package pages.$package$

import play.api.libs.json.JsPath

case object $className$Page extends QuestionPage[String] {
  
  override def path: JsPath = JsPath \ toString
  
  override def toString: String = "$className;format="decap"$"
}
