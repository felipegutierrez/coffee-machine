package com.spark.counter

import org.apache.log4j.Level
import org.apache.log4j.Logger
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext

object RatingsCounter extends App {

  Logger.getLogger("org").setLevel(Level.ERROR)

  val conf = new SparkConf().setAppName("RatingsCounter")
  val sc = new SparkContext("local[*]", "RatingsCounter")
}
