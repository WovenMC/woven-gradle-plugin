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

import net.fabricmc.loom.LoomGradleExtension;
import net.fabricmc.loom.task.RemapJarTask;
import org.gradle.api.JavaVersion;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;
import org.gradle.jvm.tasks.Jar;
import org.gradle.language.base.plugins.LifecycleBasePlugin;
import org.jetbrains.annotations.NotNull;

public class WovenPluginExtension {
	private final Project project;
	public JavaVersion javaVersion = JavaVersion.VERSION_1_8;

	public WovenPluginExtension(@NotNull Project project) {
		this.project = project;
	}

	/**
	 * Whether the project has a testmod source set configured.
	 *
	 * @return True if a testmod is set up, else false.
	 */
	public boolean isUsingTestmod() {
		final boolean[] result = new boolean[] {false};
		project.getPlugins().withType(JavaPlugin.class, javaPlugin -> {
			if (result[0]) {
				return;
			}

			SourceSetContainer sourceSets = project.getExtensions().getByType(SourceSetContainer.class);
			result[0] = sourceSets.findByName(WovenConstants.SourceSets.TEST_MOD) != null;
		});

		return result[0];
	}

	/**
	 * Create a new {@code testmod} source set for testing library mods.
	 *
	 * <p>The output of the created source set will be provided to the run tasks for use in development ONLY.</p>
	 *
	 * <p>Due to limitations in Loom, this source set cannot have its own dependencies</p>
	 *
	 * @see #withRemappedTestmodSourceSet() to set up a testmod configuration that will produce a production jar
	 */
	public void withTestmodSourceSet() {
		this.setupTestmodSourceSet(false);
	}

	/**
	 * Create a new {@code testmod} source set for testing library mods.
	 *
	 * <p>The output of the created source set will be provided to run tasks, and generate a remapped jar for testing in-game.</p>
	 *
	 * <p>The output jar will not include any JiJ-ed dependencies or an access widener, since those are currently set for the project as a whole.</p>
	 */
	public void withRemappedTestmodSourceSet() {
		this.setupTestmodSourceSet(true);
	}

	private void setupTestmodSourceSet(boolean remap) {
		project.getPlugins().withType(JavaPlugin.class, javaPlugin -> {
			SourceSetContainer sourceSets = project.getExtensions().getByType(SourceSetContainer.class);
			final SourceSet mainSourceSet = sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME);

			// Create a new source set, depending on the main source set
			final SourceSet testModSet = sourceSets.create(WovenConstants.SourceSets.TEST_MOD, set -> {
				project.getDependencies().add(set.getCompileClasspathConfigurationName(), mainSourceSet.getCompileClasspath());
				project.getDependencies().add(set.getRuntimeClasspathConfigurationName(), mainSourceSet.getRuntimeClasspath());
				project.getDependencies().add(set.getImplementationConfigurationName(), mainSourceSet.getOutput());
			});

			// Generate a jar from this source set and add it to the list used by the run configurations
			final TaskProvider<Jar> testModJar = project.getTasks().register(testModSet.getJarTaskName(), Jar.class, jar -> {
				jar.getArchiveClassifier().set(WovenConstants.SourceSets.TEST_MOD + "-dev");
				jar.setGroup(LifecycleBasePlugin.BUILD_GROUP);

				jar.from(testModSet.getOutput());
			});
			project.getExtensions().configure(LoomGradleExtension.class, loom -> loom.getUnmappedModCollection().from(testModJar));

			if (remap) {
				// Generate a remappped jar
				// This doesn't include any JiJ dependencies or access wideners, since those are declared per-project -- and are therefore in the main mod jar
				final TaskProvider<RemapJarTask> remapJar = project.getTasks().register(testModSet.getTaskName("remap", "Jar"),
						RemapJarTask.class, task -> {
							task.getArchiveClassifier().set(WovenConstants.SourceSets.TEST_MOD);
							task.getInput().set(project.getLayout().file(testModJar.map(AbstractArchiveTask::getArchivePath)));
							task.dependsOn(testModJar);
						});
				project.getTasks().named(LifecycleBasePlugin.ASSEMBLE_TASK_NAME).configure(task -> task.dependsOn(remapJar));
			}
		});
	}
}
