package com.parallel.supervisors

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.actorRef2Scala

object HeatWaterActor {

  case class Water(temperature: Int)

  val props = Props[HeatWaterActor]

  case class HeatWaterMsg(other: ActorRef, water: Option[Water])
  case class HeatWaterDoneMsg(water: Option[Water])
  case class WaterBoilingException(msg: String) extends Exception(msg)

  class HeatWaterActor extends Actor {
    def receive = {
      case HeatWaterMsg(other, water) => {
        println(s"heating the water [${water.getOrElse(0)}] degrees now")
        Thread.sleep(3000)
        val w: Option[Water] = Some(new Water(85))
        println(s"hot, it's hot! sending HeatWaterDoneMsg [${w.get.temperature}] with [${other.path}]")
        other ! HeatWaterDoneMsg(w)
      }
    }
  }

  def temperatureOkay(w: Option[Water]): Boolean = {
    if (w.isDefined) {
      println(s"checking temperature.... [${w.get.temperature}]")
      (80 to 85).contains(w.get.temperature)
    } else {
      println(s"temperature not defined yet =(")
    }
    false
  }
}