package com.parallel.scheduler

import com.parallel.scheduler.BrewActor.Espresso
import com.parallel.scheduler.CappuccinoActor.Cappuccino
import com.parallel.scheduler.FrothMilkActor.FrothedMilk
import com.parallel.scheduler.GrassActor.GroundGrass
import com.parallel.scheduler.WaterStorageActor.Water
import com.parallel.scheduler.TeaActor.Tea
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.actorRef2Scala

object CombineActor {

  val props = Props[CombineActor]

  case class CombineException(msg: String) extends Exception(msg)
  case class CombineCappuccinoMsg(other: ActorRef, espresso: Option[Espresso], frothedMilk: Option[FrothedMilk])
  case class CombineTeaMsg(other: ActorRef, groundGrass: Option[GroundGrass], water: Water)

  class CombineActor extends Actor {
    def receive = {
      case CombineCappuccinoMsg(other, espresso, frothedMilk) => {
        println(s"combine espresso [$espresso] with frothed milk [$frothedMilk], with [${other.path}]")
        Thread.sleep(1000)
        other ! Cappuccino(s"cappuccino [$espresso] with [$frothedMilk].")
      }
      case CombineTeaMsg(other, groundGrass, hotWater) => {
        println(s"combine tea [$groundGrass] with water [${hotWater}], with [${other.path}]")
        Thread.sleep(2000)
        other ! Tea(s"tea [$groundGrass] with water [$hotWater].")
      }
    }
  }
}
