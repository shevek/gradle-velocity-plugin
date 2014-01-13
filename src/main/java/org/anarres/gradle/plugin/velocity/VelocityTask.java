package org.anarres.gradle.plugin.velocity;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.log.SystemLogChute;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.Task;
import org.gradle.api.file.ConfigurableFileTree;
import org.gradle.api.file.FileVisitDetails;
import org.gradle.api.file.FileVisitor;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;

/**
 *
 * @author shevek
 */
public class VelocityTask extends DefaultTask {

    @InputDirectory
    public File inputDir;
    @OutputDirectory
    public File outputDir;

    @Input
    public String filter = "**/*.java";
    @Optional
    @InputDirectory
    public File includeDir;
    @Input
    public Map<String, Object> contextValues = new HashMap<String, Object>();

    public VelocityTask() {
        doLast(new Action<Task>() {

            @Override
            public void execute(Task task) {
                DefaultGroovyMethods.deleteDir(outputDir);
                outputDir.mkdirs();

                final VelocityEngine engine = new VelocityEngine();
                engine.setProperty(VelocityEngine.RUNTIME_LOG_LOGSYSTEM_CLASS, SystemLogChute.class.getName());
                engine.setProperty(VelocityEngine.RESOURCE_LOADER, "file");
                engine.setProperty(VelocityEngine.FILE_RESOURCE_LOADER_CACHE, "true");
                if (includeDir != null)
                    engine.setProperty(VelocityEngine.FILE_RESOURCE_LOADER_PATH, includeDir.getAbsolutePath());

                Map<String, Object> inputFilesSpec = new HashMap<String, Object>();
                inputFilesSpec.put("dir", inputDir);
                inputFilesSpec.put("include", filter);
                ConfigurableFileTree inputFiles = getProject().fileTree(inputFilesSpec);
                inputFiles.visit(new FileVisitor() {

                    @Override
                    public void visitDir(FileVisitDetails fvd) {
                    }

                    @Override
                    public void visitFile(FileVisitDetails fvd) {
                        try {
                            File outputFile = fvd.getRelativePath().getFile(outputDir);
                            VelocityContext context = new VelocityContext();
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
        });
    }
}
