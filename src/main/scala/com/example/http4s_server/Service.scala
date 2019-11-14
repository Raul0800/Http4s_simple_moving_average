package com.example.http4s_server

import cats.effect.Sync
import cats.implicits.{toFoldableOps => _, _}
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.{EntityDecoder, HttpRoutes}
import org.http4s.circe.{jsonEncoder, jsonOf}
import org.http4s.dsl.Http4sDsl

import scala.annotation.tailrec

case class InpData(numbers: Array[Double], winSize: Int) {

  import InpData._

  def check: Either[String, (Array[Double], Int)] = checkCorrectData(numbers, winSize)
}

class Service[IO[_] : Sync]() extends Http4sDsl[IO] {

  import Service._

  case class OutData(response: String)

  implicit val decoderInpData: EntityDecoder[IO, InpData] = jsonOf[IO, InpData]

  def routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case req@POST -> Root => {
      for {
        outData <- req.as[InpData].map(x => x.check)
        resp <- outData match {
          case Left(x) => BadRequest(x)
          case Right(x) => {
            val res = x match {
              case (data, n) => calcMA(data, n)
            }
            Ok(OutData(res.mkString("[", ", ", "]")).asJson)
          }
        }
      } yield (resp)
    }
  }
}

object InpData {
  def checkCorrectData(numbers: Array[Double], winSize: Int): Either[String, (Array[Double], Int)] = {
    (numbers, winSize) match {
      case (x, _) if x.isEmpty => Left("List of numbers is empty")
      case (_, y) if y <= 0 || y > numbers.size => Left(s"Window size must be between 1 and ${numbers.size}")
      case (x, y) => Right(x, y)
    }
  }
}

object Service {
  def calcMA_obsolete(lstOfNum: List[Double], szOfWindow: Int): Either[String, List[Double]] = {
    (lstOfNum, szOfWindow) match {
      case (x, _) if x.isEmpty => Left("List of numbers is empty")
      case (_, y) if y <= 0 || y > lstOfNum.size => Left(s"Window size must be between 1 and ${lstOfNum.size}")
      case (_, 1) => Right(lstOfNum)
      case (x, y) => Right(x.sliding(y).map(_.sum / y).toList)
    }
  }

  //Moving average O(n)
  //f_(t) = f_(t-1) - P_(t-n)/n + P_(t)/n
  //
  //f_(t) - current calculated value
  //f_(t-1) - previous calculated value
  //P_(t-n) - (t-n) value from source array
  //P_(t) - (t) value from source array
  //n - window size
  @tailrec
  def calcMA_obsolete(pair: (Array[Double], Int), results: Array[Double] = Array.empty, pos: Int = 0): Array[Double] = {
    val numbers = pair._1
    val winSize = pair._2
    (numbers, winSize, pos) match {
      case (_, 1, _) => numbers
      case (_, n, t) if t == numbers.size => results.drop(n - 1)
      case (p, n, t) if t == 0 => calcMA_obsolete((p, n), results :+ p(t) / n, t + 1)
      case (p, n, t) if t < winSize - 1 => calcMA_obsolete((p, n), results :+ (results(t - 1) + numbers(t) / n), t + 1)
      case (p, n, t) => {
        val newVal = results(t - 1) - (if (t >= n) p(t - n) else 0) / n + p(t) / n
        calcMA_obsolete((p, n), results :+ newVal, t + 1)
      }
    }
  }

  def calcMA(data: Seq[Double], n: Int): Seq[Double] = {
    n match {
      case 1 => data
      case _ => {
        data.drop(n).foldLeft((data.take(n), Seq(data.take(n).sum / n)))((acc: (Seq[Double], Seq[Double]), num: Double) => acc match {
          case (nums, ma) => (nums.drop(1) :+ num, ma :+ (ma.last - nums.head / n + num / n))
        }) match {
          case (_, ma) => ma
        }
      }
    }
  }
}
