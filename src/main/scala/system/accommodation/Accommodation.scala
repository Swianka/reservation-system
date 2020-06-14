package system.accommodation

import java.util.Date

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import system.messages.{Messages, Model}

object Accommodation {
  def apply(rooms: Set[Model.Room] = Set.empty): Behavior[Messages.AccomodationCommand] = {
    Behaviors.receive {
      (context, message) => {
        message match {
          case Messages.AccommodationSearchRequest(query: Model.Query, replyTo: ActorRef[Messages.OfferListResponse]) =>
            context.log.info("AccommodationSearchRequest in Accommodation")
            val offer = Model.Offer(1, Model.Room(1, 1, 1, ""), new Date(), new Date())
            val list = List(offer)
            replyTo ! Messages.OfferListResponse(list)
            Behaviors.same
          case Messages.AddRoomRequest(room: Model.Room, replyTo: ActorRef[Messages.AddRoomResponse]) =>
            context.log.info("AddRoomRequest")
            rooms.find(_.roomID == room.roomID) match {
              case Some(_) =>
                context.log.info("Room with same ID already exists")
                replyTo ! Messages.AddRoomFailureResponse("Room with same ID already exists")
                Behaviors.same
              case None =>
                context.log.info("Add room to accomodation")
                replyTo ! Messages.AddRoomSuccessResponse(room)
                Accommodation(rooms + room)
            }
          case Messages.ReservationRequest(request: Model.ReservationRequest, replyTo: ActorRef[Messages.ReservationResponse]) =>
            val roomAvailable = true
            if (roomAvailable) {
              context.log.info("ReservationRequest in Accommodation, room available")
              //val reservation = Model.Reservation(request.hotelID, request.roomID)
              val reservation = Model.Reservation(1,1)
              replyTo ! Messages.ReservationSuccessResponse(reservation)
            }
            else {
              context.log.info("ReservationRequest in Accommodation, room not available")
              replyTo ! Messages.ReservationFailureResponse("Room not available")
            }
            Behaviors.same
          case Messages.ReservationCancellationRequest(reservation: Model.Reservation, replyTo: ActorRef[Messages.ReservationCancellationResponse]) =>
            val cancellationAvailable = true
            if(cancellationAvailable) {
              context.log.info("ReservationCancellationRequest in Accommodation, cancellation available")
              val reservation = Model.Reservation(1, 1)
              replyTo ! Messages.ReservationCancellationSuccessResponse(reservation)
            }
            else {
              context.log.info("ReservationCancellationRequest in Accommodation, cancellation not available")
              replyTo ! Messages.ReservationCancellationFailureResponse("Reservation cannot be cancelled")
            }
            Behaviors.same
        }
      }
    }
  }
}
