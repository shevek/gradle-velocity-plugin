# Introduction

This is a gradle plugin which supports Velocity preprocessing of
source files.

# Usage:

To apply a default configuration which preprocesses src/main/velocity
into build/generated-sources/velocity:

	buildscript {
		dependencies {
			classpath 'org.anarres.gradle:gradle-velocity-plugin:[1.0.0,)'
		}
	}

	apply plugin: 'velocity'

	velocity {
		includeDir ...
		filter = '**/*.vtl'

		context {
			// Configure a HashMap with context values.
		}
	}

For more advanced usage:

	task('customVpp', type: VelocityTask) {
		// ...
	}

# API Documentation

The [JavaDoc API](http://shevek.github.io/gradle-velocity-plugin/docs/javadoc/)
is also available.

