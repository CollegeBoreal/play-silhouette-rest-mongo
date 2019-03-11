package models

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class EmailCredential(email: String, password: String)

object EmailCredential {
  implicit val restFormat: OFormat[EmailCredential] = (
    (__ \ "email").format[String] ~ (__ \ "password").format[String]
    ) (EmailCredential.apply, unlift(EmailCredential.unapply))
}
