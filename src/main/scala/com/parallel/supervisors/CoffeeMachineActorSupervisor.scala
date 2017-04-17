package com.parallel.supervisors

import com.parallel.supervisors.CappuccinoActor.CappuccinoInit
import com.parallel.supervisors.GrassActor.Grass
import com.parallel.supervisors.GrindActor.CoffeeBeans
import com.parallel.supervisors.TeaActor.TeaInit

import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.actorRef2Scala

object CoffeeMachineActorSupervisor extends App {

  val props = Props[CoffeeMachineActorSupervisor]

  class CoffeeMachineActorSupervisor extends Actor {
    private val cappuccinoActor = context.actorOf(CappuccinoActor.props, "CappuccinoActor")
    private val teaActor = context.actorOf(TeaActor.props, "TeaActor")
    def receive = {
      case CappuccinoInit(beans, time) => cappuccinoActor ! CappuccinoInit(beans, time)
      case TeaInit(grass, time)        => teaActor ! TeaInit(grass, time)
    }
  }

  val system = ActorSystem("SimpleActorSystem")
  val coffeeMachineActorSupervisor = system.actorOf(CoffeeMachineActorSupervisor.props, "CoffeeMachineActorSupervisor")
  coffeeMachineActorSupervisor ! CappuccinoInit(new CoffeeBeans("baked beans"), System.currentTimeMillis())
  println("sent message CappuccinoInit")
  coffeeMachineActorSupervisor ! TeaInit(new Grass("lemon"), System.currentTimeMillis())
  println("sent message TeaInit")

  Thread.sleep(30000)
  system.shutdown()
}
