package com.github.strdn.rundeck.plugin.jdbcexecutor;

import com.dtolabs.rundeck.core.common.Framework;
import com.dtolabs.rundeck.core.common.INodeEntry;
import com.dtolabs.rundeck.core.dispatcher.DataContextUtils;
import com.dtolabs.rundeck.core.execution.ExecutionContext;
import com.dtolabs.rundeck.core.plugins.PluginException;
import com.dtolabs.rundeck.core.plugins.configuration.ConfigurationException;
import com.dtolabs.rundeck.core.storage.ResourceMeta;
import groovy.sql.Sql;
import org.apache.commons.lang.StringUtils;
import org.rundeck.storage.api.Path;
import org.rundeck.storage.api.PathUtil;
import org.rundeck.storage.api.StorageException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;


class SqlConnectionBuilder {
    private static final String PROJ_PROP_PREFIX = "project.";
    private static final String FWK_PROP_PREFIX = "framework.";
    private static final String JDBC_PASSWORD_STORAGE_PATH = "jdbc-password-storage-path";
    private static final String JDBC_PASSWORD_OPTION = "jdbc-password";
    private static final String JDBC_USERNAME_OPTION = "jdbc-username";
    private static final String DATABASE_TYPE_OPTION = "db-type";
    private static final String JDBC_CONNECTION_TIMEOUT_PROPERTY = "jdbc-connection-timeout";
    private static final int DEFAULT_JDBC_CONNECTION_TIMEOUT = 30;

    private ExecutionContext context;
    private INodeEntry node;
    private Framework framework;
    private String frameworkProject;

    static final String JDBC_CONNECTION_STRING_OPTION = "jdbc-connect";

    SqlConnectionBuilder (final ExecutionContext context, final INodeEntry node, final Framework framework) {
        this.context = context;
        this.node = node;
        this.framework = framework;
        this.frameworkProject = context.getFrameworkProject();
    }

    private ExecutionContext getContext() {
        return context;
    }

    private INodeEntry getNode() {
        return node;
    }

    private Framework getFramework() {
        return framework;
    }

    private String getFrameworkProject() {
        return frameworkProject;
    }

    private String getPassword() throws ConfigurationException {
        String storagePath = resolveProperty(JDBC_PASSWORD_STORAGE_PATH, null, getNode(), getFrameworkProject(), getFramework());
        if (storagePath != null) {
            //look up storage value
            if (storagePath.contains("${")) {
                storagePath = DataContextUtils.replaceDataReferences(
                        storagePath,
                        context.getDataContext()
                );
            }
            Path path = PathUtil.asPath(storagePath);
            try {
                ResourceMeta contents = context.getStorageTree().getResource(path).getContents();
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                contents.writeContent(byteArrayOutputStream);
                return new String(byteArrayOutputStream.toByteArray());
            } catch (StorageException | IOException e) {
                throw new ConfigurationException("Failed to read the jdbc password for " +
                        "storage path: " + storagePath + ": " + e.getMessage());
            }
        }
        //else look up option value
        return resolveProperty(JDBC_PASSWORD_OPTION, null, getNode(),
                getFrameworkProject(), getFramework());
    }

    public String getUsername() {
        final String user;
        if (StringUtils.isNotBlank(
                resolveProperty(JDBC_USERNAME_OPTION, null, getNode(),
                        getFrameworkProject(), getFramework()))) {
            user = resolveProperty(JDBC_USERNAME_OPTION, null, getNode(),
                    getFrameworkProject(), getFramework());
        } else {
            user = getNode().getUsername();
        }
        if (user != null && user.contains("${")) {
            return DataContextUtils.replaceDataReferences(user, getContext().getDataContext());
        }
        return user;
    }

    public Sql build() throws ConfigurationException, PluginException {
        final DBTypes databaseType;
        try {
            databaseType = DBTypes.valueOf(node.getAttributes().get(DATABASE_TYPE_OPTION));
        } catch(IllegalArgumentException e){
            throw new ConfigurationException("Unknown database type:" + node.getAttributes().get(DATABASE_TYPE_OPTION));
        }
        final String jdbcDriver = databaseType.getDriverName();

        try {
            return Sql.newInstance(node.getAttributes().get(JDBC_CONNECTION_STRING_OPTION), getUsername(), getPassword(), jdbcDriver);
        } catch (ClassNotFoundException cnfe) {
            throw new ConfigurationException("jdbc driver class not found" + cnfe.getMessage());
        } catch (SQLException sqle) {
            throw new PluginException("Failed to prepare connection: " + sqle.getMessage(), sqle);
        }
    }

    // properties resolvers
    private static String resolveProperty(final String nodeAttribute, final String defaultValue, final INodeEntry node,
                                          final String frameworkProject, final Framework framework) {
        if (null != node.getAttributes().get(nodeAttribute)) {
            return node.getAttributes().get(nodeAttribute);
        } else if (
            framework.hasProjectProperty(PROJ_PROP_PREFIX + nodeAttribute, frameworkProject)
                    && !"".equals(framework.getProjectProperty(frameworkProject, PROJ_PROP_PREFIX + nodeAttribute))
            ) {
            return framework.getProjectProperty(frameworkProject, PROJ_PROP_PREFIX + nodeAttribute);
        } else if (framework.hasProperty(FWK_PROP_PREFIX + nodeAttribute)) {
            return framework.getProperty(FWK_PROP_PREFIX + nodeAttribute);
        } else {
            return defaultValue;
        }
    }

    private static int resolveIntProperty(final String nodeAttribute, final int defaultValue, final INodeEntry node,
                                          final String frameworkProject, final Framework framework)
            throws ConfigurationException {
        int value = defaultValue;
        final String property = resolveProperty(nodeAttribute, null, node, frameworkProject, framework);
        if (null != property) {
            try {
                value = Integer.parseInt(property);
            } catch (NumberFormatException e) {
                throw new ConfigurationException("Not a valid integer: " + nodeAttribute + ": " + property);
            }
        }
        return value;
    }
}