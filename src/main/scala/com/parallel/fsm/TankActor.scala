package com.parallel.fsm

import akka.actor.{Actor, ActorRef, FSM}
import scala.concurrent.duration._
import scala.collection._

// received events
final case class FillTank()
final case class StopFillTank()
 
// states
sealed trait TankState
case object Full extends TankState
case object Empty extends TankState
 
//data
sealed trait TankData
case object NoData extends TankData

class TankActor extends FSM[TankState, TankData] {
  startWith(Empty, NoData)
 
  when(Empty) {
    case Event(FillTank, _) =>
      goto(Full) using NoData
  }
 
  when(Full, stateTimeout = 1 second) {
    case Event(StopFillTank, _) =>
      // if tank is less then 10%
      goto(Empty) using NoData
    case Event(StateTimeout, _) =>
      println("'On' state timed out, moving to 'Off'")
      goto(Empty) using NoData
  }
 
  whenUnhandled {
    case Event(e, s) =>
      log.warning("received unhandled request {} in state {}/{}", e, stateName, s)
      goto(Empty) using NoData
  }
 
  onTransition {
    case Empty -> Full => println("Moved from Off to On")
    case Full -> Empty => println("Moved from On to Off")
  }
 
  initialize()
}

