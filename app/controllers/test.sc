import org.joda.time.format.ISODateTimeFormat
import org.joda.time.LocalDateTime
import play.api.libs.json._

case class Abc(name:String, timestamp :LocalDateTime )

object Abc{

  implicit val readsJodaLocalDateTime = Reads[LocalDateTime](js =>
    js.validate[String].map[LocalDateTime](dtString =>
      LocalDateTime.parse(dtString, ISODateTimeFormat.basicDateTime())
    )
  )

  implicit val writesJodaLocalDateTime = new Writes[LocalDateTime]{
    def writes(localDateTime: LocalDateTime) = Json.obj(
      "localDateTime" -> localDateTime.toString
    )
  }

  implicit val formats = Json.format[Abc]
  implicit def defaultOrdering: Ordering[Abc] = Ordering.fromLessThan(_.timestamp isAfter _.timestamp)
}
val a = Abc("a",LocalDateTime.now())
val b = Abc("b",LocalDateTime.now())
val c = Abc("c",LocalDateTime.now())

val list = List(b,c,a)

val sortedList = list.sorted

