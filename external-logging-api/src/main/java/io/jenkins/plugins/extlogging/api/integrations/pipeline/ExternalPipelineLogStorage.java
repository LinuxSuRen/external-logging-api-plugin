/*
 * The MIT License
 *
 * Copyright 2016 CloudBees, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package io.jenkins.plugins.extlogging.api.integrations.pipeline;


import hudson.console.AnnotatedLargeText;
import hudson.model.BuildListener;
import hudson.model.TaskListener;
import io.jenkins.plugins.extlogging.api.ExternalLoggingEventWriter;
import io.jenkins.plugins.extlogging.api.ExternalLoggingMethod;
import io.jenkins.plugins.extlogging.api.impl.ExternalLoggingOutputStream;
import io.jenkins.plugins.extlogging.api.SensitiveStringsProvider;
import jenkins.model.logging.LoggingMethod;
import jenkins.model.logging.LoggingMethodLocator;
import org.jenkinsci.plugins.workflow.flow.FlowExecutionOwner;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.log.LogStorage;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;


/**
 * Integrates remote logging with Pipeline builds using an experimental API.
 */
public class ExternalPipelineLogStorage implements LogStorage {

    private final ExternalLoggingMethod lm;
    private final WorkflowRun run;

    ExternalPipelineLogStorage(@Nonnull WorkflowRun run, @Nonnull ExternalLoggingMethod lm) {
        this.run = run;
        this.lm = lm;
    }

    @Nonnull
    @Override
    public BuildListener overallListener() throws IOException, InterruptedException {
        final LoggingMethod loggingMethod = LoggingMethodLocator.locate(run);
        if (loggingMethod instanceof ExternalLoggingMethod) {
            ExternalLoggingMethod lm = (ExternalLoggingMethod)loggingMethod;
            return new PipelineListener(run, lm);
        }

        // Else - not configured
        return null;
    }

    @Nonnull
    @Override
    public TaskListener nodeListener(@Nonnull FlowNode flowNode) throws IOException, InterruptedException {
        final LoggingMethod loggingMethod = LoggingMethodLocator.locate(run);
        if (loggingMethod instanceof ExternalLoggingMethod) {
            ExternalLoggingMethod lm = (ExternalLoggingMethod)loggingMethod;
            return new PipelineListener(run, lm);
        }

        // Else - not configured
        return null;
    }

    @Nonnull
    @Override
    public AnnotatedLargeText<FlowExecutionOwner.Executable> overallLog(@Nonnull FlowExecutionOwner.Executable executable, boolean b) {
        return null;
    }

    @Nonnull
    @Override
    public AnnotatedLargeText<FlowNode> stepLog(@Nonnull FlowNode flowNode, boolean b) {
        return null;
    }


    private static class PipelineListener implements BuildListener {

        private static final long serialVersionUID = 1;

        private final Collection<String> sensitiveStrings;
        private final ExternalLoggingEventWriter writer;
        private transient PrintStream logger;

        PipelineListener(WorkflowRun run, ExternalLoggingMethod method) {
            this.writer = method.createWriter(run);
            this.sensitiveStrings = SensitiveStringsProvider.getAllSensitiveStrings(run);


        }

        PipelineListener(WorkflowRun run, FlowNode node, ExternalLoggingMethod method) {
            this(run, method);
            writer.addMetadataEntry("flowNodeId", node.getId());
        }

        @Override public PrintStream getLogger() {
            if (logger == null) {
                logger = new PrintStream(ExternalLoggingOutputStream.createOutputStream(writer, sensitiveStrings));
            }
            return logger;
        }

    }

}