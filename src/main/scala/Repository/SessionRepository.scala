package Repository

import com.github.mauricio.async.db.postgresql.PostgreSQLConnection
import com.github.mauricio.async.db.postgresql.util.URLParser

import scala.async.Async.{async, await}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import Model._
import com.typesafe.config.Config

trait SessionRepositoryTrait {
  def create(session: Session): Future[Option[Session]]
  def findByToken(token: String, expiry: Long): Future[Option[Session]]
}

class SessionRepository(implicit val config: Config) extends SessionRepositoryTrait {

  val configuration = URLParser.parse(config.getString("connection"))

  def create(session: Session): Future[Option[Session]] = async {

    val con = await { new PostgreSQLConnection(configuration).connect }
    val result = await {
      con.sendPreparedStatement(
        "insert into usersession(token, userid,accountid,username,email, expiry) values (?, ?, ?, ?, ?, ?) returning token,userid,accountid,username,email,expiry",
        List(session.token.str, session.user.id, session.user.accountId, session.user.userName, session.user.email, session.expiry)
      )
    }

    val ret = result.rows.flatMap(_.headOption).map(r => Session(Token(r("token").toString), User(r("userid").toString.toInt, r("accountid").toString.toInt, r("username").toString, r("email").toString, "", Map()), r("expiry").toString.toLong))

    await { con.disconnect }
    ret
  }

  def findByToken(token: String, expiry: Long): Future[Option[Session]] = async {

    val con = await { new PostgreSQLConnection(configuration).connect }
    val result = await { con.sendPreparedStatement("select * from usersession where token = ? and expiry >= ?", List(token, expiry)) }

    val ret = result.rows.flatMap(_.headOption).map(r => Session(Token(r("token").toString), User(r("userid").toString.toInt, r("accountid").toString.toInt, r("username").toString, r("email").toString, "", Map()), r("expiry").toString.toLong))

    await { con.disconnect }
    ret
  }

}
