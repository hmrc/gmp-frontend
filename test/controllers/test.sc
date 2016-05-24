import play.api.mvc.Result
import play.api.test.FakeRequest

import scala.concurrent.Future

FakeRequest("GET", "/upload-csv/failure?error_message=VIRUS")(handler: Future[Result] => Any).queryString