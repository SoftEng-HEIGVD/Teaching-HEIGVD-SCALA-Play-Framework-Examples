package dao

import javax.inject.{Inject, Singleton}
import models.Student
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

// We use a trait component here in order to share the StudentsTable class with other DAO, thanks to the inheritance.
trait StudentsComponent {
  self: HasDatabaseConfigProvider[JdbcProfile] =>

  import profile.api._

  // This class convert the database's students table in a object-oriented entity: the Student model.
  class StudentsTable(tag: Tag) extends Table[Student](tag, "STUDENTS") {
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc) // Primary key, auto-incremented
    def firstName = column[String]("FIRSTNAME")
    def lastName = column[String]("LASTNAME")
    def age = column[Int]("AGE")
    def isInsolent = column[Boolean]("ISINSOLENT")

    // Map the attributes with the model; the ID is optional.
    def * = (id.?, firstName, lastName, age, isInsolent) <> (Student.tupled, Student.unapply)
  }
}

// This class contains the object-oriented list of students and offers methods to query the data.
// A DatabaseConfigProvider is injected through dependency injection; it provides a Slick type bundling a database and
// driver. The class extends the students' query table and loads the JDBC profile configured in the application's
// configuration file.
@Singleton
class StudentsDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) extends StudentsComponent with HasDatabaseConfigProvider[JdbcProfile] {
  import profile.api._

  // Get the object-oriented list of students directly from the query table.
  val students = TableQuery[StudentsTable]

  /** Retrieve the list of students */
  def list(): Future[Seq[Student]] = {
    val query = students.sortBy(s => (s.lastName, s.firstName))
    db.run(query.result)
  }

  /** Retrieve the names (first and last names) and the age of the students, whose age is inferior of the given one,
    * then sort the results by last name, then first name */
  def findIfAgeIsInferior(age: Int): Future[Seq[(String, String, Int)]] = {
    val query = (for {
      student <- students
      if student.age < age
    } yield (student.firstName, student.lastName, student.age)).sortBy(s => (s._2, s._1))

    db.run(query.result)
  }

  /** Retrieve a student from the id. */
  def findById(id: Long): Future[Option[Student]] =
    db.run(students.filter(_.id === id).result.headOption)

  /** Insert a new student, then return it. */
  def insert(student: Student): Future[Student] = {
    val insertQuery = students returning students.map(_.id) into ((student, id) => student.copy(Some(id)))
    db.run(insertQuery += student)
  }

  /** Update a student, then return an integer that indicate if the student was found (1) or not (0). */
  def update(id: Long, student: Student): Future[Int] = {
    val studentToUpdate: Student = student.copy(Some(id))
    db.run(students.filter(_.id === id).update(studentToUpdate))
  }

  /** Delete a student, then return an integer that indicate if the student was found (1) or not (0). */
  def delete(id: Long): Future[Int] =
    db.run(students.filter(_.id === id).delete)
}
