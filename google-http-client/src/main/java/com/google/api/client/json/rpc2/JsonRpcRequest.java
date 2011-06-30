/*
 * Copyright (c) 2010 Google Inc.
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

package com.google.api.client.json.rpc2;

import com.google.api.client.util.GenericData;
import com.google.api.client.util.Key;

/**
 * JSON-RPC 2.0 request object.
 *
 * <p>
 * Implementation is not thread-safe.
 * </p>
 *
 * @since 1.0
 * @author Yaniv Inbar
 */
public class JsonRpcRequest extends GenericData {

  /** Version of the JSON-RPC protocol which is {@code "2.0"}. */
  @Key
  private final String jsonrpc = "2.0";

  /**
   * Identifier established by the client that must be a string or a number or {@code null} for a
   * notification and therefore not expect to receive a response.
   */
  @Key
  private Object id;

  /** Name of the method to be invoked. */
  @Key
  private String method;

  /**
   * Structured value that holds the parameter values to be used during the invocation of the method
   * or {@code null} for none.
   */
  @Key
  private Object params;

  /**
   * Returns the version of the JSON-RPC protocol which is {@code "2.0"}.
   *
   * @since 1.5
   */
  public String getVersion() {
    return jsonrpc;
  }

  /**
   * Returns the identifier established by the client that must be a string or a number or {@code
   * null} for a notification and therefore not expect to receive a response.
   *
   * @since 1.5
   */
  public Object getId() {
    return id;
  }

  /**
   * Sets the identifier established by the client that must be a string or a number or {@code null}
   * for a notification and therefore not expect to receive a response.
   *
   * @since 1.5
   */
  public void setId(Object id) {
    this.id = id;
  }

  /**
   * Returns the name of the method to be invoked.
   *
   * @since 1.5
   */
  public String getMethod() {
    return method;
  }

  /**
   * Sets the name of the method to be invoked.
   *
   * @since 1.5
   */
  public void setMethod(String method) {
    this.method = method;
  }

  /**
   * Returns the structured value that holds the parameter values to be used during the invocation
   * of the method or {@code null} for none.
   *
   * @since 1.5
   */
  public Object getParameters() {
    return params;
  }

  /**
   * Sets the structured value that holds the parameter values to be used during the invocation of
   * the method or {@code null} for none.
   *
   * @since 1.5
   */
  public void setParameters(Object parameters) {
    this.params = parameters;
  }
}
