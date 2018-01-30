package services

// Refer to the Student class in order to have more explanations.
case class Course(var name: String, var description: String, var hasApero: Option[Boolean])

object Course {
  var mapCourses = Map[Long, Course](
    1L -> Course("SCALA", "Scala rocks!", None),
    2L -> Course("AMT", "Have you ever seen an apero that huge?", Some(true))
  )
  var counter: Long = mapCourses.size

  def addCourse(course: Course): Long = {
    counter = counter + 1
    mapCourses += (counter -> course)
    counter
  }

  def editCourse(id: Long, course: Course): Boolean = {
    if (mapCourses.contains(id)) {
      mapCourses += (id -> course)
      return true
    }

    false
  }

  def removeCourse(id: Long): Boolean = {
    if (mapCourses.contains(id)) {
      mapCourses -= id
      return true
    }

    false
  }
}
