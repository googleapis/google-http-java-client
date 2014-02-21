/*
 * Copyright (c) 2011 Google Inc.
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

package com.google.api.client.http;

import com.google.api.client.util.Data;
import com.google.api.client.util.FieldInfo;
import com.google.api.client.util.Preconditions;
import com.google.api.client.util.Types;
import com.google.api.client.util.escape.CharEscapers;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Expands URI Templates.
 *
 * This Class supports Level 1 templates and all Level 4 composite templates as described in:
 * <a href="http://tools.ietf.org/html/rfc6570">RFC 6570</a>.
 *
 * Specifically, for the variables:
 * var := "value"
 * list := ["red", "green", "blue"]
 * keys := [("semi", ";"),("dot", "."),("comma", ",")]
 *
 * The following templates results in the following expansions:
 * {var}     ->   value
 * {list}    ->   red,green,blue
 * {list*}   ->   red,green,blue
 * {keys}    ->   semi,%3B,dot,.,comma,%2C
 * {keys*}   ->   semi=%3B,dot=.,comma=%2C
 * {+list}   ->   red,green,blue
 * {+list*}  ->   red,green,blue
 * {+keys}   ->   semi,;,dot,.,comma,,
 * {+keys*}  ->   semi=;,dot=.,comma=,
 * {#list}   ->   #red,green,blue
 * {#list*}  ->   #red,green,blue
 * {#keys}   ->   #semi,;,dot,.,comma,,
 * {#keys*}  ->   #semi=;,dot=.,comma=,
 * X{.list}  ->   X.red,green,blue
 * X{.list*} ->   X.red.green.blue
 * X{.keys}  ->   X.semi,%3B,dot,.,comma,%2C
 * X{.keys*} ->   X.semi=%3B.dot=..comma=%2C
 * {/list}   ->   /red,green,blue
 * {/list*}  ->   /red/green/blue
 * {/keys}   ->   /semi,%3B,dot,.,comma,%2C
 * {/keys*}  ->   /semi=%3B/dot=./comma=%2C
 * {;list}   ->   ;list=red,green,blue
 * {;list*}  ->   ;list=red;list=green;list=blue
 * {;keys}   ->   ;keys=semi,%3B,dot,.,comma,%2C
 * {;keys*}  ->   ;semi=%3B;dot=.;comma=%2C
 * {?list}   ->   ?list=red,green,blue
 * {?list*}  ->   ?list=red&list=green&list=blue
 * {?keys}   ->   ?keys=semi,%3B,dot,.,comma,%2C
 * {?keys*}  ->   ?semi=%3B&dot=.&comma=%2C
 * {&list}   ->   &list=red,green,blue
 * {&list*}  ->   &list=red&list=green&list=blue
 * {&keys}   ->   &keys=semi,%3B,dot,.,comma,%2C
 * {&keys*}  ->   &semi=%3B&dot=.&comma=%2C
 *
 * @since 1.6
 * @author Ravi Mistry
 */
public class UriTemplate {

  static final Map<Character, CompositeOutput> COMPOSITE_PREFIXES =
      new HashMap<Character, CompositeOutput>();

  static {
    CompositeOutput.values();
  }

  private static final String COMPOSITE_NON_EXPLODE_JOINER = ",";

  /**
   * Contains information on how to output a composite value.
   */
  private enum CompositeOutput {

    /** Reserved expansion. */
    PLUS('+', "", ",", false, true),

    /** Fragment expansion. */
    HASH('#', "#", ",", false, true),

    /** Label expansion with dot-prefix. */
    DOT('.', ".", ".", false, false),

    /** Path segment expansion. */
    FORWARD_SLASH('/', "/", "/", false, false),

    /** Path segment parameter expansion. */
    SEMI_COLON(';', ";", ";", true, false),

    /** Form-style query expansion. */
    QUERY('?', "?", "&", true, false),

    /** Form-style query continuation. */
    AMP('&', "&", "&", true, false),

    /** Simple expansion. */
    SIMPLE(null, "", ",", false, false);

    private final Character propertyPrefix;
    private final String outputPrefix;
    private final String explodeJoiner;
    private final boolean requiresVarAssignment;
    private final boolean reservedExpansion;

