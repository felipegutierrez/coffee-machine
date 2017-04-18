package com.parallel.supervisors

import com.parallel.supervisors.BrewActor.BrewMsg
import com.parallel.supervisors.BrewActor.Espresso
import com.parallel.supervisors.BrewActor.EspressoMsg
import com.parallel.supervisors.CombineActor.CombineCappuccinoMsg
import com.parallel.supervisors.CombineActor.CombineException
import com.parallel.supervisors.FrothMilkActor.FrothMilkDoneMsg
import com.parallel.supervisors.FrothMilkActor.FrothMilkMsg
import com.parallel.supervisors.FrothMilkActor.FrothedMilk
import com.parallel.supervisors.FrothMilkActor.FrothingException
import com.parallel.supervisors.FrothMilkActor.Milk
import com.parallel.supervisors.GrindActor.CoffeeBeans
import com.parallel.supervisors.GrindActor.GrindDoneMsg
import com.parallel.supervisors.GrindActor.GrindMsg
import com.parallel.supervisors.GrindActor.GrindingException
import com.parallel.supervisors.GrindActor.GroundCoffee
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

object CappuccinoActor {

  val props = Props[CappuccinoActor]
  case class CappuccinoInit(beans: CoffeeBeans, time: Long)
  case class Cappuccino(value: String)

  class CappuccinoActor extends Actor {
    private val grindActor = context.actorOf(GrindActor.props, "GrindActor")
    private val heatWaterActor = context.actorOf(HeatWaterActor.props, "HeatWaterActor")
    private val frothMilkActor = context.actorOf(FrothMilkActor.props, "FrothMilkActor")
    private val brewActor = context.actorOf(BrewActor.props, "BrewActor")
    private val combineActor = context.actorOf(CombineActor.props, "CombineActor")
    var water: Option[Water] = None
    var groundCoffee: Option[GroundCoffee] = None
    var frothedMilk: Option[FrothedMilk] = None
    var espresso: Option[Espresso] = None
    var start: Long = 0

    override def supervisorStrategy = OneForOneStrategy() {
      case ae: GrindingException =>
        println("OneForOneStrategy -> GrindingException -> Resume")
        Resume
      case we: WaterBoilingException =>
        println("OneForOneStrategy -> WaterBoilingException -> Resume")
        Resume
      case ce: CombineException =>
        println("OneForOneStrategy -> CombineException -> Resume")
        Resume
      case fe: FrothingException =>
        println("OneForOneStrategy -> FrothingException -> Resume")
        Resume
      case _: Exception =>
        println("OneForOneStrategy -> Exception -> Restart")
        Restart
    }

    def receive = {
      case CappuccinoInit(beans, time) => {
        println(s"Starting CappuccinoInit $time with actors")
        println(s"[${grindActor.path}]")
        println(s"[${heatWaterActor.path}]")
        println(s"[${frothMilkActor.path}]")
        start = time
        grindActor ! GrindMsg(self, beans)
        heatWaterActor ! HeatWaterMsg(self, water)
        frothMilkActor ! FrothMilkMsg(self, new Milk("milk"))
        println("CappuccinoInit started")
      }
      case GrindDoneMsg(ground) => {
        println(s"GrindDoneMsg [${ground}]")
        groundCoffee = Some(ground)
        if (HeatWaterActor.temperatureOkay(water)) {
          println(s"temeprature is OK so we can brew =) with [${brewActor.path}]")
          brewActor ! BrewMsg(self, groundCoffee, water)
        }
      }
      case HeatWaterDoneMsg(w) => {
        println(s"HeatWaterDoneMsg [${w.get.temperature}]")
        water = w
        if (GrindActor.groundBeans(groundCoffee)) {
          println(s"ground beans are OK so we can brew =) with [${brewActor.path}]")
          brewActor ! BrewMsg(self, groundCoffee, water)
        }
      }
      case EspressoMsg(e) => {
        println(s"EspressoMsg [${e}]")
        espresso = Some(e)
        if (FrothMilkActor.frothedMilk(frothedMilk)) {
          println(s"milk is frothed so we can combine =) with [${combineActor.path}]")
          combineActor ! CombineCappuccinoMsg(self, espresso, frothedMilk)
        }
      }
      case FrothMilkDoneMsg(milk) => {
        println(s"FrothMilkDoneMsg [${milk}]")
        frothedMilk = Some(milk)
        if (FrothMilkActor.espresso(espresso)) {
          println(s"espresso is OK so we can combine =) with [${combineActor.path}]")
          combineActor ! CombineCappuccinoMsg(self, espresso, frothedMilk)
        }
      }
      case Cappuccino(cappuccino) => println(s"Here is your [$cappuccino] in ${System.currentTimeMillis() - start} miliseconds")
    }
  }
}