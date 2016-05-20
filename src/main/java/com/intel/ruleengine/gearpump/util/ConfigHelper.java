/**
 * Copyright (c) 2016 Intel Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intel.ruleengine.gearpump.util;

import io.gearpump.cluster.UserConfig;
import scala.Option;

public final class ConfigHelper {

    private ConfigHelper() {

    }

    public static String getConfigValue(UserConfig config, String key) {
        Option<String> property = config.getString(key);
        if (property.nonEmpty()) {
            return property.get();
        }
        return null;
    }
}
