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

public class WovenConstants {
	private WovenConstants() {
		throw new UnsupportedOperationException("WovenConstants only contains static definitions.");
	}

	public static final class SourceSets {
		public static final String TEST_MOD = "testmod";

		private SourceSets() {
			throw new UnsupportedOperationException("WovenConstants$SourceSets only contains static definitions.");
		}
	}
}