    /**
     * @param propertyPrefix The prefix of a parameter or {@code null} for none. In {+var} the
     *        prefix is '+'
     * @param outputPrefix The string that should be prefixed to the expanded template.
     * @param explodeJoiner The delimiter used to join composite values.
     * @param requiresVarAssignment Denotes whether or not the expanded template should contain
     *        an assignment with the variable.
     * @param reservedExpansion Reserved expansion allows pct-encoded triplets and characters in
     *        the reserved set.
     */
    CompositeOutput(Character propertyPrefix, String outputPrefix, String explodeJoiner,
        boolean requiresVarAssignment, boolean reservedExpansion) {
      this.propertyPrefix = propertyPrefix;
      this.outputPrefix = Preconditions.checkNotNull(outputPrefix);
      this.explodeJoiner = Preconditions.checkNotNull(explodeJoiner);
      this.requiresVarAssignment = requiresVarAssignment;
      this.reservedExpansion = reservedExpansion;
      if (propertyPrefix != null) {
        COMPOSITE_PREFIXES.put(propertyPrefix, this);
      }
    }

    /**
     * Returns the string that should be prefixed to the expanded template.
     */
    String getOutputPrefix() {
      return outputPrefix;
    }

    /**
     * Returns the delimiter used to join composite values.
     */
    String getExplodeJoiner() {
      return explodeJoiner;
    }

    /**
     *  Returns whether or not the expanded template should contain an assignment with the variable.
     */
    boolean requiresVarAssignment() {
      return requiresVarAssignment;
    }

    /**
     * Returns the start index of the var name. If the variable contains a prefix the start index
     * will be 1 else it will be 0.
     */
    int getVarNameStartIndex() {
      return propertyPrefix == null ? 0 : 1;
    }

    /**
     * Encodes the specified value. If reserved expansion is turned on then
     * pct-encoded triplets and characters are allowed in the reserved set.
     *
     * @param value The string to be encoded.
     *
     * @return The encoded string.
     */
    String getEncodedValue(String value) {
      String encodedValue;
      if (reservedExpansion) {
        // Reserved expansion allows pct-encoded triplets and characters in the reserved set.
        encodedValue = CharEscapers.escapeUriPath(value);
      } else {
        encodedValue = CharEscapers.escapeUri(value);
      }
      return encodedValue;
    }

    boolean getReservedExpansion() {
      return reservedExpansion;
    }
  }

  static CompositeOutput getCompositeOutput(String propertyName) {
    CompositeOutput compositeOutput = COMPOSITE_PREFIXES.get(propertyName.charAt(0));
    return compositeOutput == null ? CompositeOutput.SIMPLE : compositeOutput;
  }

  /**
   * Constructs a new {@code Map<String, Object>} from an {@code Object}.
   *
   * <p>
   * There are no null values in the returned map.
   * </p>
   */
  private static Map<String, Object> getMap(Object obj) {
    // Using a LinkedHashMap to maintain the original order of insertions. This is done to help
    // with handling unused parameters and makes testing easier as well.
    Map<String, Object> map = new LinkedHashMap<String, Object>();
    for (Map.Entry<String, Object> entry : Data.mapOf(obj).entrySet()) {
      Object value = entry.getValue();
      if (value != null && !Data.isNull(value)) {
        map.put(entry.getKey(), value);
      }
    }
    return map;
  }

