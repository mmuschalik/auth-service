
import com.github.mauricio.async.db.Connection
import com.github.mauricio.async.db.postgresql.PostgreSQLConnection
import com.github.mauricio.async.db.postgresql.util.URLParser

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.async.Async.{async, await}

object Repository {

  val configuration = URLParser.parse("jdbc:postgresql://localhost:5432/auth?user=postgres&password=actio")
  val connection: Connection = new PostgreSQLConnection(configuration)


  def createUser(user: User) : Future[Int] = async {

    val con = await { connection.connect }
    val result = await { con.sendPreparedStatement("insert into users(shortName, password) values(?, ?)", List(user.shortName, user.password)) }

    result.rowsAffected.toInt
  }
}
