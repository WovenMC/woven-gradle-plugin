/*
 * Copyright (c) 2020 WovenMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.wovenmc.woven.gradle;

import groovy.util.Node;
import net.fabricmc.loom.task.RemapJarTask;
import net.minecrell.gradle.licenser.LicenseExtension;
import net.minecrell.gradle.licenser.Licenser;
import net.wovenmc.woven.gradle.extension.WovenApiExtension;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.plugins.JavaLibraryPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.plugins.PluginContainer;
import org.gradle.api.plugins.quality.CheckstyleExtension;
import org.gradle.api.plugins.quality.CheckstylePlugin;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;
import org.gradle.api.publish.plugins.PublishingPlugin;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskCollection;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.external.javadoc.StandardJavadocDocletOptions;
import org.jetbrains.annotations.NotNull;

public class WovenGradlePlugin implements Plugin<Project> {
	@Override
	public void apply(@NotNull Project project) {
		final PluginContainer plugins = project.getPlugins();
		final ExtensionContainer extensions = project.getExtensions();
		final TaskContainer tasks = project.getTasks();

		WovenExtension extension = extensions.create("woven", WovenExtension.class, project);

		// Apply default plugins
		plugins.apply(JavaLibraryPlugin.class);
		plugins.apply(PublishingPlugin.class);
		plugins.apply(MavenPublishPlugin.class);
		plugins.apply(CheckstylePlugin.class);
		plugins.apply(Licenser.class);

		extensions.create("wovenApi", WovenApiExtension.class, project);

		// Dependencies
		project.getDependencies().add("api", "org.jetbrains:annotations:" + extension.getAnnotationsVersion());

		// Java
		extensions.configure(JavaPluginExtension.class, ext -> {
			ext.setSourceCompatibility(extension.javaVersion);
			ext.setTargetCompatibility(extension.javaVersion);

			ext.withSourcesJar();
			ext.withJavadocJar();
		});

		// UTF-8 time
		tasks.withType(JavaCompile.class).forEach(it -> it.getOptions().setEncoding(WovenConstants.ENCODING));

		// Bundle LICENSE file into the JAR.
		tasks.withType(AbstractArchiveTask.class).forEach(it -> it.from(project.getRootProject().file("LICENSE")));

		tasks.withType(Javadoc.class).getByName("javadoc", task -> {
			task.options(it -> {
				StandardJavadocDocletOptions options = (StandardJavadocDocletOptions) it;
				options.setSource("8");
				options.setEncoding(WovenConstants.ENCODING);
				options.setCharSet(WovenConstants.ENCODING);
				options.setMemberLevel(WovenConstants.JavaDoc.MEMBER_LEVEL);
				options.setLinks(WovenConstants.JavaDoc.getLinks(extension));
				// Disable the crazy super-strict doclint tool in Java 8
				options.addStringOption("Xdoclint:none", "-quiet");
			});

			SourceSetContainer sourceSets = project.getExtensions().getByType(SourceSetContainer.class);
			final SourceSet mainSourceSet = sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME);
			task.setClasspath(mainSourceSet.getRuntimeClasspath());

			task.include(WovenConstants.JavaDoc.INCLUDE);
			task.setFailOnError(false);
		});

		// Checkstyle
		extensions.configure(CheckstyleExtension.class, ext -> {
			ext.setConfigFile(project.getRootProject().file(WovenConstants.Checkstyle.FILE_NAME));
			ext.setToolVersion(WovenConstants.Checkstyle.VERSION);
		});

		// License
		extensions.configure(LicenseExtension.class, ext -> {
			ext.setHeader(project.getRootProject().file(WovenConstants.License.HEADER_FILE));
			ext.include("**/*.java");
		});

		// Publication
		extensions.configure(PublishingExtension.class, ext -> {
			ext.publications(publications -> {
				publications.register("maven", MavenPublication.class, publication -> {
					final TaskCollection<AbstractArchiveTask> archiveTasks = tasks.withType(AbstractArchiveTask.class);

					// Artifacts
					final RemapJarTask remapJarTask = (RemapJarTask) archiveTasks.getByName("remapJar");
					publication.artifact(remapJarTask);

					publication.artifact(archiveTasks.getByName("sourcesJar"), artifact -> {
						// Not using archiveTasks because loom stupidity.
						artifact.builtBy(tasks.getByName("remapSourcesJar"));
					});

					publication.artifact(archiveTasks.getByName("javadocJar"));

					// POM
					publication.pom(pom -> {
						pom.getName().set(project.getName());
						pom.getDescription().set(project.getDescription());

						pom.withXml(xml -> {
							final Node dependenciesNode = xml.asNode().appendNode("dependencies");

							project.getConfigurations().getByName("api").getAllDependencies().forEach(it -> {
								if (it.getGroup() != null || !it.getName().equals("unspecified")) {
									final Node dependencyNode = dependenciesNode.appendNode("dependency");

									dependencyNode.appendNode("groupId", it.getGroup());
									dependencyNode.appendNode("artifactId", it.getName());
									dependencyNode.appendNode("version", it.getVersion());
									dependencyNode.appendNode("scope", "compile");
								}
							});
						});
					});
				});
			});

			ext.repositories(repositories -> {
				repositories.mavenLocal();
				// @TODO: Woven Maven
			});
		});
	}
}
