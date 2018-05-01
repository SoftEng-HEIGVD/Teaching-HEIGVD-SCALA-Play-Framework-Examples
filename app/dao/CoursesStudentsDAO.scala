package dao

import javax.inject.{Inject, Singleton}
import models.CourseStudent
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

// We use a trait component here in order to share the StudentsTable class with other DAO, thanks to the inheritance.
trait CoursesStudentsComponent extends CoursesComponent with StudentsComponent {
  self: HasDatabaseConfigProvider[JdbcProfile] =>

  import profile.api._

  // This class convert the database's students table in a object-oriented entity: the Student model.
  class CoursesStudentsTable(tag: Tag) extends Table[CourseStudent](tag, "COURSES_STUDENTS") {
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc) // Primary key, auto-incremented
    def courseId = column[Long]("COURSE_ID")
    def studentId = column[Long]("STUDENT_ID")

    // Map the attributes with the model; the ID is optional.
    def * = (id.?, courseId, studentId) <> (CourseStudent.tupled, CourseStudent.unapply)
  }
}

@Singleton
class CoursesStudentsDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) extends CoursesStudentsComponent with HasDatabaseConfigProvider[JdbcProfile] {
  import profile.api._

  // Get the object-oriented list of courses-students directly from the query table.
  val coursesStudents = TableQuery[CoursesStudentsTable]

  /** Retrieve the list of courses sorted by name */
  def list(): Future[Seq[CourseStudent]] = {
    db.run(coursesStudents.result)
  }
}
