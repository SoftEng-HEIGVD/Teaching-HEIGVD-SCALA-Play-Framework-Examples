# Teaching-HEIGVD-SCALA-Play-Framework-Examples
Here you can find a basic examples of a Scala's Play Framework applications coupled with Slick.

How to setup the database:
1. Install MySQL on your machine.
2. Create a database "scala_sql_example".
3. Import the SQL script (contained in "/sql/script.sql") into the new database.

# Changes

## Foreign keys

In order to add foreign keys to table with one to many or many to many relationships, the following changes were made to the original project:

* Move the TableQuery values to the component traits instead of the DAO classes
* Add the foreign key method to the Table classes which require it

To showcase their usage, the following changes were made:

* Create an example query (`listInvitations` in `CoursesStudentsDAO`) which retrieves for each student the corresponding course aperos (to which he or she is invited)
* Display the query's result through the `welcome.scala.html` view component. This requires change to:
    * the `index.scala.html` view (to pass the parameters)
    * the `HomeController` (to call the query and pass the parameters) 

## Use lazy val

Best practices is to use `lazy val` to define `TableQueries`