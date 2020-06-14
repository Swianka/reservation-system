package system.requester

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import system.collector.Collector
import system.messages.{Messages, Model}

object Requester {
  def apply(accommodationList: List[ActorRef[Messages.AccomodationCommand]]): Behavior[Messages.AccommodationSearchRequest] = {
    Behaviors.receive {
      (context, message) => {
        context.log.info("AccommodationSearchRequest in Requester")
        message match {
          case Messages.AccommodationSearchRequest(query: Model.Query, replyTo: ActorRef[Messages.OfferListResponse]) =>
            val collRef = context.spawnAnonymous(Collector(
              expectedReplies = accommodationList.length,
              replyTo = replyTo))
            accommodationList.foreach(ref =>
              ref ! Messages.AccommodationSearchRequest(query, collRef)
            )
            Behaviors.same
        }
      }
    }
  }
}
