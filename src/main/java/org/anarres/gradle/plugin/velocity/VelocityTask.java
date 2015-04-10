package org.anarres.gradle.plugin.velocity;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.log.SystemLogChute;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.gradle.api.GradleException;
import org.gradle.api.file.ConfigurableFileTree;
import org.gradle.api.file.EmptyFileVisitor;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileVisitDetails;
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

/**
 * A bare velocity task.
 *
 * You may use this to do arbitrary velocity processing without
 * necessarily applying the plugin.
 *
 * @author shevek
 */
public class VelocityTask extends ConventionTask {

    private File inputDir;
    private File outputDir;

    private String filter = "**/*.java";
    private List<File> includeDirs = new ArrayList<File>();
    private Map<String, Object> contextValues = new HashMap<String, Object>();

    @InputDirectory
    @Nonnull
    public File getInputDir() {
        return inputDir;
    }

    public void setInputDir(@Nonnull File inputDir) {
        this.inputDir = inputDir;
    }

    @OutputDirectory
    @Nonnull
    public File getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(@Nonnull File outputDir) {
        this.outputDir = outputDir;
    }

    @Input
    @Nonnull
    public String getFilter() {
        return filter;
    }

    public void setFilter(@CheckForNull String filter) {
        this.filter = filter;
    }

    @Input
    @Nonnull
    public List<File> getIncludeDirs() {
        return includeDirs;
    }

    public void setIncludeDirs(@Nonnull List<File> includeDirs) {
        this.includeDirs = includeDirs;
    }

    @InputFiles
    @Nonnull
    private FileCollection _getIncludeFiles() {
        FileCollection files = getProject().files();
        for (File f : getIncludeDirs())
            files = files.plus(getProject().fileTree(f));
        return files;
    }

    @Input
    @Nonnull
    public Map<String, Object> getContextValues() {
        return contextValues;
    }

    public void setContextValues(@Nonnull Map<String, Object> contextValues) {
        this.contextValues = contextValues;
    }

    private void setProperty(VelocityEngine engine, String name, Object value) {
        if (getLogger().isDebugEnabled())
            getLogger().debug("VelocityEngine property: " + name + " = " + value);
        engine.setProperty(name, value);
    }

    @TaskAction
    public void runVelocity() throws Exception {
        final File outputDir = getOutputDir();

        DefaultGroovyMethods.deleteDir(outputDir);
        outputDir.mkdirs();

        final VelocityEngine engine = new VelocityEngine();
        setProperty(engine, VelocityEngine.RUNTIME_LOG_LOGSYSTEM_CLASS, SystemLogChute.class.getName());
        setProperty(engine, VelocityEngine.RESOURCE_LOADER, "file");
        setProperty(engine, VelocityEngine.FILE_RESOURCE_LOADER_CACHE, "true");
        // FILE_RESOURCE_LOADER_PATH actually takes a comma separated list. 
        StringBuilder includeBuf = new StringBuilder();
        includeBuf.append(getInputDir().getAbsolutePath());
        for (File includeDir : getIncludeDirs()) {
            includeBuf.append(", ");
            includeBuf.append(includeDir.getAbsolutePath());
        }
        setProperty(engine, VelocityEngine.FILE_RESOURCE_LOADER_PATH, includeBuf.toString());

        ConfigurableFileTree inputFiles = getProject().fileTree(getInputDir());
        inputFiles.include(getFilter());
        inputFiles.visit(new EmptyFileVisitor() {
            @Override
            public void visitFile(FileVisitDetails fvd) {
                try {
                    File outputFile = fvd.getRelativePath().getFile(outputDir);
                    if (getLogger().isDebugEnabled())
                        getLogger().debug("Preprocessing " + fvd.getFile() + " -> " + outputFile);
                    VelocityContext context = new VelocityContext();
                    for (Map.Entry<String, Object> e : getContextValues().entrySet())
                        context.put(e.getKey(), e.getValue());
                    context.put("project", getProject());
                    context.put("package", DefaultGroovyMethods.join(fvd.getRelativePath().getParent().getSegments(), "."));
                    context.put("class", fvd.getRelativePath().getLastName().replaceFirst("\\.java$", ""));
                    FileReader reader = new FileReader(fvd.getFile());
                    try {
                        outputFile.getParentFile().mkdirs();
                        FileWriter writer = new FileWriter(outputFile);
                        try {
                            engine.evaluate(context, writer, fvd.getRelativePath().toString(), reader);
                        } finally {
                            writer.close();
                        }
                    } finally {
                        reader.close();
                    }
                } catch (IOException e) {
                    throw new GradleException("Failed to process " + fvd, e);
                }
            }
        });
    }
}
