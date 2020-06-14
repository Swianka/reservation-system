package system.reserver

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import system.messages.Messages

object Reserver {
  def apply(accommodationsList: List[ActorRef[Messages.AccomodationCommand]]): Behavior[Messages.ReserverCommand] = {
    Behaviors.receive {
      (context, message) => {
        message match {
          case msg: Messages.ReservationRequest =>
            context.log.info("ReservationRequest")
            accommodationsList.lift(msg.request.hotelID) match {
              case Some(accommodation) =>
                accommodation ! msg
              case None =>
                context.log.info("ReservationRequest bad accommodation key")
                msg.replyTo ! Messages.ReservationFailureResponse("Bad accommodation key")
            }
            Behaviors.same

          case msg: Messages.ReservationCancellationRequest =>
            context.log.info("ReservationCancellationRequest")
            accommodationsList.lift(msg.reservation.hotelID) match {
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
