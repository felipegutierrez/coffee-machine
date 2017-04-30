package com.parallel.breaks

import com.parallel.breaks.GrindActor.GroundCoffee
import com.parallel.breaks.WaterStorageActor.Water

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.actorRef2Scala

object BrewActor {

  type Espresso = String

  case class BrewMsg(coffee: Option[GroundCoffee], heatedWater: Water)
  case class EspressoMsg(espresso: Espresso)
  case class BrewingException(msg: String) extends Exception(msg)

  class BrewActor(other: ActorRef) extends Actor {
    def receive = {
      case BrewMsg(coffee, heatedWater) => {
        println(s"happy brewing :) with [${heatedWater.qtd}] of water [${heatedWater.temperature}] degrees and [${coffee}]")
        Thread.sleep(2000)
        println(s"it's brewed! with [${other.path}]")
        other ! EspressoMsg(new Espresso("espresso"))
      }
    }
  }
}
