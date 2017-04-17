package com.parallel

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.actorRef2Scala

object CoffeeMachineActor extends App {

  val props = Props[CoffeeMachineActor]

  type CoffeeBeans = String
  type GroundCoffee = String
  case class Water(temperature: Int)
  type Milk = String
  type FrothedMilk = String
  type Espresso = String

  case class GrindingException(msg: String) extends Exception(msg)
  case class FrothingException(msg: String) extends Exception(msg)
  case class WaterBoilingException(msg: String) extends Exception(msg)
  case class BrewingException(msg: String) extends Exception(msg)
  case class CombineException(msg: String) extends Exception(msg)

  case class CappuccinoInit(time: Long)
  case object Process

  case class GrindMsg(other: ActorRef, beans: CoffeeBeans)
  case class GrindDoneMsg(ground: GroundCoffee)
  case class BrewMsg(other: ActorRef, coffee: Option[GroundCoffee], heatedWater: Water)
  case class EspressoMsg(espresso: Espresso)
  case class HeatWaterMsg(other: ActorRef, water: Water)
  case class HeatWaterDoneMsg(water: Water)
  case class FrothMilkMsg(other: ActorRef, milk: Milk)
  case class FrothMilkDoneMsg(milk: Milk)
  case class CombineMsg(other: ActorRef, espresso: Option[Espresso], frothedMilk: Option[FrothedMilk])
  case class Cappuccino(value: String)

  object GrindActor {
    val props = Props[GrindActor]
    class GrindActor extends Actor {
      def receive = {
        case GrindMsg(other, beans) => {
          println("start grinding...")
          Thread.sleep(1000)
          if (beans == "baked beans") throw GrindingException("are you joking?")
          println(s"finished grinding...  with [${other.path}]")
          other ! GrindDoneMsg(new GroundCoffee(s"ground coffee of $beans"))
        }
      }
    }
  }

  object HeatWaterActor {
    val props = Props[HeatWaterActor]
    class HeatWaterActor extends Actor {
      def receive = {
        case HeatWaterMsg(other, water) => {
          println(s"heating the water [${water.temperature}] degrees now")
          Thread.sleep(2000)
          val w = new Water(85)
          println(s"hot, it's hot! sending HeatWaterDoneMsg [${w.temperature}] with [${other.path}]")
          other ! HeatWaterDoneMsg(w)
        }
      }
    }
  }

  object BrewActor {
    val props = Props[BrewActor]
    class BrewActor extends Actor {
      def receive = {
        case BrewMsg(other, coffee, heatedWater) => {
          println(s"happy brewing :) with water [${heatedWater.temperature}] and [${coffee}]")
          Thread.sleep(2000)
          println(s"it's brewed! with [${other.path}]")
          other ! EspressoMsg(new Espresso("espresso"))
        }
      }
    }
  }

  object FrothMilkActor {
    val props = Props[FrothMilkActor]
    class FrothMilkActor extends Actor {
      def receive = {
        case FrothMilkMsg(other, milk) => {
          println("milk frothing system engaged!")
          Thread.sleep(1000)
          println(s"shutting down milk frothing system. with [${other.path}]")
          other ! FrothMilkDoneMsg(new FrothedMilk(s"frothed $milk"))
        }
      }
    }
  }

  object CombineActor {
    val props = Props[CombineActor]
    class CombineActor extends Actor {
      def receive = {
        case CombineMsg(other, espresso, frothedMilk) => {
          println(s"combine espresso [$espresso] with frothed milk [$frothedMilk], with [${other.path}]")
          Thread.sleep(1000)
          other ! Cappuccino(s"cappuccino [$espresso] with [$frothedMilk].")
        }
      }
    }
  }

  class CoffeeMachineActor extends Actor {
    val grindActor = context.actorOf(GrindActor.props, "GrindActor")
    val heatWaterActor = context.actorOf(HeatWaterActor.props, "HeatWaterActor")
    val frothMilkActor = context.actorOf(FrothMilkActor.props, "FrothMilkActor")
    val brewActor = context.actorOf(BrewActor.props, "BrewActor")
    val combineActor = context.actorOf(CombineActor.props, "CombineActor")
    var water: Water = new Water(20)
    var groundCoffee: Option[GroundCoffee] = None
    var frothedMilk: Option[FrothedMilk] = None
    var espresso: Option[Espresso] = None
    var start: Long = 0
    def receive = {
      case CappuccinoInit(time) => {
        println(s"Starting CappuccinoInit $time with actors")
        println(s"[${grindActor.path}]")
        println(s"[${heatWaterActor.path}]")
        println(s"[${frothMilkActor.path}]")
        start = time
        grindActor ! GrindMsg(self, new CoffeeBeans("arabica beans"))
        heatWaterActor ! HeatWaterMsg(self, water)
        frothMilkActor ! FrothMilkMsg(self, new Milk("milk"))
        println("CappuccinoInit started")
      }
      case GrindDoneMsg(ground) => {
        println(s"GrindDoneMsg [${ground}]")
        groundCoffee = Some(ground)
        if (temperatureOkay(water)) {
          println(s"temeprature is OK so we can brew =) with [${brewActor.path}]")
          brewActor ! BrewMsg(self, groundCoffee, water)
        }
      }
      case HeatWaterDoneMsg(w) => {
        println(s"HeatWaterDoneMsg [${w.temperature}]")
        water = w
        if (groundBeans(groundCoffee)) {
          println(s"ground beans are OK so we can brew =) with [${brewActor.path}]")
          brewActor ! BrewMsg(self, groundCoffee, water)
        }
      }
      case EspressoMsg(e) => {
        println(s"EspressoMsg [${e}]")
        espresso = Some(e)
        if (frothedMilk(frothedMilk)) {
          println(s"milk is frothed so we can combine =) with [${combineActor.path}]")
          combineActor ! CombineMsg(self, espresso, frothedMilk)
        }
      }
      case FrothMilkDoneMsg(milk) => {
        println(s"FrothMilkDoneMsg [${milk}]")
        frothedMilk = Some(milk)
        if (espresso(espresso)) {
          println(s"espresso is OK so we can combine =) with [${combineActor.path}]")
          combineActor ! CombineMsg(self, espresso, frothedMilk)
        }
      }
      case Cappuccino(cappuccino) => println(s"Here is your [$cappuccino] in ${System.currentTimeMillis() - start} miliseconds")
    }
    def temperatureOkay(w: Water): Boolean = {
      println(s"checking temperature.... [${w.temperature}]")
      (80 to 85).contains(water.temperature)
    }
    def groundBeans(groundCoffee: Option[Any]): Boolean = {
      println(s"checking ground beans.... [${groundCoffee}]")
      groundCoffee match {
        case Some(value) => true
        case None        => false
      }
    }
    def frothedMilk(frothedMilk: Option[Any]): Boolean = {
      println(s"checking frothed Milk.... [${frothedMilk}]")
      frothedMilk match {
        case Some(value) => true
        case None        => false
      }
    }
    def espresso(espresso: Option[Any]): Boolean = {
      println(s"checking espresso.... [${espresso}]")
      espresso match {
        case Some(value) => true
        case None        => false
      }
    }
  }

  val system = ActorSystem("SimpleActorSystem")
  val coffeeMachineActor = system.actorOf(CoffeeMachineActor.props, "CoffeeMachineActor")
  coffeeMachineActor ! CappuccinoInit(System.currentTimeMillis())

  Thread.sleep(10000)
  system.shutdown()
}
