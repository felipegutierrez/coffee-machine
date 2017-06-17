package com.spark.sort

object SeveralSorts extends App {

  val predefinedList = List(1)
  println("Type a list of number split by comma [1,2,3,5,7,10] or [0] to a predefined list: ")
  val input = scala.io.StdIn.readLine()
  // println("Your list is = [" + input + "]")

  val list = stringToList(input)
  // list.foreach(println)
  println(list)

  var end = false
  while (end == false) {
    println("Sort type [0-exit, 1-scala, 2-akka, 3-spark : ")
    val sortType = scala.io.StdIn.readLine()
    val start = System.currentTimeMillis()
    sortType match {
      case "1" =>
        print("Sort in Scala")
        val res = mergeSort(list)
        println(res)
        println("Time " + (System.currentTimeMillis() - start))
        end = true
      case "2" =>
        print("Sort in Akka")
        println("Time " + (System.currentTimeMillis() - start))
        end = true
      case "3" =>
        print("Sort in Spark")
        println("Time " + (System.currentTimeMillis() - start))
        end = true
      case "0" =>
        print("Exit")
        end = true
      case _ =>
        print("Wrong option =( Try again.... ")
    }
  }

  def stringToList(value: String): List[Int] = {
    if (value == "0") {
      List(1, 3, 98, 56, 23, 45, 12, 98, 67, 44, 14, 36, 27, 28, 19, 10, 46, 54, 76, 88, 74, 55, 35, 98, 99, 100, 2, 9, 8, 7, 6, 5, 4)
    } else {
      value.split(",").map(_.toInt).toList
    }
  }

  def mergeSort(xs: List[Int]): List[Int] = {
    val n = xs.length / 2
    if (n == 0) xs
    else {
      def merge(xs: List[Int], ys: List[Int]): List[Int] = {
        Thread.sleep(100)
        (xs, ys) match {
          case (Nil, ys) => ys
          case (xs, Nil) => xs
          case (x :: xs1, y :: ys1) =>
            if (x < y) x :: merge(xs1, ys)
            else y :: merge(xs, ys1)
        }
      }
      val (left, right) = xs splitAt (n)
      merge(mergeSort(left), mergeSort(right))
    }
  }

}
