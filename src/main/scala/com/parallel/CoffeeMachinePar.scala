package com.parallel

import java.util.concurrent.TimeoutException

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

object CoffeeMachinePar extends App {
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

  def temperatureOkay(water: Water): Future[Boolean] = Future {
    println("we're in the future!")
    (80 to 85).contains(water.temperature)
  }

  //  val nestedFuture: Future[Future[Boolean]] = heatWater(Water(25)).map {
  //    water => temperatureOkay(water)
  //  }
  //  val flatFuture: Future[Boolean] = heatWater(Water(25)).flatMap {
  //    water => temperatureOkay(water)
  //  }

  //  val acceptable: Future[Boolean] = for {
  //    heatedWater <- heatWater(Water(25))
  //    okay <- temperatureOkay(heatedWater)
  //  } yield okay

  def grind(beans: CoffeeBeans): Future[GroundCoffee] = Future {
    println("start grinding...")
    Thread.sleep(1000)
    if (beans == "baked beans") throw GrindingException("are you joking?")
    println("finished grinding...")
    s"ground coffee of $beans"
  }

  def heatWater(water: Water): Future[Water] = Future {
    println("heating the water now")
    Thread.sleep(2000)
    println("hot, it's hot!")
    water.copy(temperature = 85)
  }

  def frothMilk(milk: Milk): Future[FrothedMilk] = Future {
    println("milk frothing system engaged!")
    Thread.sleep(1000)
    println("shutting down milk frothing system")
    s"frothed $milk"
  }

  def brew(coffee: GroundCoffee, heatedWater: Water): Future[Espresso] = Future {
    println("happy brewing :)")
    Thread.sleep(2000)
    println("it's brewed!")
    "espresso"
  }

  def combine(espresso: Espresso, frothedMilk: FrothedMilk): Cappuccino = {
    println(s"combine espresso [$espresso] with frothed milk [$frothedMilk]")
    Thread.sleep(1000)
    "cappuccino"
  }

  def prepareCappuccinoSequentially(): Future[Cappuccino] = {
    for {
      ground <- grind("arabica beans")
      water <- heatWater(Water(20))
      foam <- frothMilk("milk")
      espresso <- brew(ground, water)
    } yield combine(espresso, foam)
  }

  def prepareCappuccino(): Future[Cappuccino] = {
    val groundCoffee = grind("arabica beans")
    val heatedWater = heatWater(Water(20))
    val frothedMilk = frothMilk("milk")
    for {
      ground <- groundCoffee
      water <- heatedWater
      foam <- frothedMilk
      espresso <- brew(ground, water)
    } yield combine(espresso, foam)
  }

  var start: Long = System.currentTimeMillis()
  println("Starting CoffeeMachine Parallel...")
  println("I only have 7 seconds")
  println()
  println("Starting cappuccino sequentially")
  try {
    val futureCappuccinoSequentially = prepareCappuccinoSequentially()
    Await.result(futureCappuccinoSequentially, 7 second)
  } catch {
    case te: TimeoutException => println("Too long to prepare a simple cappuccino. I am going home.")
    case e: Exception         => println(s"Error on preparing the cappuccino : $e")
  }
  var end: Long = System.currentTimeMillis()
  println(s"Finishing cappuccino sequentially! ${end - start} miliseconds")
  println()

  start = System.currentTimeMillis()
  println("Starting cappuccino parallel")
  println("I only have 7 seconds")
  val futureCappuccinoParallel = prepareCappuccino()
  Await.result(futureCappuccinoParallel, 7 second)
  end = System.currentTimeMillis()
  println(s"Finishing cappuccino Parallel! ${end - start} miliseconds")
  println()
  println("Finishing CoffeeMachine parallel")
}