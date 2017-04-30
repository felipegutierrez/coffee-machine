package com.parallel.breaks

import scala.concurrent.duration.DurationInt

import com.parallel.breaks.WaterStorageActor.system

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.actorRef2Scala

object WaterStorageActor {

  val props = Props[WaterStorageActor]

  case class Water(qtd: Int, temperature: Int)
  case class GetWaterMsg(water: Water, actorRef: ActorRef)
  case class WaterMsg(water: Water, actorRef: ActorRef)
  case class FillWaterMsg()
  case class InitWaterStorageMsg()
  case class HeatWaterMsg(other: ActorRef, water: Water)
  case class GetWaterAndHeatMsg(other: ActorRef, water: Water)
  case class HeatWaterDoneMsg(water: Water)

  case class WaterLackException(msg: String) extends Exception(msg)

  private var waterStorageQtd = -1
  val system = ActorSystem("WaterStorageActor")
  val waterStorageActor = system.actorOf(Props[WaterStorageActor], "WaterStorageActor")
  val capacity: Long = 50

  class WaterStorageActor extends Actor {
    def receive = {
      case InitWaterStorageMsg => WaterStorageActor.initWaterStorage()
      case GetWaterMsg(water, actorRef) => {
        this.synchronized {
          if (waterStorageQtd < 4) {
            throw WaterLackException(s"There is not enough water in the WaterStorage: [${waterStorageQtd}]")
          } else if (waterStorageQtd >= 4) {
            for (x <- 1 to 4) {
              println(s"decrementing storage $x")
              waterStorageQtd -= 1
            }
            Thread.sleep(100)
            sender ! WaterMsg(Water(4, 20), actorRef)
          } else {
            println("waterStorageQtd not defined yet.")
          }
        }
      }
      case FillWaterMsg => {
        println(s"Filling WaterStorage [${waterStorageQtd}] ....")
        if (waterStorageQtd < capacity) waterStorageQtd += 1
      }
    }
  }

  def initWaterStorage() {
    if (waterStorageQtd == -1) {
      println(s"InitWaterStorageMsg initWaterStorage()")
      waterStorageQtd = 0
      import system.dispatcher
      println(s"waterStorageActor [${waterStorageActor.path}]")
      val cancellable = system.scheduler.schedule(0 milliseconds, 3000 milliseconds, waterStorageActor, FillWaterMsg)
    }
  }
}
