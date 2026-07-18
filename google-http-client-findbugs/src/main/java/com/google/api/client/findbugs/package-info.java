/*
 * Copyright (c) 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

/**
 * SpotBugs package which supports the custom Google APIs Client Library SpotBugs plugin.
 *
 * <p>The artifact ID retains its historical {@code google-http-client-findbugs} name for
 * compatibility.
 *
 * <p>Usage on pom.xml:
 *
 * <pre>
 * &lt;plugin&gt;
 * &lt;groupId&gt;com.github.spotbugs&lt;/groupId&gt;
 * &lt;artifactId&gt;spotbugs-maven-plugin&lt;/artifactId&gt;
 * ...
 * &lt;configuration&gt;
 * &lt;plugins&gt;
 * &lt;plugin&gt;
 * &lt;groupId&gt;com.google.http-client&lt;/groupId&gt;
 * &lt;artifactId&gt;google-http-client-findbugs&lt;/artifactId&gt;
 * &lt;version&gt;${project.http.version}&lt;/version&gt;
 * &lt;/plugin&gt;
 * &lt;/plugins&gt;
 * &lt;/configuration&gt;
 * ...
 * &lt;/plugin&gt;
 * </pre>
 *
 * @author Eyal Peled
 */
package com.google.api.client.findbugs;