  /**
   * Expands templates in a URI template that is relative to a base URL.
   *
   * <p>
   * If the URI template starts with a "/" the raw path from the base URL is stripped out. If the
   * URI template is a full URL then it is used instead of the base URL.
   * </p>
   *
   * <p>
   * Supports Level 1 templates and all Level 4 composite templates as described in:
   * <a href="http://tools.ietf.org/html/rfc6570">RFC 6570</a>.
   * </p>
   *
   * @param baseUrl The base URL which the URI component is relative to.
   * @param uriTemplate URI component. It may contain one or more sequences of the form "{name}",
   *        where "name" must be a key in variableMap.
   * @param parameters an object with parameters designated by Key annotations. If the template has
   *        no variable references, parameters may be {@code null}.
   * @param addUnusedParamsAsQueryParams If true then parameters that do not match the template are
   *        appended to the expanded template as query parameters.
   * @return The expanded template
   * @since 1.7
   */
  public static String expand(String baseUrl, String uriTemplate, Object parameters,
      boolean addUnusedParamsAsQueryParams) {
    String pathUri;
    if (uriTemplate.startsWith("/")) {
      // Remove the base path from the base URL.
      GenericUrl url = new GenericUrl(baseUrl);
      url.setRawPath(null);
      pathUri = url.build() + uriTemplate;
    } else if (uriTemplate.startsWith("http://") || uriTemplate.startsWith("https://")) {
      pathUri = uriTemplate;
    } else {
      pathUri = baseUrl + uriTemplate;
    }
    return expand(pathUri, parameters, addUnusedParamsAsQueryParams);
  }

  /**
   * Expands templates in a URI.
   *
   * <p>
   * Supports Level 1 templates and all Level 4 composite templates as described in:
   * <a href="http://tools.ietf.org/html/rfc6570">RFC 6570</a>.
   * </p>
   *
   * @param pathUri URI component. It may contain one or more sequences of the form "{name}", where
   *        "name" must be a key in variableMap
   * @param parameters an object with parameters designated by Key annotations. If the template has
   *        no variable references, parameters may be {@code null}.
   * @param addUnusedParamsAsQueryParams If true then parameters that do not match the template are
   *        appended to the expanded template as query parameters.
   * @return The expanded template
   * @since 1.6
   */
  public static String expand(String pathUri, Object parameters,
      boolean addUnusedParamsAsQueryParams) {
    Map<String, Object> variableMap = getMap(parameters);
    StringBuilder pathBuf = new StringBuilder();
    int cur = 0;
    int length = pathUri.length();
    while (cur < length) {
      int next = pathUri.indexOf('{', cur);
      if (next == -1) {
        if (cur == 0 && !addUnusedParamsAsQueryParams) {
          // No expansions exist and we do not need to add any query parameters.
          return pathUri;
        }
        pathBuf.append(pathUri.substring(cur));
        break;
      }
      pathBuf.append(pathUri.substring(cur, next));
      int close = pathUri.indexOf('}', next + 2);
      String template = pathUri.substring(next + 1, close);
      cur = close + 1;

      boolean containsExplodeModifier = template.endsWith("*");
      CompositeOutput compositeOutput = getCompositeOutput(template);

      int varNameStartIndex = compositeOutput.getVarNameStartIndex();
      int varNameEndIndex = template.length();
      if (containsExplodeModifier) {
        // The expression contains an explode modifier '*' at the end, update end index.
        varNameEndIndex = varNameEndIndex - 1;
      }
      // Now get varName devoid of any prefixes and explode modifiers.
      String varName = template.substring(varNameStartIndex, varNameEndIndex);

      Object value = variableMap.remove(varName);
      if (value == null) {
        // The value for this variable is undefined. continue with the next template.
        continue;
      }
      if (value instanceof Iterator<?>) {
        // Get the list property value.
        Iterator<?> iterator = (Iterator<?>) value;
        value = getListPropertyValue(varName, iterator, containsExplodeModifier, compositeOutput);
      } else if (value instanceof Iterable<?> || value.getClass().isArray()) {
       // Get the list property value.
        Iterator<?> iterator = Types.iterableOf(value).iterator();
        value = getListPropertyValue(varName, iterator, containsExplodeModifier, compositeOutput);
      } else if (value.getClass().isEnum()) {
        String name = FieldInfo.of((Enum<?>) value).getName();
        if (name != null) {
          value = CharEscapers.escapeUriPath(name);
        }
      } else if (!Data.isValueOfPrimitiveType(value)) {
        // Parse the value as a key/value map.
        Map<String, Object> map = getMap(value);
        value = getMapPropertyValue(varName, map, containsExplodeModifier, compositeOutput);
      } else {
        // For everything else...
        if (compositeOutput.getReservedExpansion()) {
          value = CharEscapers.escapeUriPathWithoutReserved(value.toString());
        } else {
          value = CharEscapers.escapeUriPath(value.toString());
        }
      }
      pathBuf.append(value);
    }
    if (addUnusedParamsAsQueryParams) {
      // Add the parameters remaining in the variableMap as query parameters.
      GenericUrl.addQueryParams(variableMap.entrySet(), pathBuf);
    }
    return pathBuf.toString();
  }

