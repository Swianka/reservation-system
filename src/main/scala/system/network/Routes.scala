package system.network

import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Directives._
import akka.util.Timeout
import system.JsonSupport
import system.messages.{Messages, Model}

import scala.concurrent.Future
import scala.concurrent.duration._

class Routes(system: ActorSystem[_], clientActor: ActorRef[Messages.ClientCommand]) extends JsonSupport {

  import akka.actor.typed.scaladsl.AskPattern._

  implicit val scheduler = system.scheduler

  val routes =
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