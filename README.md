The following are steps to load the Lansing CATA data into a MySQL database, serve that data via REST and display the data via ReactJS.

# Transit Parser

git clone `https://github.com/gosenkyl/transit-parser.git`

This repo is used to generate a large SQL file that we will redirect into our MySQL instance.

Open `transit-parser/src/main/resources/` and unzip the google_transit.zip file into a directory called google_transit. I will try to keep this zip relevant until an automated process is in place. At that point, we will be able to just run the same gradle command that the nightly job will run. It will grab the latest zip file from the FTP, do a checksum for differences, if there are differences it will unzip it, run the generator, drop the database and recreate it.

You should be able to open `transit-parser/src/main/java/com/gosenk/transit/parser/Application.java`, right click and run. Once complete, a data.sql file will be created.

# MySQL
Install MySQL locally. For this demonstration, I'm using XAMPP. You can use something like a Docker container as well very easily.

First, we need to create the database.

Open your favorite MySQL editor, such as MySQL Workbench and create a connection to localhost:3306 with username: root, password: root. Copy - paste the contents of `transit-parser/src/main/resources/database.sql` into the editor and run all.

Next, navigate to the MySQL bin folder `C:\xampp\mysql\bin` in a terminal and have the full path to the data.sql file we generated previously, handy. Execute `mysql -u root -p < [path to data.sql]`. If your local connection uses a password (i.e. "root"), type that in, if not just hit enter when prompted. This process will take a little while.

> Note: Before running this a second time, drop your transit database or this will fail. I may include the drop as part of this script but for now, want the user to understand what they're doing.

To verify this worked, go to your MySQL editor and execute `select * from transit.agency;` and ensure there are results.

# Spring Boot Backend

git clone `https://github.com/gosenkyl/TransitAPI.git`

This repo serves REST.

The only file we should need to touch here is `TransitAPI/src/main/resources/application.properties`. If your MySQL is running on a different port than listed in `spring.datasource.url=jdbc:mysql://127.0.0.1:3306/transit`, or we're not running the MySQL instance locally and want to connect to a remote server, change the IP/port here.

Navigate to `TransitAPI/src/main/java/com/gosenk/transit/api/Application.java`, right click in the file and select Run. Your services should be running. To test, visit `localhost:8081/api/agencies` in a browser and ensure JSON is returned.

# React

git clone `https://github.com/gosenkyl/transit-react.git`

This repo is my first bastardized attempt at learning React (without Redux!).

Execute `npm install`. Once that is complete, execute `npm run start` and you should be up and running!

Happy coding!
