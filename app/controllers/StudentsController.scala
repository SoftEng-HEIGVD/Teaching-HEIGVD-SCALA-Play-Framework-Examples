package controllers

import dao.StudentsDAO
import javax.inject.{Inject, Singleton}
import models.Student
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class StudentsController @Inject()(cc: ControllerComponents, studentDAO: StudentsDAO) extends AbstractController(cc) {

  // Convert a Student-model object into a JsValue representation, which means that we serialize it into JSON.
  implicit val studentToJson: Writes[Student] = (
    (JsPath \ "id").write[Option[Long]] and
    (JsPath \ "firstName").write[String] and
    (JsPath \ "lastName").write[String] and
    (JsPath \ "age").write[Int] and
    (JsPath \ "isInsolent").write[Boolean]
  // Use the default 'unapply' method (which acts like a reverted constructor) of the Student case class if order to get
  // back the Student object's arguments and pass them to the JsValue.
  )(unlift(Student.unapply))

  // Convert a JsValue representation into a Student-model object, which means that we deserialize the JSON.
  implicit val jsonToStudent: Reads[Student] = (
    // In order to be valid, the student must have first and last names that are 2 characters long at least, as well as
    // an age that is greater than 0.
    (JsPath \ "id").readNullable[Long] and
    (JsPath \ "firstName").read[String](minLength[String](2)) and
    (JsPath \ "lastName").read[String](minLength[String](2)) and
    (JsPath \ "age").read[Int](min(0)) and
    (JsPath \ "isInsolent").read[Boolean]
  // Use the default 'apply' method (which acts like a constructor) of the Student case class with the JsValue in order
  // to construct a Student object from it.
  )(Student.apply _)

  /**
    * This helper parses and validates JSON using the implicit `jsonToStudent` above, returning errors if the parsed
    * json fails validation.
    */
  def validateJson[A : Reads] = parse.json.validate(
    _.validate[A].asEither.left.map(e => BadRequest(JsError.toJson(e)))
  )

  /**
    * Get the list of all existing students, then return it.
    * The Action.async is used because the request is asynchronous.
    */
  def getStudents = Action.async {
    val studentsList = studentDAO.list()
    studentsList map (s => Ok(Json.toJson(s)))
  }

  /**
    * Parse the POST request, validate the request's body, then create a new student based on the sent JSON payload, and
    * finally sends back a JSON response.
    * The action expects a request with a Content-Type header of text/json or application/json and a body containing a
    * JSON representation of the entity to create.
    */
  def createStudent = Action.async(validateJson[Student]) { implicit request =>
    // `request.body` contains a fully validated `Student` instance, since it has been validated by the `validateJson`
    // helper above.
    val student = request.body
    val createdStudent = studentDAO.insert(student)

    createdStudent.map(s =>
      Ok(
        Json.obj(
          "status" -> "OK",
          "id" -> s.id,
          "message" -> ("Student '" + s.firstName + " " + s.lastName + "' saved.")
        )
      )
    )
  }

  /**
    * Get the student identified by the given ID, then return it as JSON.
    */
  def getStudent(studentId: Long) = Action.async {
    val optionalStudent = studentDAO.findById(studentId)

    optionalStudent.map {
      case Some(s) => Ok(Json.toJson(s))
      case None =>
        // Send back a 404 Not Found HTTP status to the client if the student does not exist.
        NotFound(Json.obj(
          "status" -> "Not Found",
          "message" -> ("Student #" + studentId + " not found.")
        ))
    }
  }

  /**
    * Parse the PUT request, validate the request's body, then update the student whose ID matches with the given one,
    * based on the sent JSON payload, and finally sends back a JSON response.
    */
  def updateStudent(studentId: Long) = Action.async(validateJson[Student]) { request =>
    val newStudent = request.body

    // Try to edit the student, then return a 200 OK HTTP status to the client if everything worked.
    studentDAO.update(studentId, newStudent).map {
      case 1 => Ok(
        Json.obj(
          "status" -> "OK",
          "message" -> ("Student '" + newStudent.firstName + " " + newStudent.lastName + "' updated.")
        )
      )
      case 0 => NotFound(Json.obj(
        "status" -> "Not Found",
        "message" -> ("Student #" + studentId + " not found.")
      ))
    }
  }

  /**
    * Try to delete the student identified by the given ID, and sends back a JSON response.
    */
  def deleteStudent(studentId: Long) = Action.async {
    studentDAO.delete(studentId).map {
      case 1 => Ok(
        Json.obj(
          "status"  -> "OK",
          "message" -> ("Student #" + studentId + " deleted.")
        )
      )
      case 0 => NotFound(Json.obj(
        "status" -> "Not Found",
        "message" -> ("Student #" + studentId + " not found.")
      ))
    }
  }

}
