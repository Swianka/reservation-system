package system.messages

import akka.actor.typed.ActorRef

object Messages {

  sealed trait ClientCommand

  sealed trait AccommodationCommand

  sealed trait ReserverCommand

  final case class AccommodationSearchRequest(query: Model.Query, replyTo: ActorRef[OfferListResponse]) extends ClientCommand with AccommodationCommand

  sealed trait OfferResponse

  final case class OfferListResponse(offerList: List[Model.Offer]) extends OfferResponse

  final case class ReceiveTimeout() extends OfferResponse

  final case class ReservationRequest(request: Model.ReservationRequest, replyTo: ActorRef[ReservationResponse]) extends ClientCommand with AccommodationCommand with ReserverCommand

  sealed trait ReservationResponse

  final case class ReservationSuccessResponse(reservation: Model.Reservation) extends ReservationResponse

  final case class ReservationFailureResponse(reason: String) extends ReservationResponse

  final case class ReservationCancellationRequest(reservation: Model.Reservation, replyTo: ActorRef[ReservationCancellationResponse]) extends ClientCommand with AccommodationCommand with ReserverCommand

  sealed trait ReservationCancellationResponse extends ClientCommand

  final case class ReservationCancellationSuccessResponse(reservation: Model.Reservation) extends ReservationCancellationResponse

  final case class ReservationCancellationFailureResponse(reason: String) extends ReservationCancellationResponse

  final case class AddRoomRequest(room: Model.Room, replyTo: ActorRef[AddRoomResponse]) extends AccommodationCommand

  sealed trait AddRoomResponse

  final case class AddRoomSuccessResponse(room: Model.Room) extends AddRoomResponse

  final case class AddRoomFailureResponse(reason: String) extends AddRoomResponse

}

