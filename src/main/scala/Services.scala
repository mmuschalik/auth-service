import Repository._

object Services {
  implicit val accountRepository = new AccountRepository
  implicit val userRepository = new UserRepository
  implicit val sessionRepository = new SessionRepository
}