  /**
   * Expand the template of a composite list property.
   * Eg: If d := ["red", "green", "blue"]
   *     then {/d*} is expanded to "/red/green/blue"
   *
   * @param varName The name of the variable the value corresponds to. Eg: "d"
   * @param iterator The iterator over list values. Eg: ["red", "green", "blue"]
   * @param containsExplodeModifier Set to true if the template contains the explode modifier "*"
   * @param compositeOutput An instance of CompositeOutput. Contains information on how the
   *     expansion should be done
   * @return The expanded list template
   * @throws IllegalArgumentException if the required list path parameter is empty
   */
  private static String getListPropertyValue(String varName, Iterator<?> iterator,
      boolean containsExplodeModifier, CompositeOutput compositeOutput) {
    if (!iterator.hasNext()) {
      return "";
    }
    StringBuilder retBuf = new StringBuilder();
    retBuf.append(compositeOutput.getOutputPrefix());
    String joiner;
    if (containsExplodeModifier) {
      joiner = compositeOutput.getExplodeJoiner();
    } else {
      joiner = COMPOSITE_NON_EXPLODE_JOINER;
      if (compositeOutput.requiresVarAssignment()) {
        retBuf.append(CharEscapers.escapeUriPath(varName));
        retBuf.append("=");
      }
    }
    while (iterator.hasNext()) {
      if (containsExplodeModifier && compositeOutput.requiresVarAssignment()) {
        retBuf.append(CharEscapers.escapeUriPath(varName));
        retBuf.append("=");
      }
      retBuf.append(compositeOutput.getEncodedValue(iterator.next().toString()));
      if (iterator.hasNext()) {
        retBuf.append(joiner);
      }
    }
    return retBuf.toString();
  }

  /**
   * Expand the template of a composite map property.
   * Eg: If d := [("semi", ";"),("dot", "."),("comma", ",")]
   *     then {/d*} is expanded to "/semi=%3B/dot=./comma=%2C"
   *
   * @param varName The name of the variable the value corresponds to. Eg: "d"
   * @param map The map property value. Eg: [("semi", ";"),("dot", "."),("comma", ",")]
   * @param containsExplodeModifier Set to true if the template contains the explode modifier "*"
   * @param compositeOutput An instance of CompositeOutput. Contains information on how the
   *     expansion should be done
   * @return The expanded map template
   * @throws IllegalArgumentException if the required list path parameter is map
   */
  private static String getMapPropertyValue(String varName, Map<String, Object> map,
      boolean containsExplodeModifier, CompositeOutput compositeOutput) {
    if (map.isEmpty()) {
      return "";
    }
    StringBuilder retBuf = new StringBuilder();
    retBuf.append(compositeOutput.getOutputPrefix());
    String joiner;
    String mapElementsJoiner;
    if (containsExplodeModifier) {
      joiner = compositeOutput.getExplodeJoiner();
      mapElementsJoiner = "=";
    } else {
      joiner = COMPOSITE_NON_EXPLODE_JOINER;
      mapElementsJoiner = COMPOSITE_NON_EXPLODE_JOINER;
      if (compositeOutput.requiresVarAssignment()) {
        retBuf.append(CharEscapers.escapeUriPath(varName));
        retBuf.append("=");
      }
    }
    for (Iterator<Map.Entry<String, Object>> mapIterator = map.entrySet().iterator();
        mapIterator.hasNext();) {
      Map.Entry<String, Object> entry = mapIterator.next();
      String encodedKey = compositeOutput.getEncodedValue(entry.getKey());
      String encodedValue = compositeOutput.getEncodedValue(entry.getValue().toString());
      retBuf.append(encodedKey);
      retBuf.append(mapElementsJoiner);
      retBuf.append(encodedValue);
      if (mapIterator.hasNext()) {
        retBuf.append(joiner);
      }
    }
    return retBuf.toString();
  }
}
