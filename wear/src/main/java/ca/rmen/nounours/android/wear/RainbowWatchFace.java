/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ca.rmen.nounours.android.wear;

import ca.rmen.nounours.android.common.settings.NounoursSettings;

public class RainbowWatchFace extends NounoursWatchFace {
    private final NounoursSettings mSettings;

    public RainbowWatchFace() {
        super();
        mSettings = new WearSettings("5000", 0xff000000);
    }

    @Override
    public NounoursSettings getSettings() {
        return mSettings;
    }
}
