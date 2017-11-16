package com.streams.first

import scala.concurrent.Future

import akka.Done
import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source

object SimpleStreamExample extends App {

  implicit val system = ActorSystem("Sys")
  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher

  val numbers = 1 to 50

  //We create a Source that will iterate over the number sequence
  val numberSource: Source[Int, NotUsed] = Source.fromIterator(() => numbers.iterator)

  //Only let pass even numbers through the Flow
  val isEvenFlow: Flow[Int, Int, NotUsed] = Flow[Int].filter((num) => num % 2 == 0)

  //Create a Source of even random numbers by combining the random number Source with the even number filter Flow
  // val evenNumbersSource: Source[Int, NotUsed] = numberSource.via(isEvenFlow)

  //A Sink that will write its input onto the console
  val consoleSink: Sink[Int, Future[Done]] = Sink.foreach[Int](println)

  //Connect the Source with the Sink and run it using the materializer
  // evenNumbersSource.runWith(consoleSink)
  val resultNumberSource: Future[Done] = numberSource
    .via(isEvenFlow)
    .runWith(consoleSink)

  resultNumberSource.onComplete(_ => system.terminate())
}
