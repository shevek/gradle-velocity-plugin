/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.anarres.gradle.plugin.velocity;

import groovy.lang.Closure;
import javax.annotation.Nonnull;
import org.gradle.api.Action;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.model.ObjectFactory;
import org.gradle.util.ConfigureUtil;

/**
 *
 * @author shevek
 */
public class VelocitySourceVirtualDirectory {

    private final SourceDirectorySet velocity;

    public VelocitySourceVirtualDirectory(String displayName, ObjectFactory objectFactory) {
        velocity = objectFactory.sourceDirectorySet(displayName + ".velocity", displayName + " Velocity source");
        velocity.getFilter().include("**/*.java");
    }

    @Nonnull
    public SourceDirectorySet getVelocity() {
        return velocity;
    }

    @Nonnull
    public VelocitySourceVirtualDirectory velocity(Closure configureClosure) {
        ConfigureUtil.configure(configureClosure, getVelocity());
        return this;
    }

    @Nonnull
    public VelocitySourceVirtualDirectory velocity(Action<? super SourceDirectorySet> configureAction) {
        configureAction.execute(getVelocity());
        return this;
    }
}
