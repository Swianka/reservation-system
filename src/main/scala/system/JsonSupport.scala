package system

import scala.io.StdIn
import scala.concurrent.{ExecutionContextExecutor, ExecutionContext, Future}
import scala.util.{Failure, Success}
import scala.concurrent.Future
import scala.concurrent.duration._
import spray.json._
import system.utils.DateMarshalling._
import system.messages.Messages
import system.messages.Model
import akka.{Done, actor}
import akka.actor.typed.{ ActorRef, ActorSystem }
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.stream.ActorMaterializer
import akka.util.Timeout
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server._
import Directives._
import akka.pattern.AskTimeoutException

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val reservationFormat = jsonFormat2(Model.Reservation)
  implicit val reservationRequestFormat = jsonFormat4(Model.ReservationRequest)
  implicit val queryFormat = jsonFormat5(Model.Query)
  implicit val roomFormat = jsonFormat4(Model.Room)
  implicit val offerFormat = jsonFormat4(Model.Offer)
}