package co.blocke.scalajack
package json.test.misc

import org.scalatest.{ BeforeAndAfterAll, FunSpec, GivenWhenThen }
import org.scalatest.Matchers._

case class Parrot(color: String)

case class DumpTruck(axles: Int)

case class EitherHolder[L, R](either: Either[L, R])

case class Chair(numLegs: Int)
case class Table(numLegs: Int)

class EitherSpec extends FunSpec with GivenWhenThen with BeforeAndAfterAll {

  val sj = ScalaJack()

  describe("------------------\n:  Either Tests  :\n------------------") {
    it("Left - two class types") {
      val inst: Either[Parrot, DumpTruck] = Left(Parrot("blue"))
      val js = sj.render(inst)
      assertResult("""{"color":"blue"}""") { js }
      assertResult(inst) {
        sj.read[Either[Parrot, DumpTruck]](js)
      }
    }
    it("Right - two class types") {
      val inst: Either[Parrot, DumpTruck] = Right(DumpTruck(axles = 2))
      val js = sj.render(inst)
      assertResult("""{"axles":2}""") { js }
      assertResult(inst) {
        sj.read[Either[Parrot, DumpTruck]](js)
      }
    }
    it("Left - class type and scalar type") {
      val inst: Either[Parrot, String] = Left(Parrot("red"))
      val js = sj.render(inst)
      assertResult("""{"color":"red"}""") { js }
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
      assertResult(Right(null)) {
        sj.read[Either[Parrot, String]](js)
      }
    }
    it("Same instance Left and Right") {
      val js = "\"foo\""
      the[java.lang.IllegalArgumentException] thrownBy sj.read[Either[String, String]](js) should have message "Types String and String are not mutually exclusive"
    }
    it("Different classes with identical fields--favor Right") {
      val js = """{"numLegs":4}"""
      assertResult(sj.read[Either[Chair, Table]](js)) { Right(Table(4)) }
    }
    it("Handles traits - Right") {
      val inst = EitherHolder[String, Pet](Right(Dog("Fido", 13)))
      val js = sj.render(inst)
      assertResult("""{"either":{"_hint":"co.blocke.scalajack.json.test.misc.Dog","name":"Fido","kind":13}}""")(js)
      assertResult(inst) {
        sj.read[EitherHolder[String, Pet]](js)
      }
    }
    it("Handles traits - Left") {
      val inst = EitherHolder[Pet, String](Left(Dog("Fido", 13)))
      val js = sj.render(inst)
      assertResult("""{"either":{"_hint":"co.blocke.scalajack.json.test.misc.Dog","name":"Fido","kind":13}}""")(js)
      assertResult(inst) {
        sj.read[EitherHolder[Pet, String]](js)
      }
    }
    it("Neither value works") {
      val js = "25"
      val msg = """Parsed value fits neither class class java.lang.String nor boolean
        |25
        |^""".stripMargin
      the[java.lang.RuntimeException] thrownBy sj.read[Either[String, Boolean]](js) should have message msg
    }
  }
}
