package com.parallel.persistent

import scala.concurrent.duration.DurationInt

import com.parallel.persistent.BrewActor.BrewActor
import com.parallel.persistent.BrewActor.BrewMsg
import com.parallel.persistent.BrewActor.Espresso
import com.parallel.persistent.BrewActor.EspressoMsg
import com.parallel.persistent.CombineActor.CombineActor
import com.parallel.persistent.CombineActor.CombineCappuccinoMsg
import com.parallel.persistent.CombineActor.CombineException
import com.parallel.persistent.FrothMilkActor.FrothMilkActor
import com.parallel.persistent.FrothMilkActor.FrothMilkDoneMsg
import com.parallel.persistent.FrothMilkActor.FrothMilkMsg
import com.parallel.persistent.FrothMilkActor.FrothedMilk
import com.parallel.persistent.FrothMilkActor.FrothingException
import com.parallel.persistent.FrothMilkActor.Milk
import com.parallel.persistent.GrindActor.CoffeeBeans
import com.parallel.persistent.GrindActor.GrindActor
import com.parallel.persistent.GrindActor.GrindDoneMsg
import com.parallel.persistent.GrindActor.GrindMsg
import com.parallel.persistent.GrindActor.GrindingException
import com.parallel.persistent.GrindActor.GroundCoffee
import com.parallel.persistent.HeatWaterActor.HeatWaterActor
import com.parallel.persistent.HeatWaterActor.WaterBoilingException
import com.parallel.persistent.WaterStorageActor.GetWaterAndHeatMsg
import com.parallel.persistent.WaterStorageActor.HeatWaterDoneMsg
import com.parallel.persistent.WaterStorageActor.Water
import com.parallel.persistent.WaterStorageActor.WaterLackException

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.OneForOneStrategy
import akka.actor.Props
import akka.actor.SupervisorStrategy.Escalate
import akka.actor.SupervisorStrategy.Restart
import akka.actor.SupervisorStrategy.Resume
import akka.actor.SupervisorStrategy.Stop
import akka.actor.actorRef2Scala

object CappuccinoActor {

  case class CappuccinoInit(beans: CoffeeBeans, time: Long)
  case class CappuccinoMsg(beans: CoffeeBeans, time: Long)
  case class Cappuccino(value: String)

  class CappuccinoActor(coffeeMachine: ActorRef) extends Actor {
    private val grindActor = context.actorOf(Props(new GrindActor(self)), "GrindActor")
    private val heatWaterActor = context.actorOf(Props(new HeatWaterActor(self)), "HeatWaterActor")
    private val frothMilkActor = context.actorOf(Props(new FrothMilkActor(self)), "FrothMilkActor")
    private val brewActor = context.actorOf(Props(new BrewActor(self)), "BrewActor")
    private val combineActor = context.actorOf(Props(new CombineActor(self)), "CombineActor")
    var water: Water = new Water(0, 20)
    var groundCoffee: Option[GroundCoffee] = None
    var frothedMilk: Option[FrothedMilk] = None
    var espresso: Option[Espresso] = None
    var start: Long = 0

    // maximum 10 restarts per 1 minute
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

    def grind(beans: CoffeeBeans) = grindActor ! GrindMsg(beans)
    def heatWater = heatWaterActor ! GetWaterAndHeatMsg(water)
    def frothMilk = frothMilkActor ! FrothMilkMsg(new Milk("milk"))
    def brew = brewActor ! BrewMsg(groundCoffee, water)
    def combine = combineActor ! CombineCappuccinoMsg(espresso, frothedMilk)

    def receive = {
      case CappuccinoInit(beans, time) => {
        println(s"Starting CappuccinoInit $time with actors")
        println(s"[${grindActor.path}]")
        println(s"[${heatWaterActor.path}]")
        println(s"[${frothMilkActor.path}]")
        start = time
        grind(beans)
        heatWater
        frothMilk
        println("CappuccinoInit started")
      }
      case GrindDoneMsg(ground) => {
        println(s"GrindDoneMsg [${ground}]")
        groundCoffee = Some(ground)
        if (HeatWaterActor.temperatureOkay(water)) {
          println(s"temeprature is OK so we can brew =) with [${brewActor.path}]")
          brew
        }
      }
      case HeatWaterDoneMsg(w) => {
        println(s"HeatWaterDoneMsg [${w.qtd}] of water [${w.temperature}] degrees")
        water = w
        if (GrindActor.groundBeans(groundCoffee)) {
          println(s"ground beans are OK so we can brew =) with [${brewActor.path}]")
          brew
        }
      }
      case EspressoMsg(e) => {
        println(s"EspressoMsg [${e}]")
        espresso = Some(e)
        if (FrothMilkActor.frothedMilk(frothedMilk)) {
          println(s"milk is frothed so we can combine =) with [${combineActor.path}]")
          combine
        }
      }
      case FrothMilkDoneMsg(milk) => {
        println(s"FrothMilkDoneMsg [${milk}]")
        frothedMilk = Some(milk)
        if (FrothMilkActor.espresso(espresso)) {
          println(s"espresso is OK so we can combine =) with [${combineActor.path}]")
          combine
        }
      }
      case Cappuccino(cappuccino) => {
        coffeeMachine ! Cappuccino(s"Here is your [$cappuccino] in ${System.currentTimeMillis() - start} miliseconds")
      }
    }
  }
}
