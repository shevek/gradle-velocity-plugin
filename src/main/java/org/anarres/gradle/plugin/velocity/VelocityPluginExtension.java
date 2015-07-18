package org.anarres.gradle.plugin.velocity;

import groovy.lang.Closure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The velocity plugin extension.
 *
 * This allows configuring the velocity plugin using a
 * <code>velocity { }</code> block.
 *
 * @author shevek
 */
public class VelocityPluginExtension {

    public static final String DEFAULT_INPUT_DIR = "src/main/velocity";
    public static final String DEFAULT_OUTPUT_DIR = "build/generated-sources/velocity";
    public static final String DEFAULT_FILTER_REGEX = "**/*.java";

    public Object inputDir = DEFAULT_INPUT_DIR;
    public Object outputDir = DEFAULT_OUTPUT_DIR;
    public String filter = DEFAULT_FILTER_REGEX;
    public List<Object> includeDirs = new ArrayList<Object>();
    public Map<String, Object> contextValues = new HashMap<String, Object>();

    /*
    public Map<String, Object> getContext() {
        return contextValues;
    }
    */

    void context(Map<String, Object> map) {
        contextValues.putAll(map);
    }

    void context(Closure<?> closure) {
        closure.setDelegate(contextValues);
        closure.call();
    }

}
