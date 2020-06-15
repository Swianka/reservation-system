package system

import akka.actor.typed.{ActorRef, ActorSystem}
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.{Done, actor}
import system.accommodation.Accommodation
import system.client.Routes
import system.messages.Messages
import system.requester.Requester
import system.reserver.Reserver

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

object ReservationServer extends App {
  val usage =
    """
    Usage: [--accom number] [--accom-no-answer number]
  """
  type OptionMap = Map[String, Int]

  @scala.annotation.tailrec
  def nextOption(map: OptionMap, list: List[String]): OptionMap = {
    list match {
      case Nil => map
      case "--accom" :: value :: tail =>
        nextOption(map ++ Map("accom" -> value.toInt), tail)
      case "--accom-no-answer" :: value :: tail =>
        nextOption(map ++ Map("accom-no-answer" -> value.toInt), tail)
      case option :: tail =>
        println("Unknown option " + option)
        print(usage)
        sys.exit(1)
    }
  }

  val arglist = args.toList
  val options = nextOption(Map(), arglist)
  val numberOfAccommodations: Int = options.get("accom") match {
    case Some(i) => i
    case _ => 2
  }
  val numberOfAccommodationsNoAnswer: Int = options.get("accom-no-answer") match {
    case Some(i) => i
    case _ => 0
  }

  val system = ActorSystem[Done](Behaviors.setup { ctx =>
    implicit val untypedSystem: actor.ActorSystem = ctx.system.classicSystem
    implicit val materializer: ActorMaterializer = ActorMaterializer()(ctx.system.classicSystem)
    implicit val ec: ExecutionContextExecutor = ctx.system.executionContext

    var accommodationsMap: Map[Int, ActorRef[Messages.AccommodationCommand]] = Map.empty

    for (i <- 0 until numberOfAccommodationsNoAnswer) {
      val accommodationRef = ctx.spawnAnonymous(Accommodation(i, answer = false))
      accommodationsMap += (i -> accommodationRef)
    }
    for (i <- numberOfAccommodationsNoAnswer until numberOfAccommodations) {
      val accommodationRef = ctx.spawnAnonymous(Accommodation(i))
      accommodationsMap += (i -> accommodationRef)
    }
    val accommodationsList = accommodationsMap.values.toList
    val requesterRef = ctx.spawnAnonymous(Requester(accommodationsList))
    val reserverRef = ctx.spawnAnonymous(Reserver(accommodationsMap))

    val routes = new Routes(ctx.system, requesterRef, reserverRef, accommodationsMap)

    val serverBinding: Future[Http.ServerBinding] = Http()(untypedSystem).bindAndHandle(routes.routes, "localhost", 8080)
    serverBinding.onComplete {
      case Success(bound) =>
        println(s"Server online at http://${bound.localAddress.getHostString}:${bound.localAddress.getPort}/")
      case Failure(e) =>
        Console.err.println(s"Server could not start!")
        e.printStackTrace()
        ctx.self ! Done
    }
    Behaviors.receiveMessage {
      case Done =>
        Behaviors.stopped
    }

  }, "reservation-system")
}
