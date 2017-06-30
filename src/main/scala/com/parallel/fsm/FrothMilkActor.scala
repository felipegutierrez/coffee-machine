package com.parallel.fsm

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.actorRef2Scala

object FrothMilkActor {

  type Milk = String
  type FrothedMilk = String

  case class FrothMilkMsg(milk: Milk)
  case class FrothMilkDoneMsg(milk: Milk)
  case class FrothingException(msg: String) extends Exception(msg)

  class FrothMilkActor(other: ActorRef) extends Actor {
    def receive = {
      case FrothMilkMsg(milk) => {
        println("milk frothing system engaged!")
        Thread.sleep(3000)
        println(s"shutting down milk frothing system. with [${other.path}]")
        other ! FrothMilkDoneMsg(new FrothedMilk(s"frothed $milk"))
      }
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
