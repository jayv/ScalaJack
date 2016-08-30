package co.blocke.scalajack.flexjson.typeadapter

import co.blocke.scalajack.flexjson.{Context, Reader, TypeAdapter, TypeAdapterFactory, UnreadableJsonException, Writer}

import scala.util.{Failure, Success, Try}
import scala.reflect.runtime.universe.{Type, typeOf}

object TryTypeAdapter extends TypeAdapterFactory {

  override def typeAdapter(tpe: Type, context: Context): Option[TypeAdapter[_]] =
    if (tpe <:< typeOf[Try[_]]) {
      val valueType = tpe.typeArgs.head
      val valueTypeAdapter = context.typeAdapter(valueType)

      Some(TryTypeAdapter(valueTypeAdapter))
    } else {
      None
    }

}

case class TryTypeAdapter[T](valueTypeAdapter: TypeAdapter[T]) extends TypeAdapter[Try[T]] {

  override def read(reader: Reader): Try[T] = {
    val originalPosition = reader.position

    val attempt = Try { valueTypeAdapter.read(reader) }

    attempt match {
      case self @ Success(value) ⇒
        self

      case Failure(cause) ⇒
        reader.position = originalPosition
        reader.skipValue()

        val lengthOfUnreadableJson = reader.position - originalPosition

        val exception= new UnreadableJsonException(cause) {
          override def write(writer: Writer): Unit = {
            writer.writeRaw(reader.source, originalPosition, lengthOfUnreadableJson)
          }
        }
        Failure(exception)
    }
  }

  override def write(value: Try[T], writer: Writer): Unit =
    value match {
      case Success(v) ⇒

      case Failure(e: UnreadableJsonException) ⇒
        e.write(writer)

      case Failure(e) ⇒
        throw e
    }

}