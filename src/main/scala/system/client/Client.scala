package system.client

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import system.messages.Messages

object Client {
  def apply(requesterRef: ActorRef[Messages.AccommodationSearchRequest],
            reserverRef: ActorRef[Messages.ReserverCommand]): Behavior[Messages.ClientCommand] =
    Behaviors.receiveMessage {
      case msg: Messages.AccommodationSearchRequest =>
        requesterRef ! msg
        Behaviors.same
      case msg: Messages.ReservationRequest =>
        reserverRef ! msg
        Behaviors.same
      case msg: Messages.ReservationCancellationRequest =>
        reserverRef ! msg
        Behaviors.same
    }
}
