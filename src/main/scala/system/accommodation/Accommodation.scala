package system.accommodation

import java.util.{Calendar, Date}

import akka.actor.typed.scaladsl.Behaviors
import system.messages.Model.Offer
import akka.actor.typed.{ActorRef, Behavior}
import system.messages.{Messages, Model}

object Accommodation {
  def apply(hotelID: Int,
            rooms: Set[Model.Room] = Set.empty,
            reservations: Set[Model.ReservationInfo] = Set.empty,
            nextReservationID: Int = 0,
            answer: Boolean = true
           ): Behavior[Messages.AccommodationCommand] = {

    Behaviors.receive {
      (context, message) => {
        message match {

          case Messages.AccommodationSearchRequest(query: Model.Query, replyTo: ActorRef[Messages.OfferListResponse]) =>
            context.log.info("Accommodation" + hotelID + ": AccommodationSearchRequest")
            if (answer) {
              def comparePrices(room: Model.Room): Boolean = (query.priceFrom, query.priceTo) match {
                case (Some(priceFrom), Some(priceTo)) =>
                  room.price >= priceFrom && room.price <= priceTo
                case (None, Some(priceTo)) =>
                  room.price <= priceTo
                case (Some(priceFrom), None) =>
                  room.price >= priceFrom
                case (None, None) =>
                  true
              }

              def isAvailable(room: Model.Room): Boolean = {
                reservations
                  .filter(_.roomID == room.roomID)
                  .filterNot(_.dateTo.compareTo(query.dateFrom) <= 0)
                  .filterNot(_.dateFrom.compareTo(query.dateTo) >= 0)
                  .isEmpty
              }

              val offers = rooms
                .filter(_.roomCapacity >= query.peopleNumber)
                .filter(comparePrices)
                .filter(isAvailable)
                .map(Offer(hotelID, _, query.dateFrom, query.dateTo))
                .toList
              replyTo ! Messages.OfferListResponse(offers)
              Behaviors.same
            }
            else
              Behaviors.same

          case Messages.AddRoomRequest(room: Model.Room, replyTo: ActorRef[Messages.AddRoomResponse]) =>
            context.log.info("Accommodation" + hotelID + ": AddRoomRequest")
            if (answer) {
              rooms.find(_.roomID == room.roomID) match {
                case Some(_) =>
                  context.log.info("Accommodation" + hotelID + ": Room with same ID already exists")
                  replyTo ! Messages.AddRoomFailureResponse("Room with same ID already exists")
                  Behaviors.same
                case None =>
                  context.log.info("Accommodation" + hotelID + ": Add room to accommodation")
                  replyTo ! Messages.AddRoomSuccessResponse(room)
                  Accommodation(hotelID, rooms + room, reservations, nextReservationID)
              }
            }
            else
              Behaviors.same


          case Messages.ReservationRequest(request: Model.ReservationRequest, replyTo: ActorRef[Messages.ReservationResponse]) =>
            context.log.info("Accommodation" + hotelID + ": ReservationRequest")
            if (answer) {
              def isAvailable(roomID: Int): Boolean = {
                reservations
                  .filter(_.roomID == roomID)
                  .filterNot(_.dateTo.compareTo(request.dateFrom) <= 0)
                  .filterNot(_.dateFrom.compareTo(request.dateTo) >= 0)
                  .isEmpty
              }

              if (isAvailable(request.roomID)) {
                val reservationInfo = Model.ReservationInfo(nextReservationID, request.roomID, request.dateFrom, request.dateTo)
                val reservation = Model.Reservation(hotelID, nextReservationID)
                replyTo ! Messages.ReservationSuccessResponse(reservation)
                context.log.info("Accommodation" + hotelID + ": room available, add reservation")
                Accommodation(hotelID, rooms, reservations + reservationInfo, nextReservationID + 1)
              }
              else {
                context.log.info("Accommodation" + hotelID + ": room not available")
                replyTo ! Messages.ReservationFailureResponse("Room not available")
                Behaviors.same
              }
            }
            else
              Behaviors.same


          case Messages.ReservationCancellationRequest(reservation: Model.Reservation, replyTo: ActorRef[Messages.ReservationCancellationResponse]) =>
            context.log.info("Accommodation" + hotelID + ": ReservationCancellationRequest")
            if (answer) {
              val today = Calendar.getInstance().getTime()
              val x = reservations.filter(_.reservationID == reservation.reservationID)
              if (x.nonEmpty) {
                val matching = x.filter(_.dateFrom.compareTo(today) > 0)
                if (matching.nonEmpty) {
                  val reservationInfo = matching.head
                  context.log.info("Accommodation" + hotelID + ": cancellation available")
                  replyTo ! Messages.ReservationCancellationSuccessResponse(reservation)
                  Accommodation(hotelID, rooms, reservations - reservationInfo, nextReservationID)
                }
                else {
                  context.log.info("Accommodation" + hotelID + ": cannot cancel reservation from past")
                  replyTo ! Messages.ReservationCancellationFailureResponse("Cannot cancel reservation from past")
                  Behaviors.same
                }
              }
              else {
                context.log.info("Accommodation" + hotelID + ": reservation doesnt exist")
                replyTo ! Messages.ReservationCancellationFailureResponse("Reservation doesnt exist")
                Behaviors.same
              }
            }
            else
              Behaviors.same
        }
      }
    }
  }
}
