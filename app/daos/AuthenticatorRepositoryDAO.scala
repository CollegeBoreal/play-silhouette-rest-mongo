package daos

import com.mohiva.play.silhouette.api.repositories.AuthenticatorRepository
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.LoginInfo
import org.joda.time.DateTime
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.play.json._
import reactivemongo.play.json.collection._

import scala.language.postfixOps
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}
import play.api.libs.json.Reads._
import play.api.libs.json.Writes._
import play.api.libs.json._


trait JWTAuthenticatorFormat {

  implicit val jodaDateReads: Reads[DateTime] = JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss'Z'")
  implicit val jodaDateWrites: Writes[DateTime]  = JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss'Z'")

  implicit object FiniteDurationFormat extends Format[FiniteDuration] {
    def reads(json: JsValue): JsResult[FiniteDuration] = LongReads.reads(json).map(_.seconds)

    def writes(o: FiniteDuration): JsValue = LongWrites.writes(o.toSeconds)
  }

  implicit object JWTAuthenticatorWrites extends OWrites[JWTAuthenticator] {
    def writes(authenticator: JWTAuthenticator): JsObject =
      Json.obj(
        "_id" -> authenticator.id,
        "loginInfo" -> authenticator.loginInfo,
        "lastUsedDateTime" -> authenticator.lastUsedDateTime,
        "expirationDateTime" -> authenticator.expirationDateTime,
        "idleTimeout" -> authenticator.idleTimeout
      )
  }

  implicit object JWTAuthenticatorReads extends Reads[JWTAuthenticator] {
    def reads(json: JsValue): JsResult[JWTAuthenticator] = json match {
      case authenticator: JsObject =>
        Try {
          val id = (authenticator \ "_id").as[String]
          val providerId = (authenticator \ "authenticator" \ "loginInfo" \ "providerID").as[String]
          val providerKey = (authenticator \ "authenticator" \ "loginInfo" \ "providerKey").as[String]
          val lastUsedDateTime = (authenticator \ "authenticator" \ "lastUsedDateTime").as[DateTime]
          val expirationDateTime = (authenticator \ "authenticator" \ "expirationDateTime").as[DateTime]
          val idleTimeout = (authenticator \ "authenticator" \ "idleTimeout").asOpt[FiniteDuration]

          JsSuccess(
            new JWTAuthenticator(
              id,
              new LoginInfo(providerId, providerKey),
              lastUsedDateTime,
              expirationDateTime,
              idleTimeout
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

/**
  * Implementation of the authenticator repository which uses the database layer to persist the authenticator.
  */
@Singleton
class AuthenticatorRepositoryDAO @Inject()
  (protected val reactiveMongoApi: ReactiveMongoApi)
  (implicit ec: ExecutionContext)
  extends JWTAuthenticatorFormat
  with AuthenticatorRepository[JWTAuthenticator] {

  final val maxDuration = 12 hours

  /**
    * The data store for the password info.
    */
  lazy val authenticators: Future[JSONCollection] = reactiveMongoApi.database.map(_.collection[JSONCollection]("authenticators"))

  /**
    * Finds the authenticator for the given ID.
    *
    * @param id The authenticator ID.
    * @return The found authenticator or None if no authenticator could be found for the given ID.
    */
  override def find(id: String): Future[Option[JWTAuthenticator]] = {
    authenticators.flatMap(_.find(Json.obj("_id" -> id)).one[JWTAuthenticator])
  }

  /**
    * Adds a new authenticator.
    *
    * @param authenticator The authenticator to add.
    * @return The added authenticator.
    */
  override def add(authenticator: JWTAuthenticator): Future[JWTAuthenticator] = {
    val passInfo = Json.obj("_id" -> authenticator.id, "authenticator" -> authenticator, "duration" -> maxDuration)
    authenticators.flatMap(_.insert(passInfo)).flatMap(_ => Future(authenticator))
  }

  /**
    * Updates an already existing authenticator.
    *
    * @param authenticator The authenticator to update.
    * @return The updated authenticator.
    */
  override def update(authenticator: JWTAuthenticator): Future[JWTAuthenticator] = {
    val passInfo = Json.obj("_id" -> authenticator.id, "authenticator" -> authenticator, "duration" -> maxDuration)
    authenticators.flatMap(_.update(Json.obj("_id" -> authenticator.id), passInfo)).flatMap(_ => Future(authenticator))
  }

  /**
    * Removes the authenticator for the given ID.
    *
    * @param id The authenticator ID.
    * @return An empty future.
    */
  override def remove(id: String): Future[Unit] =
    authenticators.flatMap(_.remove(Json.obj("_id" -> id))).flatMap(_ => Future.successful(()))
}