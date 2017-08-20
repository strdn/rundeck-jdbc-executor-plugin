package com.github.strdn.rundeck.plugin.jdbcexecutor;

import com.dtolabs.rundeck.core.common.INodeEntry;
import com.dtolabs.rundeck.core.execution.workflow.steps.StepFailureReason;
import com.dtolabs.rundeck.core.execution.workflow.steps.node.NodeStepException;
import com.dtolabs.rundeck.core.plugins.Plugin;
import com.dtolabs.rundeck.core.plugins.PluginException;
import com.dtolabs.rundeck.core.plugins.configuration.ConfigurationException;
import com.dtolabs.rundeck.core.plugins.configuration.StringRenderingConstants;
import com.dtolabs.rundeck.plugins.ServiceNameConstants;
import com.dtolabs.rundeck.plugins.descriptions.PluginDescription;
import com.dtolabs.rundeck.plugins.descriptions.PluginProperty;
import com.dtolabs.rundeck.plugins.descriptions.RenderingOption;
import com.dtolabs.rundeck.plugins.descriptions.SelectValues;
import com.dtolabs.rundeck.plugins.descriptions.TextArea;
import com.dtolabs.rundeck.plugins.step.NodeStepPlugin;
import com.dtolabs.rundeck.plugins.step.PluginStepContext;
import com.dtolabs.rundeck.plugins.util.DescriptionBuilder;
import org.apache.commons.lang.StringUtils;

import javax.script.ScriptException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@Plugin(name = GroovySQLScriptNodeStepPlugin.SERVICE_PROVIDER_NAME, service = ServiceNameConstants.WorkflowNodeStep)
@PluginDescription(title = "Groovy SQL Script Executor",description = "Groovy SQL Script Executor")
public class GroovySQLScriptNodeStepPlugin implements NodeStepPlugin, DescriptionBuilder.Collaborator {

    public static final String FILE_SOURCE="File";
    public static final String INLINE_SOURCE="Inline";
    @PluginProperty(title="Script source",
                    description = "Execute stored file or inline script",
                    required = true,
                    defaultValue = FILE_SOURCE)
    @SelectValues(values = {FILE_SOURCE, INLINE_SOURCE})
    String scriptSource;

    @PluginProperty(title = "Groovy SQL script path", description = "file path", required = false)
    private String scriptPath;

    @PluginProperty(title = "Groovy SQL inline script", description = "Groovy SQL inline script")
    @TextArea
    @RenderingOption(key = StringRenderingConstants.DISPLAY_TYPE_KEY, value = "CODE")
    private String scriptBody;

    @PluginProperty(title = "Script args", description = "Groovy SQL script arguments")
    private String scriptArgs;

    public static final String SERVICE_PROVIDER_NAME = "com.github.strdn.rundeck.plugin.jdbcexecutor.GroovySQLScriptNodeStepPlugin";

    public void buildWith(DescriptionBuilder builder) {
        builder
            .name(SERVICE_PROVIDER_NAME)
            .title("Groovy SQL script executor")
            .description("Execute locally stored groovy SQL script using jdbc")
            .build();
    }

    @Override
    public void executeNodeStep(PluginStepContext context, Map<String, Object> configuration, INodeEntry entry)
                throws NodeStepException {

        //final Path scriptFilePath = Paths.get(scriptPath).toAbsolutePath();
        Path scriptFilePath = null;
        if (scriptSource.equals(FILE_SOURCE)) {
            if (StringUtils.isBlank(scriptPath))
                throw new NodeStepException("File execution selected but path is not set", StepFailureReason.ConfigurationFailure, entry.getNodename());

            scriptFilePath = Paths.get(scriptPath).toAbsolutePath();
            if (!(scriptFilePath.toFile().isFile() && scriptFilePath.toFile().canRead())) {
                throw new NodeStepException("Cannot read script file: " + scriptPath, StepFailureReason.IOFailure, entry.getNodename());
            }
        }

        if (scriptSource.equals(INLINE_SOURCE) && scriptBody.trim().isEmpty()) {
            throw new NodeStepException("Inline script execution selected but is empty", StepFailureReason.ConfigurationFailure, entry.getNodename());
        }

        try {
            if (scriptSource.equals(INLINE_SOURCE)) {
                new GroovySQLStatementExecutor().executeStatement(
                        new SqlConnectionBuilder(context.getExecutionContext(), entry, context.getFramework()).build(),
                        scriptBody, scriptArgs
                );
            } else {
                new GroovySQLStatementExecutor().executeStatement(
                        new SqlConnectionBuilder(context.getExecutionContext(), entry, context.getFramework()).build(),
                        scriptFilePath, scriptArgs
                );
            }
        } catch (ConfigurationException sqlcbce) {
            throw new NodeStepException(sqlcbce, StepFailureReason.ConfigurationFailure, entry.getNodename());
        } catch (ScriptException | PluginException sepe) {
            throw new NodeStepException(sepe, StepFailureReason.PluginFailed, entry.getNodename());
        } catch (IOException esioe) {
            throw new NodeStepException(esioe, StepFailureReason.IOFailure, entry.getNodename());
        }
    }
}
