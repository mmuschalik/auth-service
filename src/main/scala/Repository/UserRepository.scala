package Repository


import com.github.mauricio.async.db.postgresql.PostgreSQLConnection
import com.github.mauricio.async.db.postgresql.util.URLParser

import scala.async.Async.{async, await}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import Model._

object UserRepository {

  val configuration = URLParser.parse("jdbc:postgresql://localhost:5432/auth?user=postgres&password=actio")

  def findUser(userName: String) : Future[Option[User]] = async {

    val con = await { new PostgreSQLConnection(configuration).connect }
    val result = await { con.sendPreparedStatement("select id, accountid, username, email, password from users where username = ?", List(userName)) }

    val ret = result.rows.flatMap(_.headOption).map(r => User(r("id").toString.toInt, r("accountid").toString.toInt, r("username").toString, r("email").toString, r("password").toString, Map()))

    await {con.disconnect}
    ret
  }

  def createUser(user: User) : Future[Int] = async {

    val con = await { new PostgreSQLConnection(configuration).connect }
    val result = await { con.sendPreparedStatement("insert into users(accountid,username, email, password) values(?, ?, ?, ?)", List(user.accountId,user.userName, user.email, user.password)) }

    await {con.disconnect}
    result.rowsAffected.toInt
  }
}
