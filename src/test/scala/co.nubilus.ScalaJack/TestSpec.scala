package co.nubilus.scalajack
package test

import org.scalatest.{ FunSpec, GivenWhenThen, BeforeAndAfterAll }
import org.scalatest.Matchers._

class TestSpec extends FunSpec with GivenWhenThen with BeforeAndAfterAll {

	val data = Map("mymap"->Map("hey"->17,"you"->21),"nest"->Two("Nest!",true),"num"->"B","maybe"->Some("wow"),"name"->"Greg","flipflop"->true,"big"->99123986123L,"more"->List(Two("x",false),Two("y",true)),"stuff"->List("a","b"),"num"->Num.C,"age"->46)

	describe("====================\n| -- JSON Tests -- |\n====================") {
		it( "Serialize simple object to JSON -- all supported data types" ) {
			val a = ScalaJack.poof[One]( data )
			val js = ScalaJack.render(a)
			js should equal( """{"name":"Greg","stuff":["a","b"],"more":[{"foo":"x","bar":false},{"foo":"y","bar":true}],"nest":{"foo":"Nest!","bar":true},"maybe":"wow","mymap":{"hey":17,"you":21},"flipflop":true,"big":99123986123,"num":"C","age":46}""" )
		}
		it( "De-serialize simple object from JSON -- all supported data types" ) {
			val a = ScalaJack.poof[One]( data )
			val js = ScalaJack.render(a)
			js should equal( """{"name":"Greg","stuff":["a","b"],"more":[{"foo":"x","bar":false},{"foo":"y","bar":true}],"nest":{"foo":"Nest!","bar":true},"maybe":"wow","mymap":{"hey":17,"you":21},"flipflop":true,"big":99123986123,"num":"C","age":46}""" )
			val b = ScalaJack.read[One](js)
			b should equal( a )
		}
		it( "Handle empty Lists & Maps") {
			val four = Four(List[String](), Map[String,Int]())
			val js = ScalaJack.render(four)
			js should equal( """{"stuff":[],"things":{}}""" )
			ScalaJack.read[Four](js) should equal( four )
		}
		it( "Traits" ) {
			val t = Three("three",Num.A,Wow1("foo",17))
			val js2 = ScalaJack.render(t)
			js2 should equal( """{"name":"three","two":"A","pp":{"_hint":"co.nubilus.scalajack.test.Wow1","a":"foo","b":17}}""" )
			val u = ScalaJack.read[Three](js2)
			u should equal( t )
		}
		// it( "MongoKey Annotation (_id field generation) - switch on" ) {
		// 	val five = Five("Fred",Two("blah",true))
		// 	val js = ScalaJack.render(five,true)
		// 	js should equal( """{"_id":"Fred","two":{"foo":"blah","bar":true}}""" )
		// 	ScalaJack.read[Five](js) should equal( five )
		// }
		// it( "MongoKey Annotation (_id field generation) - switch off" ) {
		// 	val five = Five("Fred",Two("blah",true))
		// 	val js = ScalaJack.render(five)
		// 	js should equal( """{"name":"Fred","two":{"foo":"blah","bar":true}}""" )
		// 	ScalaJack.read[Five](js) should equal( five )
		// }

		// ------------- Nested Combinations
		it( "Serialize list of lists of case classes" ) {
			val ln = ListList("Fred", List(List(Animal("mouse", 4), Animal("bug", 6)), List(Animal("whale", 0), Animal("elephant", 4))))
			val js = ScalaJack.render(ln)
			js should equal( """{"name":"Fred","stuff":[[{"name":"mouse","legs":4},{"name":"bug","legs":6}],[{"name":"whale","legs":0},{"name":"elephant","legs":4}]]}""" )
			ScalaJack.read[ListList](js) should equal( ln )
		}
		it( "Serialize list of lists of lists of case classes" ) {
			val ln = ListListList("Fred",
				List(
					List(
						List(
							Animal("mouse", 4),
							Animal("bug", 6)),
						List(
							Animal("whale", 0),
							Animal("elephant", 4))),
					List(
						List(
							Animal("millipede", 1000),
							Animal("slug", 0)),
						List(
							Animal("bird", 2),
							Animal("tiger", 4)))))
			val js = ScalaJack.render(ln)
			js should equal( """{"name":"Fred","stuff":[[[{"name":"mouse","legs":4},{"name":"bug","legs":6}],[{"name":"whale","legs":0},{"name":"elephant","legs":4}]],[[{"name":"millipede","legs":1000},{"name":"slug","legs":0}],[{"name":"bird","legs":2},{"name":"tiger","legs":4}]]]}""" )
			ScalaJack.read[ListListList](js) should equal( ln ) 
		}
		// NOTE: If your list has a None it it, this will be lost upon re-marshal from JSON as JSON has no representation
		//       for a None (it's simply missing from the list).
		it( "Serialize list of option of case classes" ) {
			val lop = ListOpt("Jenny", List(Some(Animal("mouse", 4)), None, Some(Animal("whale", 0))))
			val js = ScalaJack.render(lop)
			js should equal( """{"name":"Jenny","stuff":[{"name":"mouse","legs":4},{"name":"whale","legs":0}]}""" )
			ScalaJack.read[ListOpt](js) should equal( lop.copy(stuff = lop.stuff.filter(_.isDefined) ) )
		}
		it( "Serialize list of map of case classes" ) {
			val lm = ListMap("Jenny", List(Map("a" -> Animal("mouse", 4)), Map("b" -> Animal("whale", 0))))
			val js = ScalaJack.render(lm)
			js should equal( """{"name":"Jenny","stuff":[{"a":{"name":"mouse","legs":4}},{"b":{"name":"whale","legs":0}}]}""" )
			ScalaJack.read[ListMap](js) should equal( lm )
		}
		it( "Serialize an option of list of case classes" ) {
			val oln = OpList("Wow", Some(List(Animal("mouse", 4), Animal("bug", 6))))
			val js = ScalaJack.render(oln)
			js should equal( """{"name":"Wow","opList":[{"name":"mouse","legs":4},{"name":"bug","legs":6}]}""" )
			ScalaJack.read[OpList](js) should equal( oln )
		}
		it( "Serialize an option of nested list of case classes" ) {
			val oln = OpListList("Yay", Some(List(List(Animal("mouse", 4), Animal("bug", 6)), List(Animal("whale", 0), Animal("elephant", 4)))))
			val js = ScalaJack.render(oln)
			js should equal( """{"name":"Yay","opListList":[[{"name":"mouse","legs":4},{"name":"bug","legs":6}],[{"name":"whale","legs":0},{"name":"elephant","legs":4}]]}""" )
			ScalaJack.read[OpListList](js) should equal( oln )
		}
		it( "Serialize an option of map of case classes" ) {
			val om = OpMap("Wow", Some(Map("hello" -> (Animal("mouse", 4)))))
			val js = ScalaJack.render(om)
			js should equal( """{"name":"Wow","opMap":{"hello":{"name":"mouse","legs":4}}}""" )
			ScalaJack.read[OpMap](js) should equal( om )
			val om2 = OpMap("Wow", None)
			val js2 = ScalaJack.render(om2)
			js2 should equal( """{"name":"Wow"}""" )
			ScalaJack.read[OpMap](js2) should equal( om2 )
		}
		it( "Serialize a nested option of case classes" ) {
			val oop = OpOp("Oops", Some(Some(Animal("mouse", 4))))
			val js = ScalaJack.render(oop)
			js should equal( """{"name":"Oops","opts":{"name":"mouse","legs":4}}""" )
			ScalaJack.read[OpOp](js) should equal( oop )
			val oop2 = OpOp("Oops", None)
			val js2 = ScalaJack.render(oop2)
			js2 should equal( """{"name":"Oops"}""" )
			ScalaJack.read[OpOp](js2) should equal( oop2 )
		}
		it( "Serialize a map of list of case classes" ) {
			val mln = MapList("Bob", Map("Mike" -> List(Animal("mouse", 4), Animal("bug", 6)), "Sally" -> List(Animal("whale", 0), Animal("elephant", 4))))
			val js = ScalaJack.render(mln)
			js should equal( """{"name":"Bob","mapList":{"Mike":[{"name":"mouse","legs":4},{"name":"bug","legs":6}],"Sally":[{"name":"whale","legs":0},{"name":"elephant","legs":4}]}}""" )
			ScalaJack.read[MapList](js) should equal( mln )
		}
		it( "Serialize a map of nested lists of case classes" ) {
			val mln = MapListList("Bob", Map("Everyone" -> List(List(Animal("mouse", 4), Animal("bug", 6)), List(Animal("whale", 0), Animal("elephant", 4)))))
			val js = ScalaJack.render(mln)
			js should equal( """{"name":"Bob","mapList":{"Everyone":[[{"name":"mouse","legs":4},{"name":"bug","legs":6}],[{"name":"whale","legs":0},{"name":"elephant","legs":4}]]}}""" )
			ScalaJack.read[MapListList](js) should equal( mln )
		}
		it( "Serialize a map of option of case classes" ) {
			val a: Option[Animal] = None
			val mln = MapOpt("Bob", Map("things" -> Some(Animal("mouse", 4)), "otherthings" -> a))
			val js = ScalaJack.render(mln)
			js should equal( """{"name":"Bob","mapOpt":{"things":{"name":"mouse","legs":4}}}""" )
			ScalaJack.read[MapOpt](js) should equal( mln.copy(mapOpt = mln.mapOpt.filter({ case (k, v) => v.isDefined })) )
		}
		it( "Serialize a map of map of case classes" ) {
			val mm = MapMap("Bob", Map("things" -> Map("a" -> Animal("mouse", 4), "b" -> Animal("horse", 4)), "stuff" -> Map("c" -> Animal("sloth", 2))))
			val js = ScalaJack.render(mm)
			js should equal( """{"name":"Bob","mapmap":{"things":{"a":{"name":"mouse","legs":4},"b":{"name":"horse","legs":4}},"stuff":{"c":{"name":"sloth","legs":2}}}}""" )
			ScalaJack.read[MapMap](js) should equal( mm )
		}
	}
}