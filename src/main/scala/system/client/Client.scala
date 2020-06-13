package system.client

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import scala.io.StdIn
import system.messages.Model
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._
import system.utils.DateMarshalling._

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val reservationFormat = jsonFormat2(Model.Reservation)
  implicit val reservationRequestFormat = jsonFormat4(Model.ReservationRequest)
  implicit val queryFormat = jsonFormat5(Model.Query)
  implicit val roomFormat = jsonFormat4(Model.Room)
  implicit val offerFormat = jsonFormat4(Model.Offer)
}

object WebServer extends Directives with JsonSupport {
  def main(args: Array[String]) {

    implicit val system = ActorSystem("reservation-system")
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher

    val route =
      concat(
        path("search") {
          get {
            entity(as[Model.Query]) { query =>
              val peopleNumber = query.peopleNumber
              val dateFrom = query.dateFrom
              val dateTo = query.dateTo
              val priceFrom = query.priceFrom
              val priceTo = query.priceTo
              complete(s"Search $peopleNumber $dateFrom $dateTo $priceFrom $priceTo")
            }
          }
        },
        path("reserve") {
          post {
            entity(as[Model.ReservationRequest]) { reserve =>
              val hotelId = reserve.hotelID
              val roomId = reserve.roomID
              val dateFrom = reserve.dateFrom
              val dateTo = reserve.dateTo
              complete(s"Reserve $hotelId $roomId $dateFrom $dateTo")
            }
          }
        },
        path("cancel") {
          post {
            entity(as[Model.Reservation]) { reservation =>
              val hotelId = reservation.hotelID
              val reservationId = reservation.reservationID
              complete(s"Cancel $hotelId:$reservationId")
            }
          }
        })

    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }
}
