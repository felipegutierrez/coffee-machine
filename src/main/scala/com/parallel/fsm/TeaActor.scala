package com.parallel.fsm

import scala.concurrent.duration.DurationInt

import com.parallel.fsm.CombineActor.CombineActor
import com.parallel.fsm.CombineActor.CombineException
import com.parallel.fsm.CombineActor.CombineTeaMsg
import com.parallel.fsm.FrothMilkActor.FrothingException
import com.parallel.fsm.GrassActor.Grass
import com.parallel.fsm.GrassActor.GrassActor
import com.parallel.fsm.GrassActor.GrassDoneMsg
import com.parallel.fsm.GrassActor.GrassMsg
import com.parallel.fsm.GrassActor.GroundGrass
import com.parallel.fsm.GrindActor.GrindingException
import com.parallel.fsm.HeatWaterActor.HeatWaterActor
import com.parallel.fsm.HeatWaterActor.WaterBoilingException
import com.parallel.fsm.WaterStorageActor.GetWaterAndHeatMsg
import com.parallel.fsm.WaterStorageActor.HeatWaterDoneMsg
import com.parallel.fsm.WaterStorageActor.Water
import com.parallel.fsm.WaterStorageActor.WaterLackException

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

  case class TeaInit(grass: Grass, time: Long)
  case class TeaMsg(grass: Grass, time: Long)
  case class Tea(value: String)

  class TeaActor(coffeeMachine: ActorRef) extends Actor {
    private val grassActor = context.actorOf(Props(new GrassActor(self)), "GrassActor")
    private val heatWaterActor = context.actorOf(Props(new HeatWaterActor(self)), "HeatWaterActor")
    private val combineActor = context.actorOf(Props(new CombineActor(self)), "CombineActor")
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
        println(s"CombineException -> Restart: [${ce.getMessage}]. You have to order again, sorry.")
        Restart
      case fe: FrothingException =>
        println(s"FrothingException -> Stop: [${fe.getMessage}]")
        Stop
      case e: Exception =>
        println(s"Exception Unknow -> Escalate: [${e.getMessage}]")
        Escalate
    }

    def getGrass(grass: Grass) = grassActor ! GrassMsg(grass)
    def heatWater = heatWaterActor ! GetWaterAndHeatMsg(water)

    def receive = {
      case TeaInit(grass, time) => {
        println(s"Starting CappuccinoInit $time with actors")
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
          combineActor ! CombineTeaMsg(groundGrass, water)
        }
      }
      case HeatWaterDoneMsg(w) => {
        println(s"HeatWaterDoneMsg [${w.temperature}]")
        water = w
        if (GrassActor.groundGrass(groundGrass)) {
          println(s"ground grass are OK so we can combine =) with [${combineActor.path}]")
          combineActor ! CombineTeaMsg(groundGrass, water)
        }
      }
      case Tea(tea) => {
        coffeeMachine ! Tea(s"Here is your [$tea] in ${System.currentTimeMillis() - start} miliseconds")
      }
    }
  }
}
