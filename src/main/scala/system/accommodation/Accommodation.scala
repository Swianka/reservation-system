package system.accommodation

import java.util.Date

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import system.messages.{Messages, Model}

object Accommodation {
  def apply(): Behavior[Messages.AccommodationSearchRequest] = {
    Behaviors.receive {
      (context, message) => {
        context.log.info("AccommodationSearchRequest in Accommodation")
        message match {
          case Messages.AccommodationSearchRequest(query: Model.Query, replyTo: ActorRef[Messages.OfferListResponse]) =>
            val offer = Model.Offer(1, Model.Room(1, 1, 1, ""), new Date(), new Date())
            val list = List(offer)
            replyTo ! Messages.OfferListResponse(list)
            Behaviors.same
          case _ =>
            Behaviors.same
        }
      }
    }
  }
}
