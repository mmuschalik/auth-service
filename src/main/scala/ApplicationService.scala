import Model._
import Repository.AccountRepository

import scala.concurrent.Future
import scala.async.Async.{async, await}
import scala.concurrent.ExecutionContext.Implicits.global
import Repository._
import Model._
import akka.http.scaladsl.server.directives.Credentials
import org.apache.commons.codec.binary.Base64
import org.apache.commons.codec.digest.DigestUtils



class ApplicationService(
  implicit val accountRepository: AccountRepositoryTrait,
  implicit val userRepository: UserRepositoryTrait,
  implicit val sessionRepository: SessionRepository) {


  private val salt = "gentleman-jack"

  def registerAccount(acc: RegisterAccount): Future[Option[Account]] = async {
    val account = await {accountRepository.create(Account(0, acc.name, false))}

    if(account.isDefined) {
      val created = await {

        addUser(AddUser(acc.userName, acc.email, acc.password),Session(Token(""), User(0, account.get.id, acc.userName, acc.email, acc.password, Map()), 0))
      }
    }
    account
  }

  def activateAccount(session: Session, activationKey: String): Future[Boolean] = accountRepository.activate(session.user.accountId, activationKey)


  def login(credentials: Credentials): Future[Option[Session]] = async {
    credentials match {
      case p@Credentials.Provided(id) => {
        val user = await {
          userRepository.findByName(p.identifier)
        }
        val verified = user.exists(u => p.verify(u.password, pw => DigestUtils.sha1Hex(salt + pw) ))
        if (verified) {
          var rnd = new java.security.SecureRandom()
          val bytes = new Array[Byte](24)
          rnd.nextBytes(bytes)
          val token = Token(Base64.encodeBase64String(bytes))
          val expiry = System.currentTimeMillis() + 1000*60*60*24

          val session = await {sessionRepository.create(Session(token, user.get, expiry))}
          session
        } else
          None
      }
      case _ => None
    }
  }

  def addUser(user: AddUser, session: Session): Future[Boolean] = async {
    await {userRepository.create(User(0,session.user.accountId, user.userName, user.email, DigestUtils.sha1Hex(salt + user.password), Map())) }
    // add log
    true
  }



}
