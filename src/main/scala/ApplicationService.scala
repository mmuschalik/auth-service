import Model._
import Repository.AccountRepository

import scala.concurrent.Future
import scala.async.Async.{async, await}
import scala.concurrent.ExecutionContext.Implicits.global
import Repository._
import Model._
import akka.http.scaladsl.server.directives.Credentials
import org.apache.commons.codec.digest.DigestUtils

object ApplicationService {

  private val salt = "gentleman-jack"

  def registerAccount(acc: RegisterAccount): Future[Option[Account]] = async {
    val account = await {AccountRepository.createAccount(Account(0, acc.name, false))}

    if(account.isDefined) {
      val created = await {
        addUser(AddUser(acc.userName, acc.email, acc.password),Session(User(0, account.get.id, acc.userName, acc.email, acc.password, Map())))
      }
    }
    account
  }

  def activateAccount(session: Session, activationKey: String): Future[Boolean] = AccountRepository.activateAccount(session.user.accountId, activationKey)


  def login(credentials: Credentials): Future[Option[Session]] = async {
    credentials match {
      case p@Credentials.Provided(id) => {
        val user = await {
          UserRepository.findUser(p.identifier)
        }
        val verified = user.exists(u => p.verify(u.password, pw => DigestUtils.sha1Hex(salt + pw) ))
        if (verified)
          Some(Session(user.get))
        else
          None
      }
      case _ => None
    }
  }

  def addUser(user: AddUser, session: Session): Future[Boolean] = async {
    await {UserRepository.createUser(User(0,session.user.accountId, user.userName, user.email, DigestUtils.sha1Hex(salt + user.password), Map())) }
    // add log
    true
  }



}
