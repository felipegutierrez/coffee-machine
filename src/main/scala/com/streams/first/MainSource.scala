package com.streams.first

import java.nio.file.Paths
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import akka.Done
import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.IOResult
import akka.stream.ThrottleMode
import akka.stream.scaladsl.FileIO
import akka.stream.scaladsl.Flow
import akka.stream.scaladsl.Keep
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source
import akka.util.ByteString
import scala.BigInt
import scala.math.BigInt.int2bigInt

final case class Author(handle: String)

final case class Hashtag(name: String)

final case class Tweet(author: Author, timestamp: Long, body: String) {
  def hashtags: Set[Hashtag] =
    body.split(" ").collect { case t if t.startsWith("#") => Hashtag(t) }.toSet
}

object MainSource extends App {

  val akkaTag = Hashtag("#akka")

  val tweets: Source[Tweet, NotUsed] = Source(
    Tweet(Author("rolandkuhn"), System.currentTimeMillis, "#akka rocks!") ::
      Tweet(Author("patriknw"), System.currentTimeMillis, "#akka !") ::
      Tweet(Author("bantonsson"), System.currentTimeMillis, "#akka !") ::
      Tweet(Author("drewhk"), System.currentTimeMillis, "#akka !") ::
      Tweet(Author("ktosopl"), System.currentTimeMillis, "#akka on the rocks!") ::
      Tweet(Author("mmartynas"), System.currentTimeMillis, "wow #akka !") ::
      Tweet(Author("akkateam"), System.currentTimeMillis, "#akka rocks!") ::
      Tweet(Author("bananaman"), System.currentTimeMillis, "#bananas rock!") ::
      Tweet(Author("appleman"), System.currentTimeMillis, "#apples rock!") ::
      Tweet(Author("drama"), System.currentTimeMillis, "we compared #apples to #oranges!") ::
      Nil)

  val source: Source[Int, NotUsed] = Source(1 to 100)

  /**
   * The resulting blueprint is a Sink[String, Future[IOResult]],
   * which means that it accepts strings as its input and when materialized
   * it will create auxiliary information of type Future[IOResult]
   */
  def lineSink(filename: String): Sink[String, Future[IOResult]] =
    Flow[String]
      .map(s => ByteString(s + "\n"))
      .toMat(FileIO.toPath(Paths.get(filename)))(Keep.right)

  // this is the Actor system and its materializer
  implicit val system = ActorSystem("reactive-tweets")
  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher

  // here is where we start to use the source of tweets. we filter
  val done: Future[Done] = tweets
    .filterNot(_.hashtags.contains(akkaTag))
    .mapConcat(_.hashtags)
    .map(_.name.toUpperCase)
    .runWith(Sink.foreach(println))
  done.onComplete(_ => println("done"))

  val tweetsResult: Future[IOResult] = tweets
    .filterNot(_.hashtags.contains(akkaTag))
    .mapConcat(_.hashtags)
    .map(_.name.toUpperCase)
    .map(num => ByteString(s"$num\n"))
    .runWith(FileIO.toPath(Paths.get("src/main/output/tweets.txt")))

  tweetsResult.onComplete(_ => println("done to the file."))

  val factorials = source.scan(BigInt(1))((acc, next) => acc * next)
  val resultFactorial: Future[IOResult] =
    factorials
      .map(num => ByteString(s"$num\n"))
      .runWith(FileIO.toPath(Paths.get("src/main/output/resultFactorial.txt")))

  // source.runForeach(i => println(i))(materializer)

  val resultFactorialToSink: Future[IOResult] =
    factorials
      .map(_.toString)
      .runWith(lineSink("src/main/output/resultFactorialToSink.txt"))

  resultFactorialToSink.onComplete(_ => println("done resultFactorialToSink"))
  println("done to the file-inline.")

  val resultFactorialThrottle: Future[Done] = factorials
    .zipWith(Source(0 to 100))((num, idx) => s"$idx! = $num")
    .throttle(1, 1.second, 1, ThrottleMode.shaping)
    .runForeach(println)

  resultFactorialThrottle.onComplete(_ => println("resultFactorialThrottle"))

  val resultFactorialThrottleToSink: Future[IOResult] = factorials
    .zipWith(Source(0 to 100))((num, idx) => s"$idx! = $num")
    .throttle(1, 1.second, 1, ThrottleMode.shaping)
    .runWith(lineSink("src/main/output/resultFactorialThrottleToSink.txt"))

  resultFactorialThrottleToSink.onComplete(_ => system.terminate())
}
