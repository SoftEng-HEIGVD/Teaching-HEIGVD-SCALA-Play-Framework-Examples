package controllers

import dao.{CoursesDAO, StudentsDAO}
import javax.inject._
import play.api.data._
import play.api.data.Forms._
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

  // Declare a case class that will be used in the new student's form
  case class StudentRequest(firstName: String, lastName: String, age: Int)

  // Create a nre student form mapping, in order to map the values of the HTML form with a Scala Form
  // Need to import "play.api.data._" and "play.api.data.Forms._"
  def studentForm = Form(
    mapping(
      "firstName" -> text,
      "lastName" -> text,
      "age" -> number
    )(StudentRequest.apply)(StudentRequest.unapply)
  )

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

  /**
    * Called when the user try to post a new student from the view.
    * See https://scalaplayschool.wordpress.com/2014/08/14/lesson-4-handling-form-data-with-play-forms/ for more information
    * Be careful: if you have a "Unauthorized" error when accessing this action you have to add a "nocsrf" modifier tag
    * in the routes file above this route (see the routes file of this application for an example).
    */
  def postStudent = Action { implicit request =>
    val studentRequest = studentForm.bindFromRequest.get
    // Just display the entered values
    Ok(s"firstName: '${studentRequest.firstName}', lastName: '${studentRequest.lastName}', age: '${studentRequest.age}'")
  }

}
