package controllers

import dao.{CoursesDAO, StudentsDAO}
import javax.inject._
import play.api.mvc._
import play.api.routing.JavaScriptReverseRouter

import scala.concurrent.ExecutionContext.Implicits.global

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(cc: ControllerComponents, studentDAO: StudentsDAO, coursesDAO: CoursesDAO) extends AbstractController(cc) {

  val title = "Ultimate HEIG-VD Manager 2018"

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
  def index = Action.async { implicit request =>
    val studentsList = studentDAO.list()
    val coursesList = coursesDAO.list()

    // Wait for the promises to resolve, then return the list of students and courses.
    for {
      students <- studentsList
      courses <- coursesList
    } yield Ok(views.html.index(title, students, courses))
  }

  /**
    * Call the "about" html template.
    */
  def about = Action {
    Ok(views.html.about(title))
  }

}
