package com.parallel.supervisors

import com.parallel.supervisors.CombineActor.CombineException
import com.parallel.supervisors.CombineActor.CombineTeaMsg
import com.parallel.supervisors.GrassActor.Grass
import com.parallel.supervisors.GrassActor.GrassDoneMsg
import com.parallel.supervisors.GrassActor.GrassException
import com.parallel.supervisors.GrassActor.GrassMsg
import com.parallel.supervisors.GrassActor.GroundGrass
import com.parallel.supervisors.HeatWaterActor.HeatWaterDoneMsg
import com.parallel.supervisors.HeatWaterActor.HeatWaterMsg
import com.parallel.supervisors.HeatWaterActor.Water
import com.parallel.supervisors.HeatWaterActor.WaterBoilingException

import akka.actor.Actor
import akka.actor.OneForOneStrategy
import akka.actor.Props
import akka.actor.SupervisorStrategy.Restart
import akka.actor.SupervisorStrategy.Resume
import akka.actor.actorRef2Scala

object TeaActor {

  val props = Props[TeaActor]
  case class TeaInit(grass: Grass, time: Long)
  case class Tea(value: String)

  class TeaActor extends Actor {
    private val grassActor = context.actorOf(GrassActor.props, "GrassActor")
    private val heatWaterActor = context.actorOf(HeatWaterActor.props, "HeatWaterActor")
    private val combineActor = context.actorOf(CombineActor.props, "CombineActor")
    var water: Option[Water] = None
    var groundGrass: Option[GroundGrass] = None
    var start: Long = 0

    override def supervisorStrategy = OneForOneStrategy() {
      case ae: GrassException =>
        println("OneForOneStrategy -> GrassException -> Resume")
        Resume
      case we: WaterBoilingException =>
        println("OneForOneStrategy -> WaterBoilingException -> Resume")
        Resume
      case ce: CombineException =>
        println("OneForOneStrategy -> CombineException -> Resume")
        Resume
      case _: Exception =>
        println("OneForOneStrategy -> Exception -> Restart")
        Restart
    }

    def receive = {
      case TeaInit(grass, time) => {
        println(s"Starting CappuccinoInit $time with actors")
        println(s"[${grassActor.path}]")
        println(s"[${heatWaterActor.path}]")
        start = time
        grassActor ! GrassMsg(self, grass)
        heatWaterActor ! HeatWaterMsg(self, water)
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
        println(s"HeatWaterDoneMsg [${w.get.temperature}]")
        water = w
        if (GrassActor.groundGrass(groundGrass)) {
          println(s"ground grass are OK so we can combine =) with [${combineActor.path}]")
          combineActor ! CombineTeaMsg(self, groundGrass, water)
        }
      }
      case Tea(tea) => println(s"Here is your [$tea] in ${System.currentTimeMillis() - start} miliseconds")
    }
  }
}
