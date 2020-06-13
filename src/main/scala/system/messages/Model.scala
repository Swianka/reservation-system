package system.messages

import java.util.Date

object Model {

  final case class Query(var peopleNumber: Int, var dateFrom: Date, var dateTo: Date, var priceFrom: Int = 0, var priceTo: Int = Int.MaxValue)

  final case class Room(var roomID: Int, var roomCapacity: Int, var price: Int, var details: String)

  final case class Offer(var hotelID: Int, var room: Room, var dateFrom: Date, var dateTo: Date)

  final case class ReservationRequest(var hotelID: Int, var roomID: Int, var dateFrom: Date, var dateTo: Date)

  final case class Reservation(var hotelID: Int, var reservationID: Int)

}
