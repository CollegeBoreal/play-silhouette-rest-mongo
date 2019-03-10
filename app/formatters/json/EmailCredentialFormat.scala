package formatters.json

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class EmailCredentials(email: String, password: String)

object EmailCredentialFormat {
  implicit val restFormat: OFormat[EmailCredentials] = (
    (__ \ "email").format[String] ~ (__ \ "password").format[String]
    ) (EmailCredentials.apply, unlift(EmailCredentials.unapply))
}

