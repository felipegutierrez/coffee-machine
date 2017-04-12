package com.sequential

import scala.util.Try

object CoffeeMachineSeq extends App {
  type CoffeeBeans = String
  type GroundCoffee = String
  case class Water(temperature: Int)
  type Milk = String
  type FrothedMilk = String
  type Espresso = String
  type Cappuccino = String

  // some exceptions for things that might go wrong in the individual steps
  // (we'll need some of them later, use the others when experimenting
  // with the code):
  case class GrindingException(msg: String) extends Exception(msg)
  case class FrothingException(msg: String) extends Exception(msg)
  case class WaterBoilingException(msg: String) extends Exception(msg)
  case class BrewingException(msg: String) extends Exception(msg)

  // dummy implementations of the individual steps:
  def grind(beans: CoffeeBeans): GroundCoffee = {
    println(s"ground coffee of [$beans]")
    Thread.sleep(1000)
    s"ground coffee of $beans"
  }
  def heatWater(water: Water): Water = {
    println(s"heatWater temperature [${water.temperature}]")
    Thread.sleep(2000)
    water.copy(temperature = 85)
  }
  def frothMilk(milk: Milk): FrothedMilk = {
    println(s"froth milk [$milk]")
    Thread.sleep(1000)
    s"frothed $milk"
  }
  def brew(coffee: GroundCoffee, heatedWater: Water): Espresso = {
    println(s"brew coffee [$coffee] with heated water [${heatedWater.temperature}]")
    Thread.sleep(2000)
    "espresso"
  }
  def combine(espresso: Espresso, frothedMilk: FrothedMilk): Cappuccino = {
    println(s"combine espresso [$espresso] with frothed milk [$frothedMilk]")
    Thread.sleep(1000)
    "cappuccino"
  }

  // going through these steps sequentially:
  def prepareCappuccino(): Try[Cappuccino] = for {
    ground <- Try(grind("arabica beans"))
    water <- Try(heatWater(Water(25)))
    espresso <- Try(brew(ground, water))
    foam <- Try(frothMilk("milk"))
  } yield combine(espresso, foam)

  val start: Long = System.currentTimeMillis()
  println("Starting CoffeeMachineSeq...")
  println()
  prepareCappuccino()
  println()
  val finish: Long = System.currentTimeMillis()
  println(s"Finishing CoffeeMachineSeq! ${finish - start} miliseconds")
}
