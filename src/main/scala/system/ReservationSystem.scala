package system

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.{Done, actor}
import system.accommodation.Accommodation
import system.client.Client
import system.network.Routes
import system.requester.Requester

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

object ReservationServer extends App {
  val system = ActorSystem[Done](Behaviors.setup { ctx =>
    implicit val untypedSystem: actor.ActorSystem = ctx.system.classicSystem
    implicit val materializer: ActorMaterializer = ActorMaterializer()(ctx.system.classicSystem)
    implicit val ec: ExecutionContextExecutor = ctx.system.executionContext

    val accommodationRef = ctx.spawnAnonymous(Accommodation())
    val accommodation2Ref = ctx.spawnAnonymous(Accommodation())
    val accommodationList = List(accommodationRef, accommodation2Ref)
    val requesterRef = ctx.spawnAnonymous(Requester(accommodationList))
    val clientActorRef = ctx.spawnAnonymous(Client(requesterRef))

    val routes = new Routes(ctx.system, clientActorRef, accommodationList)

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
