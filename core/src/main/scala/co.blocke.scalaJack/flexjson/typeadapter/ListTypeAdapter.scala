package co.blocke.scalajack.flexjson.typeadapter

import co.blocke.scalajack.flexjson.{ Context, TypeAdapter, TypeAdapterFactory }

import scala.reflect.runtime.universe.{ Type, typeOf }

object ListTypeAdapter extends TypeAdapterFactory {

  override def typeAdapter(tpe: Type, context: Context, superParamTypes: List[Type]): Option[TypeAdapter[_]] =
    if (tpe <:< typeOf[List[_]]) {
      val elementType = tpe.dealias.typeArgs.head
      val elementTypeAdapter = context.typeAdapter(elementType, elementType.typeArgs)

      Some(CanBuildFromTypeAdapter(List.canBuildFrom, elementTypeAdapter))
    } else {
      None
    }

}