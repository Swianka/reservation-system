package system.client

import akka.actor.typed.{ActorRef, ActorSystem, Scheduler}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import system.JsonSupport
import system.messages.{Messages, Model}

import scala.concurrent.Future
import scala.concurrent.duration._

class Routes(system: ActorSystem[_],
             requesterRef: ActorRef[Messages.AccommodationSearchRequest],
             reserverRef: ActorRef[Messages.ReserverCommand],
             accommodationList: List[ActorRef[Messages.AccomodationCommand]]) extends JsonSupport {

  import akka.actor.typed.scaladsl.AskPattern._

  implicit val scheduler: Scheduler = system.scheduler

  val routes: Route =
    concat(
      path("search") {
        get {
          entity(as[Model.Query]) { query =>
            implicit val timeout: Timeout = Timeout(5.seconds)
            val offers: Future[Messages.OfferListResponse] = requesterRef.ask(replyTo => Messages.AccommodationSearchRequest(query, replyTo))
            onSuccess(offers) { offers =>
              complete(offers.offerList)
            }
          }
        }
      },
      path("reserve") {
        post {
          entity(as[Model.ReservationRequest]) { reserve =>
            implicit val timeout: Timeout = Timeout(5.seconds)
            val reservationResponse: Future[Messages.ReservationResponse] = reserverRef.ask(replyTo => Messages.ReservationRequest(reserve, replyTo))
            onSuccess(reservationResponse) {
              case Messages.ReservationSuccessResponse(reservation) =>
                complete(reservation)
              case Messages.ReservationFailureResponse(reason) =>
                complete(reason)
            }
          }
        }
      },
      path("cancel") {
        post {
          entity(as[Model.Reservation]) { reservation =>
            implicit val timeout: Timeout = Timeout(5.seconds)
            val cancelationResponse: Future[Messages.ReservationCancellationResponse] = reserverRef.ask(replyTo => Messages.ReservationCancellationRequest(reservation, replyTo))
            onSuccess(cancelationResponse) {
              case Messages.ReservationCancellationSuccessResponse(reservation) =>
                complete("Done")
              case Messages.ReservationCancellationFailureResponse(reason) =>
                complete(reason)
            }
          }
        }
      },
      path("accomodation" / IntNumber / "addroom") { i: Int =>
        post {
          entity(as[Model.Room]) { room =>
            implicit val timeout: Timeout = Timeout(5.seconds)
            accommodationList.lift(i) match {
              case Some(accomodationRef) =>
                val response: Future[Messages.AddRoomResponse] = accomodationRef.ask(replyTo => Messages.AddRoomRequest(room, replyTo))
                onSuccess(response) {
                  case Messages.AddRoomSuccessResponse(room) =>
                    complete(room)
                  case Messages.AddRoomFailureResponse(reason) =>
                    complete(reason)
                }
              case None =>
                complete(StatusCodes.NotFound, "Accommodation does not exists")
            }
          }
        }
      })
}