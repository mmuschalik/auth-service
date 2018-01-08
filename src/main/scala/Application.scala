import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.Done
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._

import com.typesafe.scalalogging.Logger

import scala.concurrent.Future
import scala.async.Async.{async, await}
import scala.io.StdIn
import org.json4s._
import de.heikoseeberger.akkahttpjson4s.Json4sSupport._

import scala.util.{Failure, Success}

object Application {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  implicit val serialization = native.Serialization
  implicit val formats = DefaultFormats

  val logger = Logger("AppService")


  def main(args: Array[String]) {

    val route: Route = post {
      path("create-user") {
        entity(as[User]) { user =>

          val f = async {
            logger.info("Going to execute now")
            val num = await(Repository.createUser(user))
            logger.info(num.toString)
            num
          }

          onComplete(f) {
            case Success(i) => complete("Yes")
            case Failure(i) => { logger.info(i.toString); complete("No")}
          }
        }
      }
    }

    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)
    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ ⇒ system.terminate()) // and shutdown when done

  }

}
