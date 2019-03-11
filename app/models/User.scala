package models

import com.mohiva.play.silhouette.api.{Identity, LoginInfo}
import play.api.libs.json.{Json, _}

import scala.util.{Failure, Success, Try}

case class User(id: Option[String], loginInfo: LoginInfo, username: String, email: String,
                fullName: String, avatarURL: Option[String], activated: Boolean) extends Identity

object User {

  implicit val loginInfoReader: Reads[LoginInfo] = Json.reads[LoginInfo]
  implicit val loginInfoWriter: Writes[LoginInfo] = Json.writes[LoginInfo]

  implicit val reader: Reads[User] = Json.reads[User]
  implicit val writer: Writes[User] = Json.writes[User]

  implicit object UserWrites extends OWrites[User] {
    def writes(user: User): JsObject =
      user.id match {
        case Some(id) =>
          Json.obj(
            "_id" -> user.id,
            "loginInfo" -> Json.obj(
              "providerID" -> user.loginInfo.providerID,
              "providerKey" -> user.loginInfo.providerKey
            ),
            "username" -> user.username,
            "email" -> user.email,
            "fullName" -> user.fullName,
            "avatarURL" -> user.avatarURL,
            "activated" -> user.activated
          )
        case _ =>
          Json.obj(
            "loginInfo" -> Json.obj(
              "providerID" -> user.loginInfo.providerID,
              "providerKey" -> user.loginInfo.providerKey
            ),
            "username" -> user.username,
            "email" -> user.email,
            "fullName" -> user.fullName,
            "avatarURL" -> user.avatarURL,
            "activated" -> user.activated
          )
      }

    implicit object UserReads extends Reads[User] {
      def reads(json: JsValue): JsResult[User] = json match {
        case user: JsObject =>
          Try {
            val id = (user \ "_id" \ "$oid").asOpt[String]

            val providerId = (user \ "loginInfo" \ "providerID").as[String]
            val providerKey = (user \ "loginInfo" \ "providerKey").as[String]

            val username = (user \ "userName").as[String]
            val email = (user \ "email").as[String]
            val fullName = (user \ "fullName").as[String]
            val avatarURL = (user \ "avatarURL").asOpt[String]
            val activated = (user \ "activated").as[Boolean]

            JsSuccess(
              new User(
                id,
                new LoginInfo(providerId, providerKey),
                username,
                email,
                fullName,
                avatarURL,
                activated
              )
            )
          } match {
            case Success(value) => value
            case Failure(cause) => JsError(cause.getMessage)
          }
        case _ => JsError("expected.jsobject")
      }
    }

  }

}
