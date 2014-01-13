package org.anarres.gradle.plugin.velocity;

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
                // TODO: This isn't lazy evaluation. :-(
                task.inputDir = project.file(extension.inputDir);
                task.outputDir = project.file(extension.outputDir);
                task.contextValues = extension.contextValues;
            }
        });

        project.getTasks().getByName("compileJava").dependsOn(velocityVppTask);
        SourceSetContainer sourceSets = project.getConvention().getPlugin(JavaPluginConvention.class).getSourceSets();
        final SourceSet mainSourceSet = sourceSets.getByName("main");
        mainSourceSet.getJava().srcDir(extension.outputDir);
    }

}
