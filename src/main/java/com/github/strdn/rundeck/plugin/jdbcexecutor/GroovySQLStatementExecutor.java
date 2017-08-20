package com.github.strdn.rundeck.plugin.jdbcexecutor;

import groovy.sql.Sql;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class GroovySQLStatementExecutor {
    static void executeStatement(final Sql sql, final String groovyStatementScript, final String args) throws ScriptException {
        ScriptEngineManager factory = new ScriptEngineManager();
        ScriptEngine engine = factory.getEngineByName("groovy");
        final Map<String, Object> engineBindings = new HashMap<String, Object>();
        engineBindings.put("sql", sql);
        if (args != null) engineBindings.put("args", args.split(" "));

        try {
            engine.eval(groovyStatementScript, new SimpleBindings(engineBindings));
        } finally {
            sql.close();
        }
    }

    static void executeStatement(final Sql sql, final Path groovyStatementScriptPath, final String args) throws ScriptException, IOException {
        final String fileContent = new String(Files.readAllBytes(groovyStatementScriptPath));
        executeStatement(sql, fileContent, args);
    }
}
