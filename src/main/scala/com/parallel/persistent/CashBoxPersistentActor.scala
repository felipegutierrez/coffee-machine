package com.parallel.persistent

import akka.persistence.PersistentActor
import akka.persistence.SaveSnapshotFailure
import akka.persistence.SaveSnapshotSuccess
import akka.persistence.SnapshotOffer

class Cmd(val coins: Double)
case class AddCoinsCmd(override val coins: Double) extends Cmd(coins)
case class RemoveCoinsCmd(override val coins: Double) extends Cmd(coins)

case class AddCoinsEvt(coins: Double)
case class RemoveCoinsEvt(coins: Double)

case object Shutdown

case class CashBoxState(amount: Double = 0.0) {
  def updated(evt: AddCoinsEvt): CashBoxState = copy(evt.coins + amount)
  def updated(evt: RemoveCoinsEvt): CashBoxState = copy(evt.coins - amount)
  def getAmount: Double = amount
  override def toString: String = amount.toString
}

class CashBoxPersistentActor extends PersistentActor {

  override def persistenceId = "cash-box-persistent-actor"

  //note : This is  mutable
  var state = CashBoxState()

  def updateState(event: AddCoinsEvt): Unit = state = state.updated(event)
  def updateState(event: RemoveCoinsEvt): Unit = state = state.updated(event)

  def getAmount: Double = state.getAmount

  val receiveRecover: Receive = {
    case evt: AddCoinsEvt    => updateState(evt)
    case evt: RemoveCoinsEvt => updateState(evt)
    case SnapshotOffer(_, snapshot: CashBoxState) => {
      println(s"offered state = $snapshot")
      state = snapshot
    }
  }

  val receiveCommand: Receive = {
    case AddCoinsCmd(data) =>
      persist(AddCoinsEvt(data)) { event =>
        updateState(event)
        context.system.eventStream.publish(event)
      }
    case RemoveCoinsCmd(data) =>
      persist(RemoveCoinsEvt(data)) { event =>
        updateState(event)
        context.system.eventStream.publish(event)
      }
    case "snap" => saveSnapshot(state)
    case SaveSnapshotSuccess(metadata) =>
      println(s"SaveSnapshotSuccess(metadata) :  metadata=$metadata")
    case SaveSnapshotFailure(metadata, reason) =>
      println("""SaveSnapshotFailure(metadata, reason) :
        metadata=$metadata, reason=$reason""")
    case "print" => println(s"$state coins")
    case "boom"  => throw new Exception("boom")
    case Shutdown => context.stop(self)
  }
}
