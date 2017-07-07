package com.parallel.persistent

import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

import com.parallel.persistent.CappuccinoActor.CappuccinoActor
import com.parallel.persistent.CappuccinoActor.CappuccinoInit
import com.parallel.persistent.GrassActor.Grass
import com.parallel.persistent.GrindActor.CoffeeBeans
import com.parallel.persistent.TeaActor.TeaActor
import com.parallel.persistent.TeaActor.TeaInit
import com.parallel.persistent.WaterStorageActor.AskWaterMsg

import akka.actor.ActorSystem
import akka.actor.FSM
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.pattern.ask
import akka.util.Timeout

object CoffeeMachinePersistentActor extends App {

  // received events
  final case class PowerOn()
  final case class PowerOff()
  final case class InsertMoney(coins: Double)
  final case class DebitMoney(coins: Double)
  final case class AskState()
  final case class AskAmount()
  final case class OrderCappuccino()
  final case class OrderTea()

  // states
  sealed trait CoffeeMachineState
  case object On extends CoffeeMachineState
  case object Off extends CoffeeMachineState
  case object Running extends CoffeeMachineState
  case object Idle extends CoffeeMachineState

  //data
  sealed trait CoffeeMachineData
  case object Money extends CoffeeMachineData
  case object NoMoney extends CoffeeMachineData

  val system = ActorSystem("CoffeeMachinePersistentActor")
  val coffeeMachinePersistentActor = system.actorOf(Props(new CoffeeMachinePersistentActor()), "CoffeeMachinePersistentActor")
  val waterStorageActor = system.actorOf(WaterStorageActor.props, "WaterStorageActor")
  val cashBoxPersistentActor = system.actorOf(Props(classOf[CashBoxPersistentActor]), "CashBoxPersistentActor")

  class CoffeeMachinePersistentActor extends FSM[CoffeeMachineState, CoffeeMachineData] {

    val cappuccinoActor = context.actorOf(Props(new CappuccinoActor(coffeeMachinePersistentActor)), "CappuccinoActor")
    val teaActor = context.actorOf(Props(new TeaActor(self)), "TeaActor")

    startWith(Off, NoMoney)

    when(Off) {
      case Event(PowerOn, _) => goto(On) using NoMoney
      case Event(AskState, _) =>
        cashBoxPersistentActor ! "print"
        stay
    }

    when(On) {
      case Event(PowerOff, _) => goto(Off) using NoMoney
      case Event(InsertMoney(c), _) =>
        cashBoxPersistentActor ! AddCoinsCmd(c)
        cashBoxPersistentActor ! "print"
        stay
      case Event(DebitMoney(c), _) =>
        cashBoxPersistentActor ! RemoveCoinsCmd(c)
        cashBoxPersistentActor ! "print"
        stay
      case Event(AskState, _) =>
        cashBoxPersistentActor ! "print"
        stay
      case Event(AskAmount, _) =>
        cashBoxPersistentActor ! "print"
        stay
      case Event(OrderCappuccino, _) =>
        cappuccinoActor ! CappuccinoInit(new CoffeeBeans("brasilian beans"), System.currentTimeMillis())
        sender ! "We are making your Cappuccino..."
        stay
      case Event(OrderTea, _) =>
        teaActor ! TeaInit(new Grass("lemon"), System.currentTimeMillis())
        sender ! "We are making your Tea..."
        stay
    }

    when(Running) {
      case Event(DebitMoney(c), _) =>
        cashBoxPersistentActor ! RemoveCoinsCmd(c)
        stay
      case Event(AskState, _) =>
        cashBoxPersistentActor ! "print"
        stay
    }

    when(Idle) {
      case Event(PowerOff, _) => goto(Off) using NoMoney
      case Event(InsertMoney(c), _) =>
        cashBoxPersistentActor ! AddCoinsCmd(c)
        cashBoxPersistentActor ! "print"
        goto(On) using Money
      case Event(DebitMoney(c), _) =>
        cashBoxPersistentActor ! RemoveCoinsCmd(c)
        cashBoxPersistentActor ! "print"
        goto(On) using Money
      case Event(AskState, _) =>
        cashBoxPersistentActor ! "print"
        goto(On) using NoMoney
    }

