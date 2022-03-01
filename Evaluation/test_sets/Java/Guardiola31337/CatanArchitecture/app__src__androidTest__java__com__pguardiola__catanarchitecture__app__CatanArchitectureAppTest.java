/*
 * Copyright (C) 2015 Pablo Guardiola Sánchez.
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

package com.pguardiola.catanarchitecture.app;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import com.pguardiola.catanarchitecture.app.folders.Folders;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.core.IsNull.notNullValue;

@RunWith(AndroidJUnit4.class) public class CatanArchitectureAppTest {

  @Rule public ActivityTestRule<Folders> mActivityRule = new ActivityTestRule<>(Folders.class);

  @Test public void foldersViewShouldExist() {
    onView(withId(R.id.folders)).check(matches(notNullValue()));
  }
}