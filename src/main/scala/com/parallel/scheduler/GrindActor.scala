package com.parallel.scheduler

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.actorRef2Scala

object GrindActor {
  type CoffeeBeans = String
  type GroundCoffee = String

  val props = Props[GrindActor]

  case class GrindMsg(other: ActorRef, beans: CoffeeBeans)
  case class GrindDoneMsg(ground: GroundCoffee)
  case class GrindingException(msg: String) extends Exception(msg)

  class GrindActor extends Actor {
    def receive = {
      case GrindMsg(other, beans) => {
        println("start grinding...")
        Thread.sleep(3000)
        if (beans == "baked beans") throw GrindingException("are you joking?")
        println(s"finished grinding...  with [${other.path}]")
        other ! GrindDoneMsg(new GroundCoffee(s"ground coffee of $beans"))
      }
    }
  }

  def groundBeans(groundCoffee: Option[Any]): Boolean = {
    println(s"checking ground beans.... [${groundCoffee}]")
    groundCoffee match {
      case Some(value) => true
      case None        => false
    }
  }
}
