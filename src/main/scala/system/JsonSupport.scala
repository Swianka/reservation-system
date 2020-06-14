package system

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._
import system.messages.Model
import system.utils.DateMarshalling._

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val reservationFormat = jsonFormat2(Model.Reservation)
  implicit val reservationRequestFormat = jsonFormat4(Model.ReservationRequest)
  implicit val queryFormat = jsonFormat5(Model.Query)
  implicit val roomFormat = jsonFormat4(Model.Room)
  implicit val offerFormat = jsonFormat4(Model.Offer)
}