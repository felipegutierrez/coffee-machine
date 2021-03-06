package com.parallel.scheduler

import scala.concurrent.duration.DurationInt

import com.parallel.scheduler.CombineActor.CombineException
import com.parallel.scheduler.CombineActor.CombineTeaMsg
import com.parallel.scheduler.FrothMilkActor.FrothingException
import com.parallel.scheduler.GrassActor.Grass
import com.parallel.scheduler.GrassActor.GrassDoneMsg
import com.parallel.scheduler.GrassActor.GrassMsg
import com.parallel.scheduler.GrassActor.GroundGrass
import com.parallel.scheduler.GrindActor.GrindingException
import com.parallel.scheduler.HeatWaterActor.WaterBoilingException
import com.parallel.scheduler.WaterStorageActor.GetWaterAndHeatMsg
import com.parallel.scheduler.WaterStorageActor.HeatWaterDoneMsg
import com.parallel.scheduler.WaterStorageActor.Water
import com.parallel.scheduler.WaterStorageActor.WaterLackException

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.OneForOneStrategy
import akka.actor.Props
import akka.actor.SupervisorStrategy.Escalate
import akka.actor.SupervisorStrategy.Restart
import akka.actor.SupervisorStrategy.Resume
import akka.actor.SupervisorStrategy.Stop
import akka.actor.actorRef2Scala

object TeaActor {

  val props = Props[TeaActor]
  case class TeaInit(grass: Grass, time: Long, actorRef: ActorRef)
  case class TeaMsg(grass: Grass, time: Long)
  case class Tea(value: String)

  class TeaActor extends Actor {
    private val grassActor = context.actorOf(GrassActor.props, "GrassActor")
    private val heatWaterActor = context.actorOf(HeatWaterActor.props, "HeatWaterActor")
    private val combineActor = context.actorOf(CombineActor.props, "CombineActor")
    var coffeeMachine: Option[ActorRef] = None
    var water: Water = new Water(0, 20)
    var groundGrass: Option[GroundGrass] = None
    var start: Long = 0

    override def supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
      case ae: GrindingException =>
        println(s"GrindingException -> Resume: [${ae.getMessage}]")
        Resume
      case we: WaterBoilingException =>
        println(s"WaterBoilingException -> Resume: [${we.getMessage}]")
        Resume
      case we: WaterLackException =>
        println(s"WaterLackException -> Resume HeatWaterActor: [${we.getMessage}]")
        Thread.sleep(1000)
        heatWater
        Resume
      case ce: CombineException =>
        println(s"CombineException -> Restart: [${ce.getMessage}]")
        Restart
      case fe: FrothingException =>
        println(s"FrothingException -> Stop: [${fe.getMessage}]")
        Stop
      case e: Exception =>
        println(s"Exception Unknow -> Escalate: [${e.getMessage}]")
        Escalate
    }

    def getGrass(grass: Grass) = grassActor ! GrassMsg(self, grass)
    def heatWater = heatWaterActor ! GetWaterAndHeatMsg(self, water)

    def receive = {
      case TeaInit(grass, time, actorRef) => {
        println(s"Starting CappuccinoInit $time with actors")
        if (!coffeeMachine.isDefined) coffeeMachine = Some(actorRef)
        println(s"[${grassActor.path}]")
        println(s"[${heatWaterActor.path}]")
        start = time
        getGrass(grass)
        heatWater
        println("CappuccinoInit started")
      }
      case GrassDoneMsg(ground) => {
        println(s"GrassDoneMsg [${ground}]")
        groundGrass = Some(ground)
        if (HeatWaterActor.temperatureOkay(water)) {
          println(s"temeperature is OK so we can combine =) with [${combineActor.path}]")
          combineActor ! CombineTeaMsg(self, groundGrass, water)
        }
      }
      case HeatWaterDoneMsg(w) => {
        println(s"HeatWaterDoneMsg [${w.temperature}]")
        water = w
        if (GrassActor.groundGrass(groundGrass)) {
          println(s"ground grass are OK so we can combine =) with [${combineActor.path}]")
          combineActor ! CombineTeaMsg(self, groundGrass, water)
        }
      }
      case Tea(tea) => {
        coffeeMachine.get ! Tea(s"Here is your [$tea] in ${System.currentTimeMillis() - start} miliseconds")
      }
    }
  }
}
