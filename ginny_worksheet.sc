import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatter.ofLocalizedDateTime
import java.time.{LocalDate, format}
import scala.util.{Failure, Success, Try}

//val inputDateFormatter = format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
//println(inputDateFormatter)

//private def protectedDateConvert(date: String): Option[String] = {
//  val tryConverting = Try(LocalDate.parse(date, format.DateTimeFormatter.ofPattern(DATE_FORMAT)))
//
//  tryConverting match {
//    case Success(convertedDate) => Some(convertedDate)
////    case Failure(e) =>
////      logger.warn(s"[BulkCreationService][protectedDateConvert] ${e.getMessage}", e)
////      None
//}
//}
//println(protectedDateConvert("03-23-1979"))
val DATE_FORMAT: String = "yyyy-MM-dd"
val date: LocalDate = LocalDate.now
val testFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-dd-MM")
val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MM-dd-yyyy")
val text: String = date.format(formatter)
val parsedDate: LocalDate = LocalDate.parse(text, formatter)
//println(date)
//println(formatter)
println(text)
println(parsedDate)