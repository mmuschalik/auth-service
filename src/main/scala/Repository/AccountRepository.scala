package Repository

import com.github.mauricio.async.db.postgresql.PostgreSQLConnection
import com.github.mauricio.async.db.postgresql.util.URLParser

import scala.async.Async.{async, await}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import Model._

object AccountRepository {

  val configuration = URLParser.parse("jdbc:postgresql://localhost:5432/auth?user=postgres&password=actio")

  def createAccount(acc: Account): Future[Option[Account]] = async {
    val key = java.util.UUID.randomUUID().toString
    val con = await { new PostgreSQLConnection(configuration).connect }
    val result = await { con.sendPreparedStatement("insert into account(name,activated,activationkey) values(?, ?, ?) returning id,name,activated", List(acc.name,acc.activated, key)) }

    val ret = result.rows.flatMap(_.headOption).map(r => Account(r("id").toString.toInt,r("name").toString, r("activated").toString.toBoolean))

    await {con.disconnect}
    ret
  }

  def activateAccount(accountId: Int, activationKey: String): Future[Boolean] = async {
    val con = await { new PostgreSQLConnection(configuration).connect }
    val result = await { con.sendPreparedStatement("update account set activated=true where id = ? and activationkey = ?", List(accountId, activationKey)) }

    val ret = result.rowsAffected == 1

    await {con.disconnect}
    ret
  }

}
