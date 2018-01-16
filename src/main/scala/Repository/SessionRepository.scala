package Repository

import com.github.mauricio.async.db.postgresql.PostgreSQLConnection
import com.github.mauricio.async.db.postgresql.util.URLParser

import scala.async.Async.{async, await}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import Aggregates._
import ValueObjects.Token
import com.typesafe.config.Config

trait SessionRepositoryTrait extends RepoTrait {
  def saveAndGet(session: Session): Future[ValueObjects.Session]
  def findByToken(token: String, expiry: Long): Future[Option[ValueObjects.Session]]
}

class SessionRepository(implicit val config: Config) extends SessionRepositoryTrait {

  val configuration = URLParser.parse(config.getString("connection"))

  def saveAndGet(session: Session): Future[ValueObjects.Session] = async {

    val con = await { new PostgreSQLConnection(configuration).connect }

    val resultQuery = await {
      con.sendPreparedStatement(
        "with us as (\ninsert into usersession(userid,token,expiry) values (?, ?, ?) returning id\n) select u.id as userid,u.accountid,u.username,u.email,s.token,s.expiry from usersession s inner join users u on (s.userid = u.id)",
        List(session.userId, session.token.str, session.expiry)
      )
    }

    val ret = resultQuery.rows.flatMap(_.headOption).map(r => ValueObjects.Session(r("userid").toString.toInt, r("accountid").toString.toInt, r("username").toString, Token(r("token").toString), r("expiry").toString.toLong))

    await { con.disconnect }
    ret.get
  }

  def findByToken(token: String, expiry: Long): Future[Option[ValueObjects.Session]] = async {

    val con = await { new PostgreSQLConnection(configuration).connect }
    val resultQuery = await { con.sendPreparedStatement("select u.id as userid,u.accountid,u.username,u.email,s.token,s.expiry from usersession s inner join users u on (s.userid = u.id) where s.token = ? and s.expiry >= ?", List(token, expiry)) }

    val ret = resultQuery.rows.flatMap(_.headOption).map(r => ValueObjects.Session(r("userid").toString.toInt, r("accountid").toString.toInt, r("username").toString, Token(r("token").toString), r("expiry").toString.toLong))
    await { con.disconnect }
    ret
  }

}
