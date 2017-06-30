package com.parallel.fsm

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

import com.parallel.fsm.CappuccinoActor.Cappuccino
import com.parallel.fsm.CappuccinoActor.CappuccinoMsg
import com.parallel.fsm.CombineActor.CombineException
import com.parallel.fsm.FrothMilkActor.FrothingException
import com.parallel.fsm.GrassActor.Grass
import com.parallel.fsm.GrindActor.CoffeeBeans
import com.parallel.fsm.GrindActor.GrindingException
import com.parallel.fsm.HeatWaterActor.WaterBoilingException
import com.parallel.fsm.TeaActor.Tea
import com.parallel.fsm.TeaActor.TeaMsg
import com.parallel.fsm.WaterStorageActor.WaterLackException

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

  class CoffeeMachineActorFSM extends FSM[CoffeeMachineState, CoffeeMachineData] {

    startWith(Off, NoMoney)

    when(Off) {
      case Event(PowerOn, _) => goto(On) using NoMoney
      case Event(AskState, _) =>
        sender ! "State: Off"
        stay
    }

    when(On) {
      case Event(PowerOff, _)    => goto(Off) using NoMoney
      case Event(InsertMoney, _) => goto(Running) using Money
      case Event(AskState, _) =>
        sender ! "State: On"
        stay
    }

    when(Running) {
      case Event(DebitMoney, _) => goto(Idle) using NoMoney
      case Event(AskState, _) =>
        sender ! "State: Running"
        stay
    }

    when(Idle) {
      case Event(PowerOff, _)    => goto(Off) using NoMoney
      case Event(InsertMoney, _) => goto(Running) using Money
      case Event(AskState, _) =>
        sender ! "State: Idle"
        stay
    }

    whenUnhandled {
      case Event(e, s) =>
        log.warning("received unhandled request {} in state {}/{}", e, stateName, s)
        goto(Idle) using NoMoney
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

    //    private val cappuccinoActor = context.actorOf(Props(new CappuccinoActor(self)), "CappuccinoActor")
    //    private val teaActor = context.actorOf(Props(new TeaActor(self)), "TeaActor")
    //    def receive = {
    //      case CappuccinoMsg(beans, time) => {
    //        cappuccinoActor ! CappuccinoInit(beans, time)
    //        sender ! "We are making your Cappuccino..."
    //      }
    //      case Cappuccino(value) => println(value)
    //      case TeaMsg(grass, time) => {
    //        teaActor ! TeaInit(grass, time)
    //        sender ! "We are making your Tea..."
    //      }
    //      case Tea(value) => println(value)
    //    }
  }

  val system = ActorSystem("CoffeeMachineActorFSM")
  val coffeeMachineActorFSM = system.actorOf(Props(new CoffeeMachineActorFSM()), "CoffeeMachineActorFSM")

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
        case _ => println("wrong option")
      }
    }
  }

  def older() {
    val system = ActorSystem("CoffeeMachineActorFSM")
    val coffeeMachineActorFSM = system.actorOf(Props(new CoffeeMachineActorFSM()), "CoffeeMachineActorFSM")

    implicit val timeout = Timeout(3 seconds)

    val futureCappuccino: Future[Any] = coffeeMachineActorFSM ? CappuccinoMsg(new CoffeeBeans("brasilian beans"), System.currentTimeMillis())
    futureCappuccino.onSuccess {
      case Cappuccino(value) => println(s"Cappuccino onSuccess $value")
      case value: String     => println(s"onSuccess $value")
    }
    futureCappuccino.onFailure {
      case GrindingException(value)     => println(s"GrindingException $value")
      case WaterBoilingException(value) => println(s"WaterBoilingException $value")
      case WaterLackException(value)    => println(s"WaterLackException $value")
      case CombineException(value)      => println(s"CombineException $value")
      case FrothingException(value)     => println(s"FrothingException $value")
      case value                        => println(s"onFailure $value")
    }

    val futureTea: Future[Any] = coffeeMachineActorFSM ? TeaMsg(new Grass("lemon"), System.currentTimeMillis())
    futureTea.onSuccess {
      case Tea(value)    => println(s"Tea onSuccess $value")
      case value: String => println(s"onSuccess $value")
    }
    futureTea.onFailure {
      case GrindingException(value)     => println(s"GrindingException $value")
      case WaterBoilingException(value) => println(s"WaterBoilingException $value")
      case WaterLackException(value)    => println(s"WaterLackException $value")
      case CombineException(value)      => println(s"CombineException $value")
      case FrothingException(value)     => println(s"FrothingException $value")
      case value                        => println(s"onFailure $value")
    }

    Thread.sleep(30000)
    system.shutdown()
  }
}
