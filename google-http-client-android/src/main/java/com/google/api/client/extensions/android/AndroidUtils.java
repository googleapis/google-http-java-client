/*
 * Copyright 2012 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.api.client.extensions.android;

import android.os.Build;
import com.google.api.client.util.Beta;
import com.google.api.client.util.Preconditions;

/**
 * {@link Beta} <br>
 * Utilities for Android.
 *
 * @since 1.11
 * @author Yaniv Inbar
 */
@Beta
public class AndroidUtils {

  /**
   * Returns whether the SDK version is the given level or higher.
   *
   * @see android.os.Build.VERSION_CODES
   */
  public static boolean isMinimumSdkLevel(int minimumSdkLevel) {
    return Build.VERSION.SDK_INT >= minimumSdkLevel;
  }

  /**
   * Throws an {@link IllegalArgumentException} if {@link #isMinimumSdkLevel(int)} is {@code false}
   * on the given level.
   *
   * @see android.os.Build.VERSION_CODES
   */
  public static void checkMinimumSdkLevel(int minimumSdkLevel) {
    Preconditions.checkArgument(
        isMinimumSdkLevel(minimumSdkLevel),
        "running on Android SDK level %s but requires minimum %s",
        Build.VERSION.SDK_INT,
        minimumSdkLevel);
  }

  private AndroidUtils() {}
}
