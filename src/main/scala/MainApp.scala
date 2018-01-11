import Model.{RegisterAccount, Session}
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.stream.ActorMaterializer
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.Credentials
import com.typesafe.scalalogging.Logger

import scala.concurrent.Future
import scala.io.StdIn
import org.json4s._
import de.heikoseeberger.akkahttpjson4s.Json4sSupport._

import scala.util.{Failure, Success}

object MainApp {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  implicit val serialization = native.Serialization
  implicit val formats = DefaultFormats

  val logger = Logger("AppService")

  def myUserPassAuthenticator(credentials: Credentials): Future[Option[Session]] = {
    ApplicationService.login(credentials)
  }

  def main(args: Array[String]) {



    val route: Route =
     path("register") {
        entity(as[RegisterAccount]) { acc =>

          val f = ApplicationService.registerAccount(acc)

          onComplete(f) {
            case Success(i) => complete(f)
            case Failure(i) => {
              logger.error(i.toString);
              complete(StatusCodes.BadRequest)
            }
          }
        }
     } ~
     authenticateBasicAsync(realm = "secure site", myUserPassAuthenticator) { session =>
      path("account" / IntNumber / "activate") { accountId =>
        parameters('activationKey) { activationKey =>
          val f = ApplicationService.activateAccount(session, activationKey)
          onComplete(f) {
            case Success(i) => complete(f)
            case Failure(i) => {
              logger.info(i.toString);
              complete("No")
            }
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
