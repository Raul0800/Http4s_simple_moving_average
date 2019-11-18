package com.example.http4s_server

import org.scalatest._

class ServiceTest extends FlatSpec with Matchers {
  //Tests for InpData.checkCorrectData

  "numbers : [ 1, -111, 2 ], winSize : 2" should "response : Right(Array(1.0, -111.0, 2.0), 2)" in {
    val numbers: Array[Double] = Array(1.0, -111.0, 2.0)
    val winSize: Int = 2
    val res: Either[String, (Array[Double], Int)] = InpData.checkCorrectData(numbers, winSize)
    res should be('right)
    res.right.get._1 should equal(Array(1.0, -111.0, 2.0))
    res.right.get._2 should equal(2)
  }

  "numbers : [ 1, -111, 2 ], winSize : -2" should "response : Left(\"Window size must be between 1 and 3\")" in {
    val numbers: Array[Double] = Array(1, -111, 2)
    val winSize: Int = -2
    val res: Either[String, (Array[Double], Int)] = InpData.checkCorrectData(numbers, winSize)
    res should be('left)
    res.left.get should equal("Window size must be between 1 and 3")
  }

  "numbers : [ ], winSize : 5" should "response : Left(\"List of numbers is empty\")" in {
    val numbers: Array[Double] = Array()
    val winSize: Int = 5
    val res: Either[String, (Array[Double], Int)] = InpData.checkCorrectData(numbers, winSize)
    res should be('left)
    res.left.get should equal("List of numbers is empty")
  }

  //Tests for InpData.checkCorrectData

  "numbers : [ 1, -111, 2 ], winSize : 2" should "response : Array(-55.0, -54.5)" in {
    val numbers: Array[Double] = Array(1, -111, 2)
    val winSize: Int = 2
    val res: Seq[Double] = Service.calcMA(numbers, winSize)
    res should equal(Array(-55.0, -54.5))
  }

  "numbers : [ 1, 2 ], winSize : 1" should "response : Array(1, 2)" in {
    val numbers: Array[Double] = Array(1, 2)
    val winSize: Int = 1
    val res: Seq[Double] = Service.calcMA(numbers, winSize)
    res should equal(Array(1, 2))
  }
}