package services

// We need a case class in order to easily write JSON from a Student object and vice-versa with the default 'apply' and
// 'unapply' methods. You can find more details in the Student and Course controllers.
case class Student(var firstName: String, var lastName: String, var age: Int)

object Student {
  // Contain a map of students ([id -> student]).
  var mapStudents = Map[Long, Student](
    1L -> Student("Mathias", "Solgin", 23),
    2L -> Student("Tony", "Calf", 25),
    3L -> Student("Michel-Michel", "Michel", 52)
  )
  // Used to determine the ID of the next student.
  var counter: Long = mapStudents.size

  /**
    * Add a new student in the students collection, then return its ID.
    */
  def addStudent(student: Student): Long = {
    counter = counter + 1
    mapStudents += (counter -> student)
    counter
  }

  /**
    * Edit the student whose ID matches with the given one, if it exists.
    * @return a boolean value that indicates if the student existed (and thus has been edited) or not.
    */
  def editStudent(id: Long, student: Student): Boolean = {
    if (mapStudents.contains(id)) {
      mapStudents += (id -> student)
      return true
    }

    false
  }

  /**
    * Remove the student whose ID matches with the given one from the map, if it exists.
    * @return a boolean value that indicates if the student existed (and thus has been removed) or not.
    */
  def removeStudent(id: Long): Boolean = {
    if (mapStudents.contains(id)) {
      mapStudents -= id
      return true
    }

    false
  }
}
