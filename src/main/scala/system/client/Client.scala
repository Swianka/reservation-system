package system.client

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import system.messages.Messages

object Client {
  def apply(requesterRef: ActorRef[Messages.AccommodationSearchRequest]): Behavior[Messages.ClientCommand] =
    Behaviors.receiveMessage {
      case msg: Messages.AccommodationSearchRequest =>
        requesterRef ! msg
        Behaviors.same
      case Messages.ReservationRequest(request, replyTo) =>
        replyTo ! Messages.ReservationFailureResponse("Nope")
        Behaviors.same
      case Messages.ReservationCancellationRequest(reservation, replyTo) =>
        Behaviors.same
    }
}
