package com.example.http4s_server

import cats.effect.Sync
import cats.implicits.{toFoldableOps => _, _}
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.{EntityDecoder, HttpRoutes}
import org.http4s.circe.{jsonEncoder, jsonOf}
import org.http4s.dsl.Http4sDsl

import scala.annotation.tailrec

class Service[IO[_] : Sync]() extends Http4sDsl[IO] {

  import Service._

  case class InpData(listOfNumbers: List[Double], sizeOfWindow: Int)

  case class OutData(response: String)

  implicit val decoderInpData: EntityDecoder[IO, InpData] = jsonOf[IO, InpData]

  def routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case req@POST -> Root => {
      for {
        outData <- req.as[InpData].map(x => calcMA(x.listOfNumbers, x.sizeOfWindow))
        resp <- outData match {
          case Left(x) => BadRequest(x)
          case Right(x) => Ok(OutData(x.mkString("[", ", ", "]")).asJson)
        }
      } yield (resp)
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
  def calcMA(lstOfNum: List[Double], szOfWindow: Int, calcLst: List[Double] = List.empty, pos: Int = 0): Either[String, List[Double]] = {
    (lstOfNum, szOfWindow, pos) match {
      case (p, _, _) if p.isEmpty => Left("List of numbers is empty")
      case (_, n, _) if n <= 0 || n > lstOfNum.size => Left(s"Window size must be between 1 and ${lstOfNum.size}")
      case (_, 1, _) => Right(lstOfNum)
      case (_, n, t) if t == lstOfNum.size => Right(calcLst.drop(n - 1))
      case (p, n, t) if t == 0 => calcMA(p, n, calcLst :+ p(t) / n, t + 1)
      case (p, n, t) if t < szOfWindow - 1 => calcMA(p, n, calcLst :+ (calcLst(t - 1) + lstOfNum(t) / n), t + 1)
      case (p, n, t) => {
        val newVal = calcLst(t - 1) - (if (t >= n) p(t - n) else 0) / n + p(t) / n
        calcMA(p, n, calcLst :+ newVal, t + 1)
      }
    }
  }
}
