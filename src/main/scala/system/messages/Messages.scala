package system.messages

import akka.actor.typed.ActorRef

object Messages {

  sealed trait AccommodationMsg

  final case class AccommodationSearchRequest(query: Model.Query, replyTo: ActorRef[OfferListResponse]) extends AccommodationMsg

  final case class OfferListResponse(offerList: List[Model.Offer])

  final case class ReservationRequest(request: Model.ReservationRequest, replyTo: ActorRef[ReservationResponse]) extends AccommodationMsg

  sealed trait ReservationResponse extends AccommodationMsg

  final case class ReservationSuccessResponse(reservation: Model.Reservation) extends ReservationResponse

  final case class ReservationFailureResponse(reason: String) extends ReservationResponse

  final case class ReservationCancellationRequest(reservation: Model.Reservation, replyTo: ActorRef[ReservationCancellationResponse]) extends AccommodationMsg

  sealed trait ReservationCancellationResponse extends AccommodationMsg

  final case class ReservationCancellationSuccessResponse(reservation: Model.Reservation) extends ReservationCancellationResponse

  final case class ReservationCancellationFailureResponse(reason: String) extends ReservationCancellationResponse

  final case class AddRoomRequest(room: Model.Room, replyTo: ActorRef[AddRoomResponse])

  sealed trait AddRoomResponse

  final case class AddRoomSuccessResponse(room: Model.Room) extends AddRoomResponse

  final case class AddRoomFailureResponse(reason: String) extends AddRoomResponse

}

