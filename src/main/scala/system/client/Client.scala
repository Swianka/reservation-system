package system.client

import scala.io.StdIn
import scala.concurrent.{ExecutionContextExecutor, ExecutionContext, Future}
import scala.util.{Failure, Success}
import scala.concurrent.Future
import scala.concurrent.duration._
import spray.json._
import system.utils.DateMarshalling._
import system.messages.Messages
import system.messages.Model
import akka.{Done, actor}
import akka.actor.typed.{ ActorRef, ActorSystem }
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.stream.ActorMaterializer
import akka.util.Timeout
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server._
import Directives._
import akka.pattern.AskTimeoutException

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val reservationFormat = jsonFormat2(Model.Reservation)
  implicit val reservationRequestFormat = jsonFormat4(Model.ReservationRequest)
  implicit val queryFormat = jsonFormat5(Model.Query)
  implicit val roomFormat = jsonFormat4(Model.Room)
  implicit val offerFormat = jsonFormat4(Model.Offer)
}

object Client {
  def apply(): Behavior[Messages.ClientCommand] =
    Behaviors.receiveMessage {
      case Messages.AccommodationSearchRequest(query, replyTo) =>
        Behaviors.same
      case Messages.ReservationRequest(request, replyTo) =>
        replyTo ! Messages.ReservationFailureResponse("Nope")
        Behaviors.same
      case Messages.ReservationCancellationRequest(reservation, replyTo) =>
        Behaviors.same
    }
}

class ClientRoutes(system: ActorSystem[_], clientActor: ActorRef[Messages.ClientCommand]) extends JsonSupport {
  import akka.actor.typed.scaladsl.AskPattern._

  implicit val scheduler = system.scheduler

  val clientRoutes =
    concat(
      path("search") {
        get {
          entity(as[Model.Query]) { query =>
            implicit val timeout = Timeout(5.seconds)
            val offers: Future[Messages.OfferListResponse] = clientActor.ask(replyTo => Messages.AccommodationSearchRequest(query, replyTo))
            onSuccess(offers) { offers =>
              complete(offers.offerList)
            }
          }
        }
      },
      path("reserve") {
        post {
          entity(as[Model.ReservationRequest]) { reserve =>
            implicit val timeout = Timeout(5.seconds)
              val reservationResponse: Future[Messages.ReservationResponse] = clientActor.ask(replyTo => Messages.ReservationRequest(reserve, replyTo))
              onSuccess(reservationResponse) { reservationResponse =>
                reservationResponse match {
                  case Messages.ReservationSuccessResponse(reservation) =>
                    complete(reservation)
                  case Messages.ReservationFailureResponse(reason) =>
                    complete(reason)
                }
              }
            }
        }
      },
      path("cancel") {
        post {
          entity(as[Model.Reservation]) { reservation =>
            implicit val timeout = Timeout(5.seconds)
            val cancelationResponse: Future[Messages.ReservationCancellationResponse] = clientActor.ask(replyTo => Messages.ReservationCancellationRequest(reservation, replyTo))
            onSuccess(cancelationResponse) { cancelationResponse =>
              cancelationResponse match {
                case Messages.ReservationCancellationSuccessResponse(reservation) =>
                  complete("Done")
                case Messages.ReservationCancellationFailureResponse(reason) =>
                  complete(reason)
              }
            }
          }
        }
      })
}

object ReservationServer extends App {
  val system = ActorSystem[Done](Behaviors.setup { ctx =>
    implicit val untypedSystem: actor.ActorSystem = ctx.system.classicSystem
    implicit val materializer: ActorMaterializer = ActorMaterializer()(ctx.system.classicSystem)
    implicit val ec: ExecutionContextExecutor = ctx.system.executionContext

    val clientActorRef = ctx.spawn(Client(), "clientActor")

    val routes = new ClientRoutes(ctx.system, clientActorRef)

    val serverBinding: Future[Http.ServerBinding] = Http()(untypedSystem).bindAndHandle(routes.clientRoutes, "localhost", 8080)
    serverBinding.onComplete {
      case Success(bound) =>
        println(s"Server online at http://${bound.localAddress.getHostString}:${bound.localAddress.getPort}/")
      case Failure(e) =>
        Console.err.println(s"Server could not start!")
        e.printStackTrace()
        ctx.self ! Done
    }
    Behaviors.receiveMessage {
      case Done =>
        Behaviors.stopped
    }

  }, "reservation-system")
}
