package org.anarres.gradle.plugin.velocity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.internal.file.FileResolver;
import org.gradle.api.internal.plugins.DslObject;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;

/**
 * The velocity plugin.
 *
 * This creates a default configuration which preprocesses files
 * according to the VelocityPluginExtension.
 *
 * @author shevek
 */
public class VelocityPlugin implements Plugin<Project> {

    private final ObjectFactory objectFactory;

    @Inject
    public VelocityPlugin(ObjectFactory objectFactory) {
        this.objectFactory = objectFactory;
    }

    @Override
    public void apply(final Project project) {
        project.getPlugins().apply(JavaBasePlugin.class);

        final VelocityPluginExtension extension = project.getExtensions().create("velocity", VelocityPluginExtension.class);

        project.getConvention().getPlugin(JavaPluginConvention.class).getSourceSets().all(
                new Action<SourceSet>() {
                    @Override
                    public void execute(SourceSet t) {
                        apply(project, t, extension);
                    }
                });
    }

    private void apply(@Nonnull final Project project, @Nonnull SourceSet sourceSet, @Nonnull final VelocityPluginExtension extension) {
        final VelocitySourceVirtualDirectory velocitySourceSet = new VelocitySourceVirtualDirectory(sourceSet.getName(), objectFactory);
        new DslObject(sourceSet).getConvention().getPlugins().put("velocity", velocitySourceSet);
        final String srcDir = String.format("src/%s/velocity", sourceSet.getName());
        velocitySourceSet.getVelocity().srcDir(srcDir);
        sourceSet.getAllSource().source(velocitySourceSet.getVelocity());

        final String velocityTaskName = sourceSet.getTaskName("process", "Velocity");
        VelocityTask velocityTask = project.getTasks().create(velocityTaskName, VelocityTask.class, new Action<VelocityTask>() {
            @Override
            public void execute(VelocityTask task) {
                task.setDescription("Preprocesses velocity template files.");

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

                task.conventionMapping("contextValues", new Callable<Map<String, Object>>() {
                    @Override
                    public Map<String, Object> call() {
                        return extension.contextValues;
                    }
                });
            }
        });

        velocityTask.setSource(velocitySourceSet.getVelocity());

        final String outputDirectoryName = String.format("%s/generated-sources/antlr/%s",
                project.getBuildDir(), sourceSet.getName());
        File outputDir = new File(outputDirectoryName);
        velocityTask.setOutputDir(outputDir);
        sourceSet.getJava().srcDir(outputDir);

        project.getTasks().getByName(sourceSet.getCompileJavaTaskName()).dependsOn(velocityTask);
    }
}
