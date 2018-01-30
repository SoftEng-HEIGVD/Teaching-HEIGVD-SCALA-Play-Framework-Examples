$(function() {
    // Make the students grid editable.
    const editableGridStudents = new EditableGrid("GridStudents", {
        // Called when some value has been modified.
        modelChanged: (rowId, colId, oldValue, newValue, row) => {
            const studentId = parseInt(row.attributes.studentId.value);
            const router = jsRoutes.controllers.StudentsController.updateStudent(studentId);

            // Make a PUT request to the Student controller in order to update the student.
            $.ajax({
                method: router.method,
                url: router.url,
                contentType: 'application/json',
                // Ugly hack to collect the student's new attributes.
                data: JSON.stringify({
                    firstName: $(`#row-student-${rowId} > .student-first-name`).text(),
                    lastName: $(`#row-student-${rowId} > .student-last-name`).text(),
                    age: parseInt($(`#row-student-${rowId} > .student-age`).text())
                })
            }).done((data, textStatus, jqXHR) => {
                console.log(`Student #${studentId} successfully updated.`)
            }).fail((jqXHR, textStatus, errorThrown) => {
                console.log(jqXHR, textStatus, errorThrown);
                alert(`An error occurred when updating the student: ${JSON.stringify(jqXHR.responseJSON)}.`);
            });
        }
    });

    // Build and load the metadata of the students grid
    editableGridStudents.load({ metadata: [
            { name: "firstName", datatype: "string", editable: true },
            { name: "lastName", datatype: "string", editable: true },
            { name: "age", datatype: "integer", editable: true }
        ]});

    // Then attach the object to the HTML table and render it.
    editableGridStudents.attachToHTMLTable('table-students');
    editableGridStudents.renderGrid();

    // Same for the courses
    const editableGridCourses = new EditableGrid("GridCourses", {
        modelChanged: (rowId, colId, oldValue, newValue, row) => {
            const courseId = parseInt(row.attributes.courseId.value);
            const router = jsRoutes.controllers.CoursesController.updateCourse(courseId);
            let courseData = {
                name: $(`#row-course-${rowId} > .course-name`).text(),
                description: $(`#row-course-${rowId} > .course-description`).text()
            };

            if ($(`#row-course-${rowId} > .course-has-apero`).attr('realValue') !== 'null') {
                courseData.hasApero = $(`#row-course-${rowId} > .course-has-apero`).attr('realValue') === 'true';
            }

            $.ajax({
                method: router.method,
                url: router.url,
                contentType: 'application/json',
                data: JSON.stringify(courseData)
            }).done((data, textStatus, jqXHR) => {
                console.log(`Course #${courseId} successfully updated.`)
            }).fail((jqXHR, textStatus, errorThrown) => {
                console.log(jqXHR, textStatus, errorThrown);
                alert(`An error occurred when updating the course: ${JSON.stringify(jqXHR.responseJSON)}.`);
            });
        }
    });

    editableGridCourses.load({ metadata: [
            { name: "name", datatype: "string", editable: true },
            { name: "description", datatype: "string", editable: true },
            { name: "hasApero", datatype: "html", editable: false }
        ]});

    editableGridCourses.attachToHTMLTable('table-courses');
    editableGridCourses.renderGrid();
});

function deleteStudent(rowId, studentId, studentFirstName) {
    if (confirm(`Are you sure you want to delete the poor ${studentFirstName}?`)) {
        const router = jsRoutes.controllers.StudentsController.deleteStudent(studentId);

        // Make a DELETE request to the Student controller.
        $.ajax({
            method: router.method,
            url: router.url
        }).done((data, textStatus, jqXHR) => {
            console.log(`Student #${studentId} successfully deleted.`)
            $(`#row-student-${rowId}`).remove();
        }).fail((jqXHR, textStatus, errorThrown) => {
            console.log(jqXHR, textStatus, errorThrown);
            alert('An error occurred when deleting the student, please retry.');
        });
    }
}

function deleteCourse(rowId, courseId, courseName) {
    if (courseName === 'SCALA') {
        alert('Sorry, you cannot delete the SCALA course, since it is immuable in time. Huehuehue.');
    } else if (confirm(`Are you sure you want to delete ${courseName}, its poor assistant and its poor professor?`)) {
        const router = jsRoutes.controllers.CoursesController.deleteCourse(courseId);

        console.log(router);

        $.ajax({
            method: router.method,
            url: router.url
        }).done((data, textStatus, jqXHR) => {
            console.log(`Course #${courseId} successfully deleted.`)
            $(`#row-course-${rowId}`).remove();
        }).fail((jqXHR, textStatus, errorThrown) => {
            console.log(jqXHR, textStatus, errorThrown);
            alert('An error occurred when deleting the course, please retry.');
        });
    }
}