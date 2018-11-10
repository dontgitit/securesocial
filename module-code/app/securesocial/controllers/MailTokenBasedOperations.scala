/**
 * Copyright 2012-2014 Jorge Aliss (jaliss at gmail dot com) - twitter: @jaliss
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package securesocial.controllers

import java.util.UUID

import org.joda.time.DateTime
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.i18n.Messages
import play.api.mvc.{ RequestHeader, Result }
import securesocial.core.{ RuntimeEnvironment, SecureSocial }
import securesocial.core.providers.MailToken

import scala.concurrent.Future

object MailTokenBasedOperations {
  val TokenDurationKey = "securesocial.userpass.tokenDuration"

  /**
   * Creates a token for mail based operations
   *
   * @param email the email address
   * @param isSignUp a boolean indicating if the token is used for a signup or password reset operation
   * @param tokenDurationMinutes duration, in minutes, indicating how long the token should remain valid
   * @return a MailToken instance
   */
  def createToken(email: String, isSignUp: Boolean, tokenDurationMinutes: Int): MailToken = {
    val now = DateTime.now
    MailToken(
      UUID.randomUUID().toString, email.toLowerCase, now, now.plusMinutes(tokenDurationMinutes), isSignUp = isSignUp)
  }

  /**
   * Creates a token for mail based operations
   * Its duration will be derived from a configuration setting in the current environment
   *
   * @param email the email address
   * @param isSignUp a boolean indicating if the token is used for a signup or password reset operation
   * @return a MailToken instance
   */
  def createToken(email: String, isSignUp: Boolean)(implicit env: RuntimeEnvironment): MailToken = {
    val TokenDuration = env.configuration.get[Int](TokenDurationKey)
    createToken(email, isSignUp, TokenDuration)
  }
}

/**
 * The base controller for password reset and password change operations
 *
 */
abstract class MailTokenBasedOperations extends SecureSocial {
  val Success = "success"
  val Error = "error"
  val Email = "email"

  val startForm = Form(
    Email -> email.verifying(nonEmpty))

  /**
   * Creates a token for mail based operations
   *
   * @param email the email address
   * @param isSignUp a boolean indicating if the token is used for a signup or password reset operation
   * @return a MailToken instance
   */
  def createToken(email: String, isSignUp: Boolean): Future[MailToken] = {
    Future.successful(MailTokenBasedOperations.createToken(email, isSignUp))
  }

  /**
   * Helper method to execute actions where a token needs to be retrieved from
   * the backing store
   *
   * @param token the token id
   * @param isSignUp a boolean indicating if the token is used for a signup or password reset operation
   * @param f the function that gets invoked if the token exists
   * @param request the current request
   * @return the action result
   */
  protected def executeForToken(token: String, isSignUp: Boolean,
    f: MailToken => Future[Result])(implicit request: RequestHeader): Future[Result] =
    {
      env.userService.findToken(token).flatMap {
        case Some(t) if !t.isExpired && t.isSignUp == isSignUp => f(t)
        case _ =>
          val to = if (isSignUp) env.routes.startSignUpUrl else env.routes.startResetPasswordUrl
          Future.successful(Redirect(to).flashing(Error -> Messages(BaseRegistration.InvalidLink)))
      }
    }

  /**
   * The result sent after the start page is handled
   *
   * @param request the current request
   * @return the action result
   */
  protected def handleStartResult()(implicit request: RequestHeader): Result = Redirect(env.routes.loginPageUrl)

  /**
   * The result sent after the operation has been completed by the user
   *
   * @param request the current request
   * @return the action result
   */
  protected def confirmationResult()(implicit request: RequestHeader): Result = Redirect(env.routes.loginPageUrl)
}
