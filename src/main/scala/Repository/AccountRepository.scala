package Repository

import com.github.mauricio.async.db.postgresql.PostgreSQLConnection
import com.github.mauricio.async.db.postgresql.util.URLParser

import scala.async.Async.{async, await}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import Aggregates.{Account, _}
import com.typesafe.config.Config

trait AccountRepositoryTrait extends RepoTrait {
  def getById(id: Int): Future[Account]
  def save(acc: Account): Future[Unit]
}

class AccountRepository(implicit val config: Config) extends AccountRepositoryTrait {

  val configuration = URLParser.parse(config.getString("connection"))

  private def create(acc: Account): Future[Unit] = async {

    val connection = await { new PostgreSQLConnection(configuration).connect }
    val queryResult = await { connection.sendPreparedStatement("insert into account(name,activated,activationkey) values(?, ?, ?) returning id", List(acc.name, acc.isActive, acc.activationKey)) }
    val accountId = queryResult.rows.flatMap(_.headOption).map(r => r("id").toString.toInt).get

    await { connection.disconnect }
    this.setId(acc, accountId)
  }

  private def update(acc: Account): Future[Unit] = async {
    val connection = await { new PostgreSQLConnection(configuration).connect }
    await { connection.sendPreparedStatement("update account set name = ?,activated = ? from account where id = ?", List(acc.name, acc.isActive, acc.id)) }
    await { connection.disconnect }
    Unit
  }

  def getById(id: Int): Future[Account] = async {
    val connection = await { new PostgreSQLConnection(configuration).connect }
    val queryResult = await { connection.sendPreparedStatement("selected id,name,activated,activationkey from account where id = ?", List(id)) }
    val row = queryResult.rows.get.head
    val account = new Account(row("id").toString.toInt, row("name").toString, row("activated").toString.toBoolean, row("activationkey").toString)

    await { connection.disconnect }
    account
  }

  def save(acc: Account): Future[Unit] = if (acc.id == 0) create(acc) else update(acc)

}
