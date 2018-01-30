package controllers

import javax.inject.{Inject, Singleton}

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._
import play.api.mvc.{AbstractController, ControllerComponents}
import services.Course

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class CoursesController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  // Refer to the StudentsController class in order to have more explanations.
  implicit val courseToJson: Writes[Course] = (
    (JsPath \ "name").write[String] and
    (JsPath \ "description").write[String] and
    (JsPath \ "hasApero").writeNullable[Boolean]
  )(unlift(Course.unapply))

  implicit val jsonToCourse: Reads[Course] = (
    (JsPath \ "name").read[String](minLength[String](3) keepAnd maxLength[String](5)) and
    (JsPath \ "description").read[String] and
    (JsPath \ "hasApero").readNullable[Boolean]
  )(Course.apply _)

  def validateJson[A : Reads] = parse.json.validate(
    _.validate[A].asEither.left.map(e => BadRequest(JsError.toJson(e)))
  )

  def getCourses = Action {
    val jsonCoursesList = Json.toJson(Course.mapCourses)
    Ok(jsonCoursesList)
  }

  def createCourse = Action(validateJson[Course]) { request =>
    val course = request.body
    val id = Course.addCourse(course)

    Ok(
      Json.obj(
        "status"  -> "OK",
        "id"      -> id,
        "message" -> ("Course '" + course.name + "' saved.")
      )
    )
  }

  def getCourse(courseId: Long) = Action {
    if (Course.mapCourses.contains(courseId)) {
      val jsonCourse = Json.toJson(Course.mapCourses.get(courseId))
      Ok(jsonCourse)
    } else {
      NotFound(Json.obj(
        "status" -> "Not Found",
        "message" -> ("Course #" + courseId + " not found.")
      ))
    }
  }

  def updateCourse(courseId: Long) = Action(validateJson[Course]) { request =>
    val newCourse = request.body

    if (Course.editCourse(courseId, newCourse)) {
      Ok(
        Json.obj(
          "status" -> "OK",
          "message" -> ("Course '" + newCourse.name + "' updated.")
        )
      )
    } else {
      NotFound(Json.obj(
        "status" -> "Not Found",
        "message" -> ("Course #" + courseId + " not found.")
      ))
    }
  }

  def deleteCourse(courseId: Long) = Action {
    if (Course.removeCourse(courseId)) {
      Ok(
        Json.obj(
          "status"  -> "OK",
          "message" -> ("Course #" + courseId + " deleted.")
        )
      )
    } else {
      NotFound(Json.obj(
        "status" -> "Not Found",
        "message" -> ("Course #" + courseId + " not found.")
      ))
    }
  }

}
