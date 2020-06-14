package system.reserver

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import system.messages.{Messages, Model}
import system.messages.Messages.{ReservationCancellationResponse, ReservationResponse}

object Reserver {
  def apply(accommodationMap: Map[Int, ActorRef[Messages.AccommodationMsg]]): Behavior[Messages.AccommodationMsg] = {
    Behaviors.receive {
      (context, message) => {
        message match {
          case Messages.ReservationRequest(request: Model.ReservationRequest, replyTo: ActorRef[ReservationResponse]) =>
            val accommodation = accommodationMap.getOrElse(0, null)
            if(accommodation == null) {
              context.log.info("ReservationRequest in Reserver, bad key")
              replyTo ! Messages.ReservationFailureResponse("Bad accomodation key")
              Behaviors.same
            }
            else{
              accommodation ! Messages.ReservationRequest(request, replyTo)
              Behaviors.same
            }

          case Messages.ReservationCancellationRequest(reservation: Model.Reservation, replyTo: ActorRef[ReservationCancellationResponse]) =>
            val accommodation = accommodationMap.getOrElse(0, null)
            if(accommodation == null) {
              context.log.info("ReservationCancellationRequest in Reserver, bad key")
              replyTo ! Messages.ReservationFailureResponse("Bad accomodation key")
              Behaviors.same
            }
            else {
              accommodation ! Messages.ReservationCancellationRequest(reservation, replyTo)
              Behaviors.same
            }
          case _ =>
            Behaviors.same
        }
      }
    }
  }
}
