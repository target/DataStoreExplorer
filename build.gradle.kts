fun properties(key: String) = providers.gradleProperty(key)
fun environment(key: String) = providers.environmentVariable(key)

plugins {
	id("java")
	alias(libs.plugins.kotlin)
	alias(libs.plugins.gradleIntelliJPlugin)
	alias(libs.plugins.changelog)
	alias(libs.plugins.kotlinxSerialization)
}

group = properties("pluginGroup").get()
version = properties("pluginVersion").get()

repositories {
	mavenCentral()
	google()
}

if (!hasProperty("studioCompilePath")) {
	throw GradleException("No StudioCompilePath value was set, please create gradle.properties file")
}

val studioCompilePath = properties("studioCompilePath")

dependencies {
	implementation(libs.coroutinesCore)
	implementation(libs.protobufJavaUtil)
	implementation(libs.protocJar)
	implementation(libs.kotlinxSerializationJson)
	implementation(libs.datastorePreferencesCore)

	compileOnly(fileTree("dir" to "$studioCompilePath/plugins/android/lib", "include" to "*.jar"))
	compileOnly(fileTree("dir" to "$studioCompilePath/lib", "include" to "*.jar"))

	testImplementation(libs.coroutinesTest)
	testImplementation(libs.assertjCore)
	testImplementation(libs.junit)
	testImplementation(libs.mockk)
	testImplementation(fileTree("dir" to "$studioCompilePath/plugins/android/lib", "include" to "*.jar"))
	testImplementation(fileTree("dir" to "$studioCompilePath/lib", "include" to "*.jar"))
}

// Set the JVM language level used to build the project. Use Java 11 for 2020.3+, and Java 17 for 2022.2+.
kotlin {
	jvmToolchain(11)
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
	pluginName.set(properties("pluginName"))
	version.set(properties("platformVersion"))
	type.set(properties("platformType")) // Target IDE Platform

	// Plugin Dependencies. Uses `platformPlugins` property from the gradle.properties file.
	plugins.set(properties("platformPlugins").map { it.split(',').map(String::trim).filter(String::isNotEmpty) })
}

// Configure Gradle Changelog Plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
changelog {
	groups.set(emptyList())
	repositoryUrl.set(properties("pluginRepositoryUrl"))
}

tasks {
	wrapper {
		gradleVersion = properties("gradleVersion").get()
	}

	patchPluginXml {
		version.set(properties("pluginVersion"))
		sinceBuild.set(properties("pluginSinceBuild"))
		untilBuild.set(properties("pluginUntilBuild"))
	}

	signPlugin {
		certificateChain.set(environment("CERTIFICATE_CHAIN"))
		privateKey.set(environment("PRIVATE_KEY"))
		password.set(environment("PRIVATE_KEY_PASSWORD"))
	}

	publishPlugin {
		dependsOn("patchChangelog")
		token.set(environment("PUBLISH_TOKEN"))
		// The pluginVersion is based on the SemVer (https://semver.org) and supports pre-release labels, like 2.1.7-alpha.3
		// Specify pre-release label to publish the plugin in a custom Release Channel automatically. Read more:
		// https://plugins.jetbrains.com/docs/intellij/deployment.html#specifying-a-release-channel
		channels.set(
			properties("pluginVersion").map { listOf(it.split('-').getOrElse(1) { "default" }.split('.').first()) }
		)
	}

	run {
		// https://youtrack.jetbrains.com/issue/IDEA-296777
		// workaround for https://youtrack.jetbrains.com/issue/IDEA-285839/Classpath-clash-when-using-coroutines-in-an-unbundled-IntelliJ-plugin
		buildPlugin {
			exclude { "coroutines" in it.name }
		}
		prepareSandbox {
			exclude { "coroutines" in it.name }
		}
	}
}

