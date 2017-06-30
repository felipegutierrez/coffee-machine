package com.parallel.fsm

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

import com.parallel.fsm.CappuccinoActor.Cappuccino
import com.parallel.fsm.CappuccinoActor.CappuccinoActor
import com.parallel.fsm.CappuccinoActor.CappuccinoInit
import com.parallel.fsm.CappuccinoActor.CappuccinoMsg
import com.parallel.fsm.CombineActor.CombineException
import com.parallel.fsm.FrothMilkActor.FrothingException
import com.parallel.fsm.GrassActor.Grass
import com.parallel.fsm.GrindActor.CoffeeBeans
import com.parallel.fsm.GrindActor.GrindingException
import com.parallel.fsm.HeatWaterActor.WaterBoilingException
import com.parallel.fsm.TeaActor.Tea
import com.parallel.fsm.TeaActor.TeaActor
import com.parallel.fsm.TeaActor.TeaInit
import com.parallel.fsm.TeaActor.TeaMsg
import com.parallel.fsm.WaterStorageActor.WaterLackException

import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.pattern.ask
import akka.util.Timeout

object CoffeeMachineActorBreaks extends App {

  class CoffeeMachineActorBreaks extends Actor {
    private val cappuccinoActor = context.actorOf(Props(new CappuccinoActor(self)), "CappuccinoActor")
    private val teaActor = context.actorOf(Props(new TeaActor(self)), "TeaActor")
    def receive = {
      case CappuccinoMsg(beans, time) => {
        cappuccinoActor ! CappuccinoInit(beans, time)
        sender ! "We are making your Cappuccino..."
      }
      case Cappuccino(value) => println(value)
      case TeaMsg(grass, time) => {
        teaActor ! TeaInit(grass, time)
        sender ! "We are making your Tea..."
      }
      case Tea(value) => println(value)
    }
  }

  val system = ActorSystem("CoffeeMachineActorSupervisor")
  val coffeeMachineActorBreaks = system.actorOf(Props(new CoffeeMachineActorBreaks()), "CoffeeMachineActorBreaks")

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
