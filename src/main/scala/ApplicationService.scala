import Aggregates.{Account, _}

import scala.concurrent.Future
import scala.async.Async.{async, await}
import scala.concurrent.ExecutionContext.Implicits.global
import Repository._
import Aggregates._
import ValueObjects.Token
import akka.http.scaladsl.server.directives.Credentials
import org.apache.commons.codec.binary.Base64
import org.apache.commons.codec.digest.DigestUtils

class ApplicationService(
  implicit
  val accountRepository: AccountRepositoryTrait,
  implicit val userRepository: UserRepositoryTrait,
  implicit val sessionRepository: SessionRepository
) {

  private val salt = "gentleman-jack"

  def registerAccount(acc: RegisterAccount): Future[Account] = async {
    val account = Account(acc.name)

    await { accountRepository.save(account) }

    await {
        addUser(AddUser(acc.userName, acc.email, acc.password), account.id)
    }

    account
  }

  def activateAccount(session: ValueObjects.Session, activationKey: String): Future[Boolean] = async {
    val account = await {accountRepository.getById(session.accountId)}
    if(account.activationKey == activationKey) {
      account.activate()
      await {
        accountRepository.save(account)
      }
    }
    account.activationKey == activationKey
  }

  def login(credentials: Credentials): Future[Option[ValueObjects.Session]] = async {
    credentials match {
      case p @ Credentials.Provided(id) => {
        val user = await {
          userRepository.findByName(p.identifier)
        }
        val verified = user.exists(u => p.verify(u.password, pw => DigestUtils.sha1Hex(salt + pw)))
        if (verified) {
          var rnd = new java.security.SecureRandom()
          val bytes = new Array[Byte](24)
          rnd.nextBytes(bytes)
          val token = Token(Base64.encodeBase64String(bytes))
          val expiry = System.currentTimeMillis() + 1000 * 60 * 60 * 24

          val session = await { sessionRepository.saveAndGet(new Aggregates.Session(0, user.get.id, token, expiry)) }
          Some(session)
        } else
          None
      }
      case _ => None
    }
  }

  def validateToken(credentials: Credentials): Future[Option[Session]] = async {
    credentials match {
      case Credentials.Provided(token) => {
        val session = await { sessionRepository.findByToken(token, System.currentTimeMillis()) }
        session
      }
      case _ => None
    }

  }

  def addUser(user: AddUser, accountId: Int): Future[Unit] = async {
    await { userRepository.save(new User(0, accountId, user.userName, user.email, DigestUtils.sha1Hex(salt + user.password))) }
    // add log
  }

}
