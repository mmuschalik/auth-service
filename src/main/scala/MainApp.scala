import Aggregates.RegisterAccount
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.stream.ActorMaterializer
import akka.http.scaladsl.server.{ExceptionHandler, RejectionHandler, Route}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.Credentials
import com.typesafe.scalalogging.Logger

import scala.concurrent.Future
import scala.io.StdIn
import org.json4s._
import de.heikoseeberger.akkahttpjson4s.Json4sSupport._
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._

import scala.util.{Failure, Success}
import Services._
import ch.megard.akka.http.cors.scaladsl.settings.CorsSettings

object MainApp {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher
  implicit val serialization = native.Serialization
  implicit val formats = DefaultFormats

  val logger = Logger("AppService")
  val applicationService = new ApplicationService()

  def main(args: Array[String]) {

    val settings = CorsSettings.defaultSettings.copy(allowGenericHttpRequests = false)

    // Your rejection handler
    val rejectionHandler = corsRejectionHandler withFallback RejectionHandler.default

    // Your exception handler
    val exceptionHandler = ExceptionHandler {
      case e: NoSuchElementException => complete(StatusCodes.NotFound -> e.getMessage)
    }

    val handleErrors = handleRejections(rejectionHandler) & handleExceptions(exceptionHandler)


    val route: Route = cors(settings) {
      handleErrors {
        path("register") {
          entity(as[RegisterAccount]) { acc =>

            val f = applicationService.registerAccount(acc)

            onComplete(f) {
              case Success(i) => complete(f)
              case Failure(i) => {
                logger.error(i.toString);
                complete(StatusCodes.BadRequest)
              }
            }
          }
        } ~
          authenticateBasicAsync(realm = "secure site", credentials => applicationService.login(credentials)) { session =>
            path("login") {
              complete(session)
            } ~
              path("account" / IntNumber / "activate") { accountId =>
                parameters('activationKey) { activationKey =>
                  val f = applicationService.activateAccount(session, activationKey)
                  onComplete(f) {
                    case Success(i) => complete(f)
                    case Failure(i) => {
                      logger.info(i.toString);
                      complete("No")
                    }
                  }
                }
              }
          } ~
          authenticateOAuth2Async(realm = "secure site", credentials => applicationService.validateToken(credentials)) { session =>
            path("validate") {
              complete(session)
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
