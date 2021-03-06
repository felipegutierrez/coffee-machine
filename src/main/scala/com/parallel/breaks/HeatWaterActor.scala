package com.parallel.breaks

import scala.concurrent.duration.DurationInt

import com.parallel.breaks.WaterStorageActor.GetWaterAndHeatMsg
import com.parallel.breaks.WaterStorageActor.GetWaterMsg
import com.parallel.breaks.WaterStorageActor.HeatWaterDoneMsg
import com.parallel.breaks.WaterStorageActor.InitWaterStorageMsg
import com.parallel.breaks.WaterStorageActor.Water
import com.parallel.breaks.WaterStorageActor.WaterLackException
import com.parallel.breaks.WaterStorageActor.WaterMsg

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.OneForOneStrategy
import akka.actor.SupervisorStrategy.Escalate
import akka.actor.SupervisorStrategy.Restart
import akka.actor.actorRef2Scala
import akka.util.Timeout

object HeatWaterActor {

  case class WaterBoilingException(msg: String) extends Exception(msg)

  class HeatWaterActor(actorRef: ActorRef) extends Actor {

    implicit val timeout = Timeout(10.seconds)
    private val waterStorageActor = context.actorOf(WaterStorageActor.props, "CombineActor")

    override def supervisorStrategy = OneForOneStrategy() {
      case we: WaterLackException =>
        println(s"WaterLackException -> Escalate: [${we.getMessage}]")
        Escalate
      case e: Exception =>
        println(s"Exception Unknow -> Restart: [${e.getMessage}]")
        Restart
    }

    def receive = {
      case GetWaterAndHeatMsg(water) => waterStorageActor ! GetWaterMsg(water)
      case WaterMsg(water) => {
        Thread.sleep(3000)
        val waterHeated = new Water(water.qtd, 85)
        println(s"hot, it's hot! sending HeatWaterDoneMsg [${waterHeated.temperature}]")
        actorRef ! HeatWaterDoneMsg(waterHeated)
      }
    }

    override def preStart() {
      println("HeatWaterActor preStart")
      initWaterStorage()
    }

    override def postRestart(reason: Throwable) {
      println("HeatWaterActor postRestart")
    }

    def initWaterStorage() {
      waterStorageActor ! InitWaterStorageMsg
    }
  }

  def temperatureOkay(w: Water): Boolean = {
    println(s"checking temperature.... [${w.temperature}]")
    (80 to 85).contains(w.temperature)
  }
}
