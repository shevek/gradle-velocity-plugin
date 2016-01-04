/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.anarres.gradle.plugin.velocity;

import groovy.lang.Closure;
import javax.annotation.Nonnull;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.internal.file.DefaultSourceDirectorySet;
import org.gradle.api.internal.file.FileResolver;
import org.gradle.util.ConfigureUtil;

/**
 *
 * @author shevek
 */
public class VelocitySourceSet {

    private final SourceDirectorySet velocity;

    public VelocitySourceSet(String displayName, FileResolver fileResolver) {
        velocity = new DefaultSourceDirectorySet(String.format("Velocity %s source", displayName), fileResolver);
        velocity.getFilter().include("**/*.java");
    }

    @Nonnull
    public SourceDirectorySet getVelocity() {
        return velocity;
    }

    @Nonnull
    public VelocitySourceSet velocity(Closure configureClosure) {
        ConfigureUtil.configure(configureClosure, getVelocity());
        return this;
    }
}
