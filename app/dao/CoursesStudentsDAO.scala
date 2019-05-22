package dao

import javax.inject.{Inject, Singleton}
import models.{Course, CourseStudent, Student}
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

    def course = foreignKey("COURSE_FK", courseId, courses)(_.id)
    def student = foreignKey("STUDENT_FK", studentId, students)(_.id)

    // Map the attributes with the model; the ID is optional.
    def * = (id.?, courseId, studentId) <> (CourseStudent.tupled, CourseStudent.unapply)
  }

  lazy val coursesStudents = TableQuery[CoursesStudentsTable]
}

@Singleton
class CoursesStudentsDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) extends CoursesStudentsComponent with HasDatabaseConfigProvider[JdbcProfile] {
  import profile.api._

  def list(): Future[Seq[CourseStudent]] = {
    db.run(coursesStudents.result)
  }

  def listInvitations(): Future[Map[Student, Seq[Course]]] = {
    // Thanks to foreign keys
    val query = for {
      courseStudent <- coursesStudents
      course <- courseStudent.course
      if course.hasApero
      student <- courseStudent.student
      if !student.isInsolent
    } yield (course, student)

    // Without foreign keys
//    val query = for {
//      courseStudent <- coursesStudents
//      course <- courses if course.id === courseStudent.courseId
//      if course.hasApero
//      student <- students if student.id === courseStudent.studentId
//      if !student.isInsolent
//    } yield (course, student)

    for {
      pair <- db.run(query.result)
    } yield pair
      .groupBy(_._2) // Regroup by student
      .mapValues(_.map(_._1)) // List only courses
  }
}
