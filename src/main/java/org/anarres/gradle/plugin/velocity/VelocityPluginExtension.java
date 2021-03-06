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

    public List<Object> includeDirs = new ArrayList<Object>();
    public Map<String, Object> contextValues = new HashMap<String, Object>();

    void context(Map<String, Object> map) {
        contextValues.putAll(map);
    }

    void context(Closure<?> closure) {
        closure.setDelegate(contextValues);
        closure.call();
    }

}
