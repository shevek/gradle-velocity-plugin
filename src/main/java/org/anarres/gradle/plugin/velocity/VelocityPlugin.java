package org.anarres.gradle.plugin.velocity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;

/**
 * The velocity plugin.
 *
 * This creates a default configuration which preprocesses files
 * according to the VelocityPluginExtension.
 *
 * @author shevek
 */
public class VelocityPlugin implements Plugin<Project> {

    @Override
    public void apply(final Project project) {
        final VelocityPluginExtension extension = project.getExtensions().create("velocity", VelocityPluginExtension.class);
        Task velocityVppTask = project.getTasks().create("velocityVpp", VelocityTask.class, new Action<VelocityTask>() {
            @Override
            public void execute(VelocityTask task) {
                task.setDescription("Preprocesses velocity template files.");

                task.conventionMapping("inputDir", new Callable<File>() {
                    @Override
                    public File call() {
                        return project.file(extension.inputDir);
                    }
                });

                task.conventionMapping("includeDirs", new Callable<List<File>>() {
                    @Override
                    public List<File> call() {
                        List<Object> includeDirs = extension.includeDirs;
                        if (includeDirs == null)
                            return null;
                        List<File> out = new ArrayList<File>();
                        for (Object includeDir : includeDirs)
                            out.add(project.file(includeDir));
                        return out;
                    }
                });

                task.conventionMapping("outputDir", new Callable<File>() {
                    @Override
                    public File call() {
                        return project.file(extension.outputDir);
                    }
                });

                task.conventionMapping("contextValues", new Callable<Map<String, Object>>() {
                    @Override
                    public Map<String, Object> call() {
                        return extension.contextValues;
                    }
                });

                task.conventionMapping("filter", new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        return extension.filter;
                    }
                });
            }
        });

        project.getTasks().getByName("compileJava").dependsOn(velocityVppTask);
        SourceSetContainer sourceSets = project.getConvention().getPlugin(JavaPluginConvention.class).getSourceSets();
        final SourceSet mainSourceSet = sourceSets.getByName("main");
        mainSourceSet.getJava().srcDir(extension.outputDir);
    }

}
