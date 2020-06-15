package system.collector

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import system.messages.{Messages, Model}

import scala.concurrent.duration._

object Collector {
  def apply(expectedReplies: Int,
            replyTo: ActorRef[Messages.OfferListResponse]
           ): Behavior[Messages.OfferResponse] = {

    Behaviors.setup { context =>
      context.setReceiveTimeout(3.second, Messages.ReceiveTimeout())

      def collecting(replies: List[Model.Offer], numberOfReplies: Int): Behavior[Messages.OfferResponse] = {
        Behaviors.receiveMessage {
          case Messages.OfferListResponse(offerList: List[Model.Offer]) =>
            context.log.info("OfferListResponse in Collector")
            val newReplies = replies ++ offerList
            if (numberOfReplies + 1 == expectedReplies) {
              replyTo ! Messages.OfferListResponse(newReplies)
              Behaviors.stopped
            } else
              collecting(newReplies, numberOfReplies + 1)

          case Messages.ReceiveTimeout() =>
            context.log.info("Timeout in Collector")
            replyTo ! Messages.OfferListResponse(replies)
            Behaviors.stopped
        }
      }

      collecting(List[Model.Offer](), 0)
    }
  }
}
