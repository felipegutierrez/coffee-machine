package com.parallel.fsm

import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

import com.parallel.fsm.CappuccinoActor.CappuccinoActor
import com.parallel.fsm.CappuccinoActor.CappuccinoInit
import com.parallel.fsm.GrassActor.Grass
import com.parallel.fsm.GrindActor.CoffeeBeans
import com.parallel.fsm.TeaActor.TeaActor
import com.parallel.fsm.TeaActor.TeaInit

import akka.actor.ActorSystem
import akka.actor.FSM
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.pattern.ask
import akka.util.Timeout

object CoffeeMachineActorFSM extends App {

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

  val system = ActorSystem("CoffeeMachineActorFSM")
  val coffeeMachineActorFSM = system.actorOf(Props(new CoffeeMachineActorFSM()), "CoffeeMachineActorFSM")

  class CoffeeMachineActorFSM extends FSM[CoffeeMachineState, CoffeeMachineData] {

    val cappuccinoActor = context.actorOf(Props(new CappuccinoActor(coffeeMachineActorFSM)), "CappuccinoActor")
    val teaActor = context.actorOf(Props(new TeaActor(self)), "TeaActor")

    var amount: Double = 0.0
    startWith(Off, NoMoney)

    when(Off) {
      case Event(PowerOn, _) => goto(On) using NoMoney
      case Event(AskState, _) =>
        sender ! s"State: Off - Total: $amount"
        stay
    }

    when(On) {
      case Event(PowerOff, _) => goto(Off) using NoMoney
      case Event(InsertMoney(c), _) =>
        amount = amount + c
        println(s"Receive money: $c . Total: $amount")
        stay
      case Event(DebitMoney(c), _) =>
        amount = amount - c
        stay
      case Event(AskState, _) =>
        sender ! s"State: On - Total: $amount"
        stay
      case Event(AskAmount, _) =>
        sender ! amount
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
        amount = amount - c
        stay
      case Event(AskState, _) =>
        sender ! s"State: Running - Total: $amount"
        stay
    }

    when(Idle) {
      case Event(PowerOff, _) => goto(Off) using NoMoney
      case Event(InsertMoney(c), _) =>
        amount = amount + c
        println(s"Receive money: $c . Total: $amount")
        goto(On) using Money
      case Event(DebitMoney(c), _) =>
        amount = amount - c
        goto(On) using Money
      case Event(AskState, _) =>
        sender ! s"State: Idle - Total: $amount"
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

  system.shutdown()

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
      print("= ?  = ")
      val input = scala.io.StdIn.readLine()
      input match {
        case "0" =>
          println("exit .....")
          flag = false
        case "1" =>
          println("Coffee Machine Status .....")
          val status: Future[Any] = coffeeMachineActorFSM ? AskState
          val result = Await.result(status, timeout.duration).asInstanceOf[String]
          println(result)
        case "2" =>
          println("Coffee Machine turn on .....")
          coffeeMachineActorFSM ? PowerOn
        case "3" =>
          println("Coffee Machine turn off .....")
          coffeeMachineActorFSM ! PowerOff
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
                coffeeMachineActorFSM ! InsertMoney(0.05)
                flagM = false
              case "2" =>
                coffeeMachineActorFSM ! InsertMoney(0.10)
                flagM = false
              case "3" =>
                coffeeMachineActorFSM ! InsertMoney(0.25)
                flagM = false
              case "4" =>
                coffeeMachineActorFSM ! InsertMoney(0.5)
                flagM = false
              case "5" =>
                coffeeMachineActorFSM ! InsertMoney(1)
                flagM = false
              case _ => println("wrong coin")
            }
          }
          println()
        case "5" =>
          val result: Future[Any] = coffeeMachineActorFSM ? AskAmount
          val amount = Await.result(result, timeout.duration).asInstanceOf[Double]
          if (amount >= 0.5) {
            coffeeMachineActorFSM ! OrderCappuccino
            coffeeMachineActorFSM ! DebitMoney(0.5)
          } else {
            println(s"There is no enogh money to order a Cappuccino. Amount: $amount")
          }
        case "6" =>
          val result: Future[Any] = coffeeMachineActorFSM ? AskAmount
          val amount = Await.result(result, timeout.duration).asInstanceOf[Double]
          if (amount >= 0.25) {
            coffeeMachineActorFSM ! OrderTea
            coffeeMachineActorFSM ! DebitMoney(0.25)
          } else {
            println(s"There is no enogh money to order a Tea. Amount: $amount")
          }
        case _ => println("wrong option")
      }
    }
  }
}
