package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.AvatarService
import com.mohiva.play.silhouette.api.util.{Clock, PasswordHasherRegistry}
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import daos.UserDAO
import io.swagger.annotations.{Api, ApiImplicitParam, ApiImplicitParams, ApiOperation}
import models.{SignUp, Token, User}
import play.api.Configuration
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.json.{JsError, JsValue, Json, OFormat}
import play.api.libs.mailer.{Email, MailerClient}
import play.api.mvc.{AbstractController, Action, ControllerComponents}
import utils.auth.DefaultEnv
import utils.responses.rest.Bad

import scala.concurrent.{ExecutionContext, Future}

@Api(value = "Registration")
class SignUpController @Inject()(components: ControllerComponents,
                                 userService: UserDAO,
                                 configuration: Configuration,
                                 silhouette: Silhouette[DefaultEnv],
                                 clock: Clock,
                                 credentialsProvider: CredentialsProvider,
                                 authInfoRepository: AuthInfoRepository,
                                 passwordHasherRegistry: PasswordHasherRegistry,
                                 avatarService: AvatarService,
                                 mailerClient: MailerClient,
                                 messagesApi: MessagesApi)
                                (implicit ex: ExecutionContext) extends AbstractController(components) with I18nSupport {

  implicit val signUpFormat: OFormat[SignUp] = Json.format[SignUp]

  @ApiOperation(value = "Register and get authentication token", response = classOf[Token])
  @ApiImplicitParams(
    Array(
      new ApiImplicitParam(
        value = "SignUp",
        required = true,
        dataType = "models.SignUp",
        paramType = "body"
      )
    )
  )
  def signUp: Action[JsValue] = Action.async(parse.json) { implicit request =>
    request.body.validate[SignUp].map { signUp =>
      val loginInfo = LoginInfo(CredentialsProvider.ID, signUp.email)
      userService.retrieve(loginInfo).flatMap {
        case None => /* user not already exists */
          val user = User(None, loginInfo, loginInfo.providerKey, signUp.email, signUp.fullName, None, true)
          // val plainPassword = UUID.randomUUID().toString.replaceAll("-", "")
          val authInfo = passwordHasherRegistry.current.hash(signUp.password)
          for {
            avatar <- avatarService.retrieveURL(signUp.email)
            _ <- userService.save(user.copy(avatarURL = avatar))
            _ <- authInfoRepository.add(loginInfo, authInfo)
            authenticator <- silhouette.env.authenticatorService.create(loginInfo)
            token <- silhouette.env.authenticatorService.init(authenticator)
            result <- silhouette.env.authenticatorService.embed(token,
              Ok(Json.toJson(Token(token = token, expiresOn = authenticator.expirationDateTime)))
            )
          } yield {
            val url = routes.ApplicationController.index().absoluteURL()
            mailerClient.send(Email(
              subject = Messages("email.sign.up.subject"),
              from = Messages("email.from"),
              to = Seq(user.email),
              bodyText = Some(views.txt.emails.signUp(user, url).body),
              bodyHtml = Some(views.html.emails.signUp(user, url).body)
            ))
            silhouette.env.eventBus.publish(SignUpEvent(user, request))
            silhouette.env.eventBus.publish(LoginEvent(user, request))
            result
          }
        case Some(_) => /* user already exists! */
          Future(Conflict(Json.toJson(Bad(message = "user already exists"))))
      }
    }.recoverTotal {
      case error =>
        Future.successful(BadRequest(Json.toJson(Bad(message = JsError.toJson(error)))))
    }
  }
}
