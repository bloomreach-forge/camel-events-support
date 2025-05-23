/*
 * Copyright 2025 Bloomreach B.V. (https://www.bloomreach.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *         http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onehippo.forge.camel.converter.json;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.camel.Converter;
import org.apache.camel.TypeConverters;

import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * JSON Converter.
 */
@Converter(generateLoader = true)
public class JSONConverter implements TypeConverters {

    @Converter
    public static String toString(JSONObject json) {
        return json.toString();
    }

    @Converter
    public static InputStream toInputStream(JSONObject json) {
        return new ByteArrayInputStream(json.toString().getBytes());
    }

    @Converter
    public static JSONObject toJSON(String jsonString) {
        return (JSONObject) new JSONTokener(jsonString).nextValue();
    }
}
