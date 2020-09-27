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

import org.gradle.external.javadoc.JavadocMemberLevel;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class WovenConstants {
	public static final String ANNOTATIONS_VERSION = "20.1.0";
	public static final String ENCODING = "UTF-8";
	public static final String WOVEN_MAVEN = "https://maven.wovenmc.net";

	private WovenConstants() {
		throw new UnsupportedOperationException("WovenConstants only contains static definitions.");
	}

	public static final class SourceSets {
		public static final String TEST_MOD = "testmod";

		private SourceSets() {
			throw new UnsupportedOperationException("WovenConstants$SourceSets only contains static definitions.");
		}
	}

	public static final class JavaDoc {
		public static final String INCLUDE = "**/api/**";
		public static final JavadocMemberLevel MEMBER_LEVEL = JavadocMemberLevel.PACKAGE;
		public static final @Unmodifiable List<String> LINKS = Collections.unmodifiableList(Arrays.asList(
				"https://guava.dev/releases/21.0/api/docs/",
				"https://asm.ow2.io/javadoc/",
				"https://docs.oracle.com/javase/8/docs/api/",
				"http://jenkins.liteloader.com/job/Mixin/javadoc/",
				"https://logging.apache.org/log4j/2.x/log4j-api/apidocs/",
				"https://javadoc.lwjgl.org/"
		));

		public static @Unmodifiable List<String> getLinks(WovenExtension ext) {
			List<String> links = new ArrayList<>(LINKS);
			links.add("https://javadoc.io/doc/org.jetbrains/annotations/" + ext.getAnnotationsVersion());
			return Collections.unmodifiableList(links);
		}

		private JavaDoc() {
			throw new UnsupportedOperationException("WovenConstants$JavaDoc only contains static definitions.");
		}
	}

	public static final class Checkstyle {
		public static final String FILE_NAME = "checkstyle.xml";
		public static final String VERSION = "8.31";

		private Checkstyle() {
			throw new UnsupportedOperationException("WovenConstants$Checkstyle only contains static definitions");
		}
	}

	public static final class License {
		public static final String HEADER_FILE = "HEADER";

		private License() {
			throw new UnsupportedOperationException("WovenConstants$License only contains static definitions");
		}
	}
}
