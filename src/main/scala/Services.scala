import Repository._
import com.typesafe.config.ConfigFactory

object Services {
  implicit val config = ConfigFactory.load()
  implicit val accountRepository = new AccountRepository
  implicit val userRepository = new UserRepository
  implicit val sessionRepository = new SessionRepository

}
