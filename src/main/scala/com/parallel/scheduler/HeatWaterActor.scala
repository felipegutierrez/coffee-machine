package com.parallel.scheduler

import scala.concurrent.duration.DurationInt

import com.parallel.scheduler.WaterStorageActor.GetWaterAndHeatMsg
import com.parallel.scheduler.WaterStorageActor.GetWaterMsg
import com.parallel.scheduler.WaterStorageActor.HeatWaterDoneMsg
import com.parallel.scheduler.WaterStorageActor.InitWaterStorageMsg
import com.parallel.scheduler.WaterStorageActor.Water
import com.parallel.scheduler.WaterStorageActor.WaterLackException
import com.parallel.scheduler.WaterStorageActor.WaterMsg

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.OneForOneStrategy
import akka.actor.Props
import akka.actor.SupervisorStrategy.Escalate
import akka.actor.SupervisorStrategy.Restart
import akka.actor.actorRef2Scala
import akka.util.Timeout

object HeatWaterActor {

  val props = Props[HeatWaterActor]

  case class WaterBoilingException(msg: String) extends Exception(msg)

  class HeatWaterActor extends Actor {

    implicit val timeout = Timeout(10.seconds)
    var waterStorageActor: Option[ActorRef] = None

    override def supervisorStrategy = OneForOneStrategy() {
      case we: WaterLackException =>
        println(s"WaterLackException -> Escalate: [${we.getMessage}]")
        Escalate
      case e: Exception =>
        println(s"Exception Unknow -> Restart: [${e.getMessage}]")
        Restart
    }

    def receive = {
      case GetWaterAndHeatMsg(other, water) => waterStorageActor.get ! GetWaterMsg(water, other)
      case WaterMsg(water, actorRef) => {
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
      println("HeatWaterActor initWaterStorage")
      waterStorageActor = Some(context.watch(context.actorOf(WaterStorageActor.props, name = "WaterStorageActor")))
      waterStorageActor.get ! InitWaterStorageMsg
    }
  }

  def temperatureOkay(w: Water): Boolean = {
    println(s"checking temperature.... [${w.temperature}]")
    (80 to 85).contains(w.temperature)
  }
}
