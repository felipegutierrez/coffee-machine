package com.parallel.persistent

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.actorRef2Scala

object GrassActor {
  type Grass = String
  type GroundGrass = String

  case class GrassMsg(grass: Grass)
  case class GrassDoneMsg(grass: GroundGrass)
  case class GrassException(msg: String) extends Exception(msg)

  class GrassActor(other: ActorRef) extends Actor {
    def receive = {
      case GrassMsg(grass) => {
        println("start collecting grass...")
        Thread.sleep(2000)
        println(s"finished collecting grass...  with [${other.path}]")
        other ! GrassDoneMsg(new GroundGrass(s"grass of $grass"))
      }
    }
  }

  def groundGrass(groundGrass: Option[Any]): Boolean = {
    println(s"checking groundGrass.... [${groundGrass}]")
    groundGrass match {
      case Some(value) => true
      case None        => false
    }
  }
}
