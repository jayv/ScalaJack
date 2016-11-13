package co.blocke.scalajack
package json.test.misc

import org.scalatest.{ BeforeAndAfterAll, FunSpec, GivenWhenThen }
import org.scalatest.Matchers._

case class Parrot(color: String)

case class DumpTruck(axles: Int)

case class EitherHolder[L, R](either: Either[L, R])

object MyTypes {
  type Phone = String
  type Size = String
}
import MyTypes._

class EitherSpec extends FunSpec with GivenWhenThen with BeforeAndAfterAll {

  val sj = ScalaJack()

  describe("------------------\n:  Either Tests  :\n------------------") {
    it("Left - two class types") {
      val inst: Either[Parrot, DumpTruck] = Left(Parrot("blue"))
      val js = sj.render(inst)
      assertResult("""{"_hint":"co.blocke.scalajack.json.test.misc.Parrot","color":"blue"}""") { js }
      assertResult(inst) {
        sj.read[Either[Parrot, DumpTruck]](js)
      }
    }
    it("Right - two class types") {
      val inst: Either[Parrot, DumpTruck] = Right(DumpTruck(axles = 2))
      val js = sj.render(inst)
      assertResult("""{"_hint":"co.blocke.scalajack.json.test.misc.DumpTruck","axles":2}""") { js }
      assertResult(inst) {
        sj.read[Either[Parrot, DumpTruck]](js)
      }
    }
    it("Left - class type and scalar type") {
      val inst: Either[Parrot, String] = Left(Parrot("red"))
      val js = sj.render(inst)
      assertResult("""{"_hint":"co.blocke.scalajack.json.test.misc.Parrot","color":"red"}""") { js }
      assertResult(inst) {
        sj.read[Either[Parrot, DumpTruck]](js)
      }
    }
    it("Right - class type and scalar type") {
      val inst = EitherHolder[Parrot, String](Right("quack"))
      val js = sj.render(inst)
      assertResult("""{"either":"quack"}""") { js }
      assertResult(inst) {
        sj.read[EitherHolder[Parrot, String]](js)
      }
    }
    it("Is null") {
      val inst: Either[Parrot, String] = null
      val js = sj.render(inst)
      assertResult("null") { js }
      assertResult(inst) {
        sj.read[Either[Parrot, String]](js)
      }
    }
    it("Same instance Left and Right") {
      val js = "\"foo\""
      the[java.lang.IllegalArgumentException] thrownBy sj.read[Either[String, String]](js) should have message "Types String and String are not mutually exclusive"
    }
  }
}
