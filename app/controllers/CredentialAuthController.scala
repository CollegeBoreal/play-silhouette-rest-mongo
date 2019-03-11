package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.{Clock, Credentials, PasswordHasherRegistry}
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import com.mohiva.play.silhouette.impl.providers._
import daos.UserDAO
import io.swagger.annotations.{Api, ApiImplicitParam, ApiImplicitParams, ApiOperation}
import models.EmailCredential
import models.Token
import play.api.Configuration
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{Json, OFormat}
import play.api.mvc.{AbstractController, Action, ControllerComponents}
import utils.auth.DefaultEnv

import scala.concurrent.{ExecutionContext, Future}

@Api(value = "Authentication")
class CredentialAuthController @Inject()(components: ControllerComponents,
                                          userService: UserDAO,
                                          configuration: Configuration,
                                          silhouette: Silhouette[DefaultEnv],
                                          clock: Clock,
                                          credentialsProvider: CredentialsProvider,
                                          authInfoRepository: AuthInfoRepository,
                                          passwordHasherRegistry: PasswordHasherRegistry,
                                          messagesApi: MessagesApi)
                                         (implicit ex: ExecutionContext) extends AbstractController(components) with I18nSupport {

  implicit val emailCredentialFormat: OFormat[EmailCredential] = EmailCredential.restFormat

  @ApiOperation(value = "Get authentication token", response = classOf[Token])
  @ApiImplicitParams(
    Array(
      new ApiImplicitParam(
        value = "EmailCredential",
        required = true,
        dataType = "models.EmailCredential",
        paramType = "body"
      )
    )
  )
  def authenticate: Action[EmailCredential] = Action.async(parse.json[EmailCredential]) { implicit request =>
    val credentials = Credentials(request.body.email, request.body.password)
    credentialsProvider
      .authenticate(credentials)
      .flatMap { loginInfo =>
        userService.retrieve(loginInfo).flatMap {
          case Some(user) if !user.activated =>
            Future.failed(new IdentityNotFoundException("Couldn't find user"))
          case Some(user) =>
            val config = configuration.underlying
            silhouette.env.authenticatorService
              .create(loginInfo)
              .map {
                case authenticator => authenticator
              }
              .flatMap { authenticator =>
                silhouette.env.eventBus.publish(LoginEvent(user, request))
                silhouette.env.authenticatorService
                  .init(authenticator)
                  .flatMap { token =>
                    silhouette.env.authenticatorService
                      .embed(
                        token,
                        Ok(
                          Json.toJson(
                            Token(
                              token,
                              expiresOn = authenticator.expirationDateTime
                            )
                          )
                        )
                      )
                  }
              }
          case None =>
            Future.failed(new IdentityNotFoundException("Couldn't find user"))
        }
      }
      .recover {
        case _: ProviderException =>
          Forbidden
      }
  }
}
