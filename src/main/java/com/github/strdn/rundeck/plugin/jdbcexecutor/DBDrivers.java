package com.github.strdn.rundeck.plugin.jdbcexecutor;

class DBDrivers {
    private static final String ORACLE_DRIVER = "oracle.jdbc.driver.OracleDriver";
    private static final String MYSQL_DRIVER = "com.mysql.jdbc.Driver";
    private static final String POSTGRES_DRIVER = "org.postgresql.Driver";

    static String getJDBCDriver(DBTypes dbType) {
        switch (dbType) {
            case ORACLE:
                return DBDrivers.ORACLE_DRIVER;
            case MYSQL:
                return DBDrivers.MYSQL_DRIVER;
            case POSTGRES:
                return DBDrivers.POSTGRES_DRIVER;
            default:
                return null;
        }
    }
}
