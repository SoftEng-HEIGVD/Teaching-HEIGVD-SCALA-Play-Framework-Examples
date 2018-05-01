package controllers

import dao.CoursesDAO
import javax.inject.{Inject, Singleton}
import models.Course
import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class CoursesController @Inject()(cc: ControllerComponents, coursesDAO: CoursesDAO) extends AbstractController(cc) {

  // Refer to the StudentsController class in order to have more explanations.
  implicit val courseToJson: Writes[Course] = (
    (JsPath \ "id").write[Option[Long]] and
    (JsPath \ "name").write[String] and
    (JsPath \ "description").write[String] and
    (JsPath \ "hasApero").writeNullable[Boolean]
  )(unlift(Course.unapply))

  implicit val jsonToCourse: Reads[Course] = (
    (JsPath \ "id").readNullable[Long] and
    (JsPath \ "name").read[String](minLength[String](3) keepAnd maxLength[String](5)) and
    (JsPath \ "description").read[String] and
    (JsPath \ "hasApero").readNullable[Boolean]
  )(Course.apply _)

  def validateJson[A : Reads] = parse.json.validate(
    _.validate[A].asEither.left.map(e => BadRequest(JsError.toJson(e)))
  )

  def getCourses = Action.async {
    val coursesList = coursesDAO.list()
    coursesList map (c => Ok(Json.toJson(c)))
  }

  def createCourse = Action.async(validateJson[Course]) { request =>
    val course = request.body
    val createdCourse = coursesDAO.insert(course)

    createdCourse.map(c =>
      Ok(
        Json.obj(
          "status" -> "OK",
          "id" -> c.id,
          "message" -> ("Course '" + c.name + "' saved.")
        )
      )
    )
  }

  def getCourse(courseId: Long) = Action.async {
    val optionalCourse = coursesDAO.findById(courseId)

    optionalCourse.map {
      case Some(c) => Ok(Json.toJson(c))
      case None =>
        NotFound(Json.obj(
          "status" -> "Not Found",
          "message" -> ("Course #" + courseId + " not found.")
        ))
    }
  }

  def updateCourse(courseId: Long) = Action.async(validateJson[Course]) { request =>
    val newCourse = request.body

    coursesDAO.update(courseId, newCourse).map {
      case 1 => Ok(
        Json.obj(
          "status" -> "OK",
          "message" -> ("Course '" + newCourse.name + "' updated.")
        )
      )
      case 0 => NotFound(Json.obj(
        "status" -> "Not Found",
        "message" -> ("Course #" + courseId + " not found.")
      ))
    }
  }

  def deleteCourse(courseId: Long) = Action.async {
    coursesDAO.delete(courseId).map {
      case 1 => Ok(
        Json.obj(
          "status"  -> "OK",
          "message" -> ("Course #" + courseId + " deleted.")
        )
      )
      case 0 => NotFound(Json.obj(
        "status" -> "Not Found",
        "message" -> ("Course #" + courseId + " not found.")
      ))
    }
  }

}
