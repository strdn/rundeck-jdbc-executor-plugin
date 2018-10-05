# Rundeck JDBC execution plugin

This is a plugin for [Rundeck](http://rundeck.org) > 2.8.2 that provide the ability to add a nodestep with Groovy SQL scripts that will be executed with using jdbc connection on the Database.

## Build
    ./gradle clean build

## Install

Copy the `rundeck-jdbc-executor-plugin-<version>.jar` file to the `libext/` directory inside your Rundeck installation.
Also appropriate `jdbc driver` and `groovy-sql-<version>.jar` (tested with groovy-sql-2.4.11.jar) must be added to the `bootstrap/` directory. 

## Configuration

To configure connect to you database add following parameters to node description in Rundeck Project resourses.xml:

One of:
  * `db-type` : Database type. ORACLE and MYSQL was tested. See `com.github.strdn.rundeck.plugin.jdbcexecutor.DBTypes` for full list of supported databases
  * `driver-class-name`: The fully-qualified name of the driver class. If provided, `db-type` is ignored.

`jdbc-connect` : jdbc connection string
 
`jdbc-username` : database account name for connections

`jdbc-password` : password

`node-executor="jdbc-command"` (optional) for executing Groovy SQL scripts using Rundeck 'Command' tab. 
jdbc-command node executor also can be set in the project.properties file for using jdbc-command executor for all project nodes: `service.NodeExecutor.default.provider=jdbc-command`

e.g.:
```xml
<node name="remotehost" node-executor="jdbc-command" db-type="oracle" jdbc-connect="jdbc:oracle:thin:@remotehost:1521:ee122" jdbc-username="system" jdbc-password="system" description="" tags="" hostname="remotehost" osArch="amd64" osFamily="unix" osName="Linux" osVersion="" username="root"/>
```

## Usage
1. add a "Groovy SQL script executor" step to Rundeck job.
2. specify "script source" in the node step:
  - inline (default)
  - file
  
  with 'inline' option add script body to "Groovy SQL inline script" area
  
  with 'file' option specify path to the script file in "Groovy SQL script path" area
  
3. "Script args" is optional

simple script example (with Oracle Database):
```groovy
System.out.println("args =" + args.toString())
def query = "select username, account_status from dba_users where username = " + "'" + args[0] +"'";
sql.eachRow(query) { tp -> println(tp)}
query = "select operation, to_char(start_time, 'DD-MM-YYYY HH24-MI-SS') st, STATUS from DBA_OPTSTAT_OPERATIONS order by start_time desc"; 
sql.eachRow(query) { tp -> println([tp.operation, tp.st, tp.status])} 
```

 
 
