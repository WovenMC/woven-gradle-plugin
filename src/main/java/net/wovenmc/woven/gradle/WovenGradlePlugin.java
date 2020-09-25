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

import net.wovenmc.woven.gradle.extension.WovenApiExtension;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.internal.impldep.com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;

public class WovenGradlePlugin implements Plugin<Project> {
	@Override
	public void apply(@NotNull Project project) {
		WovenPluginExtension extension = project.getExtensions().create("woven", WovenPluginExtension.class, project);

		// Apply default plugins
		project.apply(ImmutableMap.of("plugin", "java-library"));

		if (extension.checkstyle) {
			project.apply(ImmutableMap.of("plugin", "checkstyle"));
		}

		project.getExtensions().create("wovenApi", WovenApiExtension.class, project);

		project.getExtensions().configure(JavaPluginExtension.class, ext -> {
			ext.setSourceCompatibility(extension.javaVersion);
			ext.setTargetCompatibility(extension.javaVersion);

			ext.withSourcesJar();
			ext.withJavadocJar();
		});
	}
}
