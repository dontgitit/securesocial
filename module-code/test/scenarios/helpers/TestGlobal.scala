package scenarios.helpers

import play.api.Configuration
import play.api.i18n.MessagesApi
import securesocial.core.services.UserService
import securesocial.core.RuntimeEnvironment

/**
 * Created by dverdone on 8/6/15.
 */

case class TestGlobal() extends RuntimeEnvironment.Default {
  type U = DemoUser
  override lazy val userService: UserService[DemoUser] = null
  override lazy val configuration: Configuration = ???
  override lazy val messagesApi: MessagesApi = ???
}
