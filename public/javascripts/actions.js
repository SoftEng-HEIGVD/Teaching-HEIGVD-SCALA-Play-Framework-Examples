$(function() {
    // Build and load the metadata of the students grid in order to make it editable.
    editableGridStudents = new EditableGrid("GridStudents");

    editableGridStudents.load({ metadata: [
            { name: "firstName", datatype: "string", editable: true },
            { name: "lastName", datatype: "string", editable: true },
            { name: "age", datatype: "integer", editable: true }
        ]});

    // Then attach the object to the HTML table and render it.
    editableGridStudents.attachToHTMLTable('table-students');
    editableGridStudents.renderGrid();

    // Same for the courses
    editableGridCourses = new EditableGrid("GridCourses");

    editableGridCourses.load({ metadata: [
            { name: "name", datatype: "string", editable: true },
            { name: "description", datatype: "string", editable: true },
            { name: "hasApero", datatype: "boolean", editable: true }
        ]});

    editableGridCourses.attachToHTMLTable('table-courses');
    editableGridCourses.renderGrid();
});

function abcdef() {
    console.log('1');
}