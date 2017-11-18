package com.github.strdn.rundeck.plugin.jdbcexecutor;

import com.dtolabs.rundeck.core.common.Framework;
import com.dtolabs.rundeck.core.common.INodeEntry;
import com.dtolabs.rundeck.core.execution.ExecutionContext;
import com.dtolabs.rundeck.core.execution.service.NodeExecutor;
import com.dtolabs.rundeck.core.execution.service.NodeExecutorResult;
import com.dtolabs.rundeck.core.execution.service.NodeExecutorResultImpl;
import com.dtolabs.rundeck.core.execution.workflow.steps.StepFailureReason;
import com.dtolabs.rundeck.core.plugins.Plugin;
import com.dtolabs.rundeck.core.plugins.PluginException;
import com.dtolabs.rundeck.core.plugins.configuration.ConfigurationException;
import com.dtolabs.rundeck.core.plugins.configuration.Describable;
import com.dtolabs.rundeck.core.plugins.configuration.Description;
import com.dtolabs.rundeck.plugins.ServiceNameConstants;
import com.dtolabs.rundeck.plugins.descriptions.PluginDescription;
import com.dtolabs.rundeck.plugins.util.DescriptionBuilder;
import javax.script.ScriptException;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import static com.github.strdn.rundeck.plugin.jdbcexecutor.GroovySQLStatementExecutor.executeStatement;

/**
 * GroovySQLCommandPlugin {@link NodeExecutor} plugin implementation.
 *
 */
@Plugin(name = GroovySQLCommandPlugin.PROVIDER_NAME, service = ServiceNameConstants.NodeExecutor)
@PluginDescription(title = "JDBC Executor",description = "JDBC Executor")
public class GroovySQLCommandPlugin implements NodeExecutor, Describable {
    static final String PROVIDER_NAME = "jdbc-command";
    private static final String SERVICE_PROVIDER_TYPE = "jdbc-command";
    public static final Logger logger = Logger.getLogger(GroovySQLCommandPlugin.class.getName());

    private Framework framework;

    static final Description DESC ;
    static {
        DescriptionBuilder builder = DescriptionBuilder.builder();
        builder.name(SERVICE_PROVIDER_TYPE)
                .title("Groovy SQL command executor")
                .description("Execute a inline groovy sql on the DB using jdbc")
        ;
        DESC = builder.build();
    }

    public Description getDescription() {
        return DESC;
    }

    public GroovySQLCommandPlugin(final Framework framework) {
        this.framework = framework;
    }

    public NodeExecutorResult executeCommand(final ExecutionContext context, final String[] command, final INodeEntry node) {
        if (null == node.getAttributes().get(SqlConnectionBuilder.JDBC_CONNECTION_STRING_OPTION)
                || StringUtils.isBlank(node.getAttributes().get(SqlConnectionBuilder.JDBC_CONNECTION_STRING_OPTION))) {
            return NodeExecutorResultImpl.createFailure(
                    StepFailureReason.ConfigurationFailure,
                    "jdbc connection string must be set for '" + node.getNodename() + "'", node);
        }

        try {
            executeStatement(new SqlConnectionBuilder(context, node, framework).build(), buildStatement(command), null);
        } catch (ScriptException | PluginException sepe) {
            return NodeExecutorResultImpl.createFailure(StepFailureReason.PluginFailed, sepe.getMessage(), node);
        } catch (ConfigurationException ce) {
            return NodeExecutorResultImpl.createFailure(StepFailureReason.ConfigurationFailure, ce.getMessage(), node);
        }
        return NodeExecutorResultImpl.createSuccess(node);
    }

    private static String buildStatement(String[] args) {
        final StringBuilder stringBuilder = new StringBuilder();
        for (String s : args) {
            stringBuilder.append(s).append(" ");
        }
        return stringBuilder.toString();
    }
}
