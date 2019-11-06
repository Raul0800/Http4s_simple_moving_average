package com.example.http4s_server

import org.scalatest._

class ServiceTest extends FlatSpec with Matchers {
  "listOfNumbers : [ 1, -111, 2 ], sizeOfWindow : 2" should "response : Right(List(-55.0, -54.5))" in {
    val lst: List[Double] = List(1, -111, 2)
    val szOfWindow: Int = 2
    Service.calcMA(lst, szOfWindow) should be(Right(List(-55.0, -54.5)))
  }

  "listOfNumbers : [ 1, 2 ], sizeOfWindow : 1" should "response : Right(List(1, 2))" in {
    val lst: List[Double] = List(1, 2)
    val szOfWindow: Int = 1
    Service.calcMA(lst, szOfWindow) should be(Right(List(1, 2)))
  }

  "listOfNumbers : [ 1, -111, 2 ], sizeOfWindow : -2" should "response : Left(\"Window size must be between 1 and 3\")" in {
    val lst: List[Double] = List(1, -111, 2)
    val szOfWindow: Int = -2
    Service.calcMA(lst, szOfWindow) should be(Left("Window size must be between 1 and 3"))
  }

  "listOfNumbers : [ 1, -111, 2 ], sizeOfWindow : 5" should "response : Left(\"Window size must be between 1 and 3\")" in {
    val lst: List[Double] = List(1, -111, 2)
    val szOfWindow: Int = 5
    Service.calcMA(lst, szOfWindow) should be(Left("Window size must be between 1 and 3"))
  }

  "listOfNumbers : [ ], sizeOfWindow : 5" should "response : Left(\"List of numbers is empty\")" in {
    val lst: List[Double] = List()
    val szOfWindow: Int = 5
    Service.calcMA(lst, szOfWindow) should be(Left("List of numbers is empty"))
  }
}