package controllers

import javax.inject._

import play.api.mvc._
import play.api.routing.JavaScriptReverseRouter
import services.{Course, Student}

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  def javascriptRoutes = Action { implicit request =>
    Ok(
      JavaScriptReverseRouter("jsRoutes")(
        routes.javascript.StudentsController.getStudents,
        routes.javascript.StudentsController.createStudent,
        routes.javascript.StudentsController.getStudent,
        routes.javascript.StudentsController.updateStudent,
        routes.javascript.StudentsController.deleteStudent,
        routes.javascript.CoursesController.getCourses,
        routes.javascript.CoursesController.createCourse,
        routes.javascript.CoursesController.getCourse,
        routes.javascript.CoursesController.updateCourse,
        routes.javascript.CoursesController.deleteCourse
      )
    ).as("text/javascript")
  }

  /**
   * Create an Action to render an HTML page with a welcome message.
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index = Action {
    Ok(views.html.index("Ultimate HEIG-VD Manager 2018", Student.mapStudents, Course.mapCourses))
  }

}
