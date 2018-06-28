package com.github.strdn.rundeck.plugin.jdbcexecutor;

public enum DBTypes {
    ORACLE, MYSQL, POSTGRES, UNKNOWN;

    private static final String oracleDatabase = "ORACLE";
    private static final String mysqlDatabase = "MYSQL";
    private static final String postgresDatabase = "POSTGRES";

    public static DBTypes getDBType(String databaseName) {
        if (oracleDatabase.equalsIgnoreCase(databaseName))
            return ORACLE;
        if (mysqlDatabase.equalsIgnoreCase(databaseName))
            return MYSQL;
        if (postgresDatabase.equalsIgnoreCase(databaseName))
            return POSTGRES;
        return UNKNOWN;
    }
}
