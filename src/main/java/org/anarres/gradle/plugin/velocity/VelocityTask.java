package org.anarres.gradle.plugin.velocity;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import org.apache.commons.lang.ArrayUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.log.SystemLogChute;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.gradle.api.GradleException;
import org.gradle.api.file.EmptyFileVisitor;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.file.FileVisitDetails;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.SourceTask;
import org.gradle.api.tasks.TaskAction;

/**
 * A bare velocity task.
 *
 * You may use this to do arbitrary velocity processing without
 * necessarily applying the plugin.
 *
 * @author shevek
 */
public class VelocityTask extends SourceTask {

    private File outputDir;

    private List<File> includeDirs;
    private Map<String, Object> contextValues;

    /**
     * Use {@link SourceTask#setSource(java.lang.Object)}.
     *
     * @param inputDir The input directory.
     * @deprecated Use {@link SourceTask#setSource(java.lang.Object)}.
     */
    @Deprecated // Use setSource() from SourceTask.
    public void setInputDir(@Nonnull File inputDir) {
        setSource(inputDir);
    }

    @OutputDirectory
    @Nonnull    // Not @Optional
    public File getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(@Nonnull File outputDir) {
        this.outputDir = outputDir;
    }

    /**
     * Use {@link SourceTask#include(java.lang.String...)}.
     *
     * @param inputDir The input filename filter.
     * @deprecated Use {@link SourceTask#include(java.lang.String...)}.
     */
    @Deprecated
    public void setIncludeFilter(@Nonnull String... includeFilter) {
        include(includeFilter);
    }

    /**
     * Use {@link SourceTask#include(java.lang.String...)}.
     *
     * @param inputDir The input filename filter.
     * @deprecated Use {@link SourceTask#include(java.lang.String...)}.
     */
    @Deprecated
    public void setFilter(@CheckForNull String filter) {
        if (filter == null)
            setIncludeFilter(ArrayUtils.EMPTY_STRING_ARRAY);
        else
            setIncludeFilter(new String[]{filter});
    }

    @Input
    @Optional
    @CheckForNull
    public List<File> getIncludeDirs() {
        return includeDirs;
    }

    public void setIncludeDirs(@Nonnull List<File> includeDirs) {
        this.includeDirs = includeDirs;
    }

    @InputFiles
    @Nonnull    // Not @Optional
    private FileCollection _getIncludeFiles() {
        FileCollection files = getProject().files();
        List<File> includeDirs = getIncludeDirs();
        if (includeDirs != null)
            for (File f : includeDirs)
                files = files.plus(getProject().fileTree(f));
        return files;
    }

    @Input
    @Optional
    @CheckForNull
    public Map<String, Object> getContextValues() {
        return contextValues;
    }

    public void setContextValues(@Nonnull Map<String, Object> contextValues) {
        this.contextValues = contextValues;
    }

    private void setProperty(VelocityEngine engine, String name, Object value) {
        getLogger().info("VelocityEngine property: " + name + " = " + value);
        engine.setProperty(name, value);
    }

    private void toIncludeBufDir(@Nonnull StringBuilder includeBuf, @Nonnull File dir) {
        getLogger().info("Including dir " + dir);
        if (includeBuf.length() > 0)
            includeBuf.append(", ");
        includeBuf.append(dir.getAbsolutePath());
    }

    private void toIncludeBufDirs(@Nonnull StringBuilder includeBuf, @CheckForNull Iterable<File> dirs) {
        if (dirs != null)
            for (File dir : dirs)
                toIncludeBufDir(includeBuf, dir);
    }

    private void toIncludeBufUnknown(@Nonnull StringBuilder includeBuf, @Nonnull Iterable<Object> sources) {
        for (Object source : sources) {
            getLogger().info("Attepmting to include " + source.getClass() + ":" + source);
            if (source instanceof File)
                toIncludeBufDir(includeBuf, (File) source);
            else if (source instanceof SourceDirectorySet)
                toIncludeBufDirs(includeBuf, ((SourceDirectorySet) source).getSrcDirs());
            // I wish we could introspect CompositeFileTree.
        }
    }

    @TaskAction
    public void runVelocity() throws Exception {
        final FileTree inputFiles = getSource();
        final File outputDir = getOutputDir();

        DefaultGroovyMethods.deleteDir(outputDir);
        outputDir.mkdirs();

        final VelocityEngine engine = new VelocityEngine();
        setProperty(engine, VelocityEngine.RUNTIME_LOG_LOGSYSTEM_CLASS, SystemLogChute.class.getName());
        setProperty(engine, VelocityEngine.RESOURCE_LOADER, "file");
        setProperty(engine, VelocityEngine.FILE_RESOURCE_LOADER_CACHE, "true");
        // FILE_RESOURCE_LOADER_PATH actually takes a comma separated list. 
        StringBuilder includeBuf = new StringBuilder();
        toIncludeBufUnknown(includeBuf, source);
        toIncludeBufDirs(includeBuf, getIncludeDirs());
        setProperty(engine, VelocityEngine.FILE_RESOURCE_LOADER_PATH, includeBuf.toString());

        inputFiles.visit(new EmptyFileVisitor() {
            @Override
            public void visitFile(FileVisitDetails fvd) {
                try {
                    File outputFile = fvd.getRelativePath().getFile(outputDir);
                    if (getLogger().isDebugEnabled())
                        getLogger().debug("Preprocessing " + fvd.getFile() + " -> " + outputFile);
                    VelocityContext context = new VelocityContext();
                    Map<String, Object> contextValues = getContextValues();
                    if (contextValues != null)
                        for (Map.Entry<String, Object> e : contextValues.entrySet())
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
