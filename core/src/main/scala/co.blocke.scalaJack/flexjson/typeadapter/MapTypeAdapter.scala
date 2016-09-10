package co.blocke.scalajack.flexjson.typeadapter

import co.blocke.scalajack.flexjson.{ Context, Reader, TokenType, TypeAdapter, TypeAdapterFactory, Writer }

import scala.reflect.runtime.universe.{ Type, typeOf }

object MapTypeAdapter extends TypeAdapterFactory {

  override def typeAdapter(tpe: Type, context: Context, superParamTypes: List[Type]): Option[TypeAdapter[_]] =
    if (tpe <:< typeOf[Map[_, _]]) {
      val keyType = tpe.dealias.typeArgs(0)
      val keyTypeAdapter = context.typeAdapter(keyType, keyType.typeArgs)

      val valueType = tpe.dealias.typeArgs(1)
      val valueTypeAdapter = context.typeAdapter(valueType, valueType.typeArgs)

      Some(MapTypeAdapter(keyTypeAdapter, valueTypeAdapter))
    } else {
      None
    }

}

case class MapTypeAdapter[K, V](
    keyTypeAdapter:   TypeAdapter[K],
    valueTypeAdapter: TypeAdapter[V]
) extends TypeAdapter[Map[K, V]] {

  override def read(reader: Reader): Map[K, V] =
    reader.peek match {
      case TokenType.Null ⇒
        reader.readNull()

      case TokenType.BeginObject ⇒
        val builder = Map.canBuildFrom[K, V]()

        reader.beginObject()

        while (reader.hasMoreMembers) {
          val key = keyTypeAdapter.read(reader)
          val value = valueTypeAdapter.read(reader)

          builder += key → value
        }

        reader.endObject()

        builder.result()
    }

  override def write(map: Map[K, V], writer: Writer): Unit =
    if (map == null) {
      writer.writeNull()
    } else {
      writer.beginObject()

      for ((key, value) ← map) {
        keyTypeAdapter.write(key, writer)
        valueTypeAdapter.write(value, writer)
      }

      writer.endObject()
    }

}