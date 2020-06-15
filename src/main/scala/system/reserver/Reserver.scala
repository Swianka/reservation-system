package system.reserver

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import system.messages.Messages

object Reserver {
  def apply(accommodationsMap: Map[Int, ActorRef[Messages.AccommodationCommand]]): Behavior[Messages.ReserverCommand] = {
    Behaviors.receive {
      (context, message) => {
        message match {
          case msg: Messages.ReservationRequest =>
            context.log.info("ReservationRequest")
            accommodationsMap.get(msg.request.hotelID) match {
              case Some(accommodation) =>
                accommodation ! msg
              case None =>
                context.log.info("ReservationRequest bad accommodation key")
                msg.replyTo ! Messages.ReservationFailureResponse("Bad accommodation key")
            }
            Behaviors.same

          case msg: Messages.ReservationCancellationRequest =>
            context.log.info("ReservationCancellationRequest")
            accommodationsMap.get(msg.reservation.hotelID) match {
              case Some(accommodation) =>
                accommodation ! msg
              case None =>
                context.log.info("ReservationCancellationRequest bad accommodation key")
                msg.replyTo ! Messages.ReservationCancellationFailureResponse("Bad accommodation key")
            }
            Behaviors.same
        }
      }
    }
  }
}
