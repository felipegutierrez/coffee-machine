package com.parallel.scheduler

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

import com.parallel.scheduler.CappuccinoActor.Cappuccino
import com.parallel.scheduler.CappuccinoActor.CappuccinoInit
import com.parallel.scheduler.CappuccinoActor.CappuccinoMsg
import com.parallel.scheduler.GrassActor.Grass
import com.parallel.scheduler.GrindActor.CoffeeBeans
import com.parallel.scheduler.TeaActor.Tea
import com.parallel.scheduler.TeaActor.TeaInit
import com.parallel.scheduler.TeaActor.TeaMsg

import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.pattern.ask
import akka.util.Timeout

object CoffeeMachineActorScheduler extends App {

  val props = Props[CoffeeMachineActorScheduler]

  class CoffeeMachineActorScheduler extends Actor {
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
  val coffeeMachineActorScheduler = system.actorOf(CoffeeMachineActorScheduler.props, "CoffeeMachineActorScheduler")

  implicit val timeout = Timeout(3 seconds)

  val futureCappuccino = coffeeMachineActorScheduler ? CappuccinoMsg(new CoffeeBeans("brasilian beans"), System.currentTimeMillis())
  val resultCappuccino = Await.result(futureCappuccino, timeout.duration).asInstanceOf[String]
  println(s"sent message CappuccinoMsg: resultCappuccino")

  val futureTea = coffeeMachineActorScheduler ? TeaMsg(new Grass("lemon"), System.currentTimeMillis())
  val resultTea = Await.result(futureTea, timeout.duration).asInstanceOf[String]
  println(s"sent message TeaMsg: resultTea")

  Thread.sleep(30000)
  system.shutdown()
}
