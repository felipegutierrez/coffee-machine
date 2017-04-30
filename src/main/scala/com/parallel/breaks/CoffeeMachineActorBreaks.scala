package com.parallel.breaks

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

import com.parallel.breaks.CappuccinoActor.Cappuccino
import com.parallel.breaks.CappuccinoActor.CappuccinoInit
import com.parallel.breaks.CappuccinoActor.CappuccinoMsg
import com.parallel.breaks.CombineActor.CombineException
import com.parallel.breaks.FrothMilkActor.FrothingException
import com.parallel.breaks.GrassActor.Grass
import com.parallel.breaks.GrindActor.CoffeeBeans
import com.parallel.breaks.GrindActor.GrindingException
import com.parallel.breaks.HeatWaterActor.WaterBoilingException
import com.parallel.breaks.TeaActor.Tea
import com.parallel.breaks.TeaActor.TeaInit
import com.parallel.breaks.TeaActor.TeaMsg
import com.parallel.breaks.WaterStorageActor.WaterLackException

import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.pattern.ask
import akka.util.Timeout

object CoffeeMachineActorBreaks extends App {

  val props = Props[CoffeeMachineActorBreaks]

  class CoffeeMachineActorBreaks extends Actor {
    private val cappuccinoActor = context.actorOf(CappuccinoActor.props, "CappuccinoActor")
    private val teaActor = context.actorOf(TeaActor.props, "TeaActor")
    def receive = {
      case CappuccinoMsg(beans, time) => {
        cappuccinoActor ! CappuccinoInit(beans, time, self)
        sender ! "We are making your Cappuccino..."
      }
      case Cappuccino(value) => println(value)
      case TeaMsg(grass, time) => {
        teaActor ! TeaInit(grass, time, self)
        sender ! "We are making your Tea..."
      }
      case Tea(value) => println(value)
    }
  }

  val system = ActorSystem("CoffeeMachineActorSupervisor")
  val coffeeMachineActorBreaks = system.actorOf(CoffeeMachineActorBreaks.props, "CoffeeMachineActorBreaks")

  implicit val timeout = Timeout(3 seconds)

  val futureCappuccino: Future[Any] = coffeeMachineActorBreaks ? CappuccinoMsg(new CoffeeBeans("brasilian beans"), System.currentTimeMillis())
  futureCappuccino.onSuccess {
    case Cappuccino(value) => println(s"Cappuccino onSuccess $value")
    case value: String     => println(s"onSuccess $value")
  }
  futureCappuccino.onFailure {
    case GrindingException(value)     => println(s"GrindingException $value")
    case WaterBoilingException(value) => println(s"WaterBoilingException $value")
    case WaterLackException(value)    => println(s"WaterLackException $value")
    case CombineException(value)      => println(s"CombineException $value")
    case FrothingException(value)     => println(s"FrothingException $value")
    case value                        => println(s"onFailure $value")
  }

  val futureTea: Future[Any] = coffeeMachineActorBreaks ? TeaMsg(new Grass("lemon"), System.currentTimeMillis())
  futureTea.onSuccess {
    case Tea(value)    => println(s"Tea onSuccess $value")
    case value: String => println(s"onSuccess $value")
  }
  futureTea.onFailure {
    case GrindingException(value)     => println(s"GrindingException $value")
    case WaterBoilingException(value) => println(s"WaterBoilingException $value")
    case WaterLackException(value)    => println(s"WaterLackException $value")
    case CombineException(value)      => println(s"CombineException $value")
    case FrothingException(value)     => println(s"FrothingException $value")
    case value                        => println(s"onFailure $value")
  }

  Thread.sleep(30000)
  system.shutdown()
}
