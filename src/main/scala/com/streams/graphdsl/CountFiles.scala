package com.streams.graphdsl

import java.io.File

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.ClosedShape
import akka.stream.Inlet
import akka.stream.Outlet
import akka.stream.scaladsl.Broadcast
import akka.stream.scaladsl.Flow
import akka.stream.scaladsl.GraphDSL
import akka.stream.scaladsl.Merge
import akka.stream.scaladsl.RunnableGraph
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source

object CountFiles extends App {

  def getListOfFiles(f: File): Array[File] = {
    val these = f.listFiles
    these ++ these.filter(_.isDirectory).flatMap(getListOfFiles)
  }

  val listAllFiles: Iterator[String] = getListOfFiles(new File("/home/felipe/Documentos")).map(f => f.getAbsolutePath).iterator

  // implicit actor system
  implicit val system = ActorSystem("CountFilesStream")

  // implicit actor materializer
  implicit val materializer = ActorMaterializer()

  val g = RunnableGraph.fromGraph(GraphDSL.create() { implicit builder =>
    import GraphDSL.Implicits._

    // Source
    val source: Outlet[String] = builder.add(Source.fromIterator(() => listAllFiles)).out
    // Sink
    val sink: Inlet[Any] = builder.add(Sink.foreach(println)).in

    // split the processing
    val bcast = builder.add(Broadcast[String](2))
    val merge = builder.add(Merge[String](2))

    // Flows
    val f1, f4 = Flow[String]
    val f2 = Flow[String].filter(f => f.endsWith("pdf"))
    val f3 = Flow[String].filter(f => f.endsWith("doc"))

    // Graph
    source ~> f1 ~>
      bcast ~> f2 ~> merge ~> f4 ~> sink
    bcast ~> f3 ~> merge

    ClosedShape
  })

  g.run()
}
