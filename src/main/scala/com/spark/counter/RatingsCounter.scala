package com.spark.counter

import org.apache.log4j.Level
import org.apache.log4j.Logger
import org.apache.spark.SparkContext

object RatingsCounter extends App {

  // set the log level to print only errors
  Logger.getLogger("org").setLevel(Level.ERROR)

  // create a SparkContext using every core of the local machine, named RatingsCounter
  val sc = new SparkContext("local[*]", "RatingsCounter")

  // load up each line of the ratings data into an RDD
  val lines = sc.textFile("src/main/resource/u.data", 0)

  // convert each line to s string, split it out by tabs and extract the third field.
  // The file format is userID, movieID, rating, timestamp
  val ratings = lines.map(x => x.toString().split("\t")(2))

  // count up how many times each value occurs
  val results = ratings.countByValue()

  // sort the resulting map of (rating, count) tuples
  val sortedResults = results.toSeq.sortBy(_._1)

  // print each result on its own line.
  sortedResults.foreach(println)
}
