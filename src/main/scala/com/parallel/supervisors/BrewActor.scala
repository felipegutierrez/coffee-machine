package com.parallel.supervisors

import com.parallel.supervisors.GrindActor.GroundCoffee
import com.parallel.supervisors.HeatWaterActor.Water

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.actorRef2Scala

object BrewActor {

  type Espresso = String

  val props = Props[BrewActor]

  case class BrewMsg(other: ActorRef, coffee: Option[GroundCoffee], heatedWater: Option[Water])
  case class EspressoMsg(espresso: Espresso)
  case class BrewingException(msg: String) extends Exception(msg)

  class BrewActor extends Actor {
    def receive = {
      case BrewMsg(other, coffee, heatedWater) => {
        println(s"happy brewing :) with water [${heatedWater.get.temperature}] and [${coffee}]")
        Thread.sleep(2000)
        println(s"it's brewed! with [${other.path}]")
        other ! EspressoMsg(new Espresso("espresso"))
      }
    }
  }
}
