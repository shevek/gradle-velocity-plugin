# Introduction

This is a gradle plugin which supports Velocity preprocessing of
source files.

# Usage:

To apply a default configuration which preprocesses src/main/velocity
into build/generated-sources/velocity:

	buildscript {
		dependencies {
			compile 'org.anarres.gradle:gradle-velocity-plugin:1.0.0'
		}
	}

	apply plugin: 'velocity'

	velocity {
		context {
			// Configure a HashMap with context values.
		}
	}

For more advanced usage:

	task('customVpp', type: VelocityTask) {
		// ...
	}

