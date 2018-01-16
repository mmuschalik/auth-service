package Repository

import com.github.mauricio.async.db.postgresql.PostgreSQLConnection
import com.github.mauricio.async.db.postgresql.util.URLParser

import scala.async.Async.{async, await}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import Aggregates._
import com.typesafe.config.Config

trait UserRepositoryTrait extends RepoTrait {
  def findByName(userName: String): Future[Option[User]]
  def save(user: User): Future[Unit]
}

class UserRepository(implicit val config: Config) extends UserRepositoryTrait {

  val configuration = URLParser.parse(config.getString("connection"))

  def findByName(userName: String): Future[Option[User]] = async {

    val con = await { new PostgreSQLConnection(configuration).connect }
    val result = await { con.sendPreparedStatement("select id, accountid, username, email, password from users where username = ?", List(userName)) }

    val ret = result.rows.flatMap(_.headOption).map(r => new User(r("id").toString.toInt, r("accountid").toString.toInt, r("username").toString, r("email").toString, r("password").toString))

    await { con.disconnect }
    ret
  }

  def save(user: User): Future[Unit] = async {

    val con = await { new PostgreSQLConnection(configuration).connect }
    await { con.sendPreparedStatement("insert into users(accountid,username, email, password) values(?, ?, ?, ?)", List(user.accountId, user.userName, user.email, user.password)) }
    await { con.disconnect }
    Unit
  }
}
