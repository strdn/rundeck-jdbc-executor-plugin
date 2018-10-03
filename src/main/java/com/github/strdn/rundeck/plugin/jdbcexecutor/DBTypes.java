package com.github.strdn.rundeck.plugin.jdbcexecutor;

public enum DBTypes {
    ORACLE("oracle.jdbc.driver.OracleDriver"),
    MYSQL("com.mysql.jdbc.Driver"),
    MSSQL("com.microsoft.sqlserver.jdbc.SQLServerDriver"),
    POSTGRES("org.postgresql.Driver");

    private final String driverName;

    DBTypes(String driverName) {
        this.driverName = driverName;
    }

    public String getDriverName() {
        return driverName;
    }
}
