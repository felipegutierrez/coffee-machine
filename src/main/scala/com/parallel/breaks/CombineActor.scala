package com.parallel.breaks

import scala.util.Random

import com.parallel.breaks.BrewActor.Espresso
import com.parallel.breaks.CappuccinoActor.Cappuccino
import com.parallel.breaks.FrothMilkActor.FrothedMilk
import com.parallel.breaks.GrassActor.GroundGrass
import com.parallel.breaks.TeaActor.Tea
import com.parallel.breaks.WaterStorageActor.Water

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.actorRef2Scala

object CombineActor {

  val props = Props[CombineActor]

  case class CombineException(msg: String) extends Exception(msg)
  case class CombineCappuccinoMsg(espresso: Option[Espresso], frothedMilk: Option[FrothedMilk])
  case class CombineTeaMsg(groundGrass: Option[GroundGrass], water: Water)

  class CombineActor(other: ActorRef) extends Actor {
    def receive = {
      case CombineCappuccinoMsg(espresso, frothedMilk) => {
        if (Random.nextInt(2000) % 2 != 0) throw CombineException(s"The Coffee Machine could not combine [$espresso] with frothed milk [$frothedMilk]. =(")
        println(s"combine espresso [$espresso] with frothed milk [$frothedMilk], with [${other.path}]")
        Thread.sleep(2000)
        other ! Cappuccino(s"cappuccino [$espresso] with [$frothedMilk].")
      }
      case CombineTeaMsg(groundGrass, hotWater) => {
        if (Random.nextInt(2000) % 2 != 0) throw CombineException(s"The Coffee Machine could not combine [$groundGrass] with water [${hotWater}]. =(")
        println(s"combine tea [$groundGrass] with water [${hotWater}], with [${other.path}]")
        Thread.sleep(2000)
        other ! Tea(s"tea [$groundGrass] with water [$hotWater].")
      }
    }
  }
}
