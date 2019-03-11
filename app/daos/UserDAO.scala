package daos

import javax.inject.Inject

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.services.IdentityService
import models.User
import play.api.libs.json.Json
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.commands.WriteResult
import reactivemongo.play.json._
import reactivemongo.play.json.collection._

import scala.concurrent.{ExecutionContext, Future}

class UserDAO @Inject()(val reactiveMongoApi: ReactiveMongoApi)(implicit ex: ExecutionContext) extends IdentityService[User] {

  lazy val users = reactiveMongoApi.database.map(_.collection[JSONCollection]("users"))

  override def retrieve(loginInfo: LoginInfo): Future[Option[User]] =
    users.flatMap(_.find(Json.obj("username" -> loginInfo.providerKey)).one[User])

  def save(user: User): Future[WriteResult] =
    users.flatMap(_.insert(user))
}
