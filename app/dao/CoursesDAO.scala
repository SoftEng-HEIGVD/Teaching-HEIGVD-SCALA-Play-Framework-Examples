package dao

import scala.concurrent.Future
import javax.inject.{Inject, Singleton}
import models.{Course, Student}
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext

trait CoursesComponent {
  self: HasDatabaseConfigProvider[JdbcProfile] =>

  import profile.api._

  // This class convert the database's courses table in a object-oriented entity: the Course model.
  class CoursesTable(tag: Tag) extends Table[Course](tag, "COURSES") {
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc) // Primary key, auto-incremented
    def name = column[String]("NAME")
    def description = column[String]("DESCRIPTION")
    def hasApero = column[Option[Boolean]]("HASAPERO") // Optional field

    // Map the attributes with the model; the ID is optional.
    def * = (id.?, name, description, hasApero) <> (Course.tupled, Course.unapply)
  }

  lazy val courses = TableQuery[CoursesTable]

}

// This class contains the object-oriented list of courses and offers methods to query the data.
// A DatabaseConfigProvider is injected through dependency injection; it provides a Slick type bundling a database and
// driver. The class extends the courses' query table and loads the JDBC profile configured in the application's
// configuration file.
@Singleton
class CoursesDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext)
  extends CoursesComponent with StudentsComponent with CoursesStudentsComponent with HasDatabaseConfigProvider[JdbcProfile] {
  import profile.api._

  /** Retrieve the list of courses sorted by name */
  def list(): Future[Seq[Course]] = {
    val query = courses.sortBy(s => s.name)
    db.run(query.result)
  }

  /** Retrieve the names of the courses only */
  def namesList(): Future[Seq[String]] = {
    val query = for {
      course <- courses
    } yield course.name

    db.run(query.result)
  }

  /** Retrieve a course from the id. */
  def findById(id: Long): Future[Option[Course]] =
    db.run(courses.filter(_.id === id).result.headOption)

  /** Get the students associated with the given course's ID. */
  def getStudentsOfCourse(id: Long): Future[Seq[Student]] = {
    val query = for {
      courseStudent <- coursesStudents
      student <- courseStudent.student
    } yield student

    db.run(query.result)
  }

  /** Insert a new course, then return it. */
  def insert(course: Course): Future[Course] = {
    val insertQuery = courses returning courses.map(_.id) into ((course, id) => course.copy(Some(id)))
    db.run(insertQuery += course)
  }

  /** Update a course, then return an integer that indicates if the course was found (1) or not (0). */
  def update(id: Long, course: Course): Future[Int] = {
    val courseToUpdate: Course = course.copy(Some(id))
    db.run(courses.filter(_.id === id).update(courseToUpdate))
  }

  /** Delete a course, then return an integer that indicates if the course was found (1) or not (0) */
  def delete(id: Long): Future[Int] =
    db.run(courses.filter(_.id === id).delete)
}