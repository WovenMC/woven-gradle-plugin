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

package net.wovenmc.woven.gradle.extension;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.jetbrains.annotations.NotNull;

public class WovenApiExtension {
	private final Project project;

	public WovenApiExtension(@NotNull Project project) {
		this.project = project;
	}

	public Dependency module(String moduleName, String moduleVersion) {
		return project.getDependencies().create(this.getDependencyNotation(moduleName, moduleVersion));
	}

	private String getDependencyNotation(String moduleName, String moduleVersion) {
		return String.format("net.wovenmc.woven:%s:%s", moduleName, moduleVersion);
	}
}