    whenUnhandled {
      case Event(e, s) =>
        log.warning("received unhandled request {} in state {}/{}", e, stateName, s)
        stay
    }

    onTransition {
      case Off -> On       => println("Moved from Off to On")
      case On -> Running   => println("Moved from On to Running")
      case On -> Off       => println("Moved from On to Off")
      case Running -> Idle => println("Moved from Running to Idle")
      case Idle -> Running => println("Moved from Idle to Running")
      case Idle -> Off     => println("Moved from Idle to Off")
    }

    initialize()
  }

  menu

  cashBoxPersistentActor ! Shutdown

  // system.shutdown()
  system.terminate()

  def menu(): Unit = {
    implicit val timeout = Timeout(3 seconds)
    var flag = true
    while (flag) {
      Thread.sleep(1000)
      println("===================== Menu =====================")
      println("= 0  = exit")
      println("= 1  = Coffee Machine Status")
      println("= 2  = Coffee Machine turn on")
      println("= 3  = Coffee Machine turn off")
      println("= 4  = Coffee Machine insert money")
      println("= 5  = Order Cappuccino")
      println("= 6  = Order Tea")
      println("= 7  = Check Water storage")
      print("= ?  = ")
      val input = scala.io.StdIn.readLine()
      input match {
        case "0" =>
          println("exit .....")
          flag = false
        case "1" =>
          println("Coffee Machine Status .....")
          coffeeMachinePersistentActor ! AskState
        // val status: Future[Any] = coffeeMachinePersistentActor ? AskState
        // val result = Await.result(status, timeout.duration).asInstanceOf[String]
        // println(result)
        case "2" =>
          println("Coffee Machine turn on .....")
          coffeeMachinePersistentActor ? PowerOn
        case "3" =>
          println("Coffee Machine turn off .....")
          coffeeMachinePersistentActor ! PowerOff
        case "4" =>
          var flagM = true
          while (flagM) {
            println("How much? 0->out 1->5 Cents 2->10 Cents 3->25 Cents 4->50 Cents 5->1 Euro")
            val money = scala.io.StdIn.readLine()
            money match {
              case "0" =>
                println("out...")
                flagM = false
              case "1" =>
                coffeeMachinePersistentActor ! InsertMoney(0.05)
                flagM = false
              case "2" =>
                coffeeMachinePersistentActor ! InsertMoney(0.10)
                flagM = false
              case "3" =>
                coffeeMachinePersistentActor ! InsertMoney(0.25)
                flagM = false
              case "4" =>
                coffeeMachinePersistentActor ! InsertMoney(0.5)
                flagM = false
              case "5" =>
                coffeeMachinePersistentActor ! InsertMoney(1)
                flagM = false
              case _ => println("wrong coin")
            }
          }
          println()
        case "5" =>
          val result: Future[Any] = coffeeMachinePersistentActor ? AskAmount
          val amount = Await.result(result, timeout.duration).asInstanceOf[Double]
          if (amount >= 0.5) {
            coffeeMachinePersistentActor ! OrderCappuccino
            coffeeMachinePersistentActor ! DebitMoney(0.5)
          } else {
            println(s"There is no enogh money to order a Cappuccino. Amount: $amount")
          }
        case "6" =>
          val result: Future[Any] = coffeeMachinePersistentActor ? AskAmount
          val amount = Await.result(result, timeout.duration).asInstanceOf[Double]
          if (amount >= 0.25) {
            coffeeMachinePersistentActor ! OrderTea
            coffeeMachinePersistentActor ! DebitMoney(0.25)
          } else {
            println(s"There is no enogh money to order a Tea. Amount: $amount")
          }
        case "7" => waterStorageActor ! AskWaterMsg
        case _   => println("wrong option")
      }
    }
  }
}
