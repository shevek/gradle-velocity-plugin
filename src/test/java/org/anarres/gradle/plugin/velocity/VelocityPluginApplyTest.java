package org.anarres.gradle.plugin.velocity;

import java.util.Collections;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author shevek
 */
public class VelocityPluginApplyTest {

    Project project;

    @Before
    public void setUp() {
        project = ProjectBuilder.builder().build();
    }

    @Test
    public void testApply() {
        project.apply(Collections.singletonMap("plugin", "java"));
        project.apply(Collections.singletonMap("plugin", "velocity"));
        assertTrue("Project is missing plugin", project.getPlugins().hasPlugin(VelocityPlugin.class));
        Task task = project.getTasks().findByName("velocityVpp");
        assertNotNull("Project is missing velocity task", task);
        assertTrue("Velocity task is the wrong type", task instanceof DefaultTask);
        assertTrue("Velocity task should be enabled", ((DefaultTask) task).isEnabled());
    }

    @Test
    public void setCustomFilter() {
        project.apply(Collections.singletonMap("plugin", "java"));
        project.apply(Collections.singletonMap("plugin", "velocity"));

        ExtensionContainer extensions = project.getExtensions();
        VelocityPluginExtension dsl = (VelocityPluginExtension) extensions.getByName("velocity");

        // Retrieve plugin task and verify default values
        VelocityTask task = (VelocityTask) project.getTasks().findByName("velocityVpp");
        String filter = task.getFilter();
        assertEquals(VelocityPluginExtension.DEFAULT_FILTER_REGEX, filter);

        // Modify the PluginExtensions filter
        String testValue = "myFilter";
        dsl.filter = testValue;

        // Verify that plugin task has the new value for filter
        task = (VelocityTask) project.getTasks().findByName("velocityVpp");
        filter = task.getFilter();

        assertEquals(testValue, filter);
    }
}
