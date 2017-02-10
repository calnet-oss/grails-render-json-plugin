/*
 * Copyright (c) 2016, Regents of the University of California and
 * contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package edu.berkeley.render.json

import edu.berkeley.render.json.converters.ExtendedJSON
import edu.berkeley.render.json.marshallers.MapJsonMarshaller
import edu.berkeley.util.domain.IncludesExcludesInterface
import grails.test.mixin.TestMixin
import grails.test.mixin.domain.DomainClassUnitTestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import grails.test.mixin.web.ControllerUnitTestMixin
import org.codehaus.groovy.grails.web.json.JSONObject
import spock.lang.Ignore
import spock.lang.Specification

@TestMixin([GrailsUnitTestMixin, ControllerUnitTestMixin, DomainClassUnitTestMixin])
class MarshalMapSpec extends Specification {
    static doWithSpring = ExtendedJSON.doWithSpringRegisterMarshallersClosure

    static class IncludeNullsMap<K, V> extends HashMap<K, V> implements IncludesExcludesInterface {
        @Override
        List<String> getExcludes() {
            return null
        }

        @Override
        List<String> getIncludes() {
            return null
        }

        @Override
        Boolean getIncludeNulls() {
            return true
        }
    }

    static enum EnumKeys {
        KEY1, KEY2, KEY3
    }

    void "test correct map marshaller registered in unit test"() {
        given:
        ExtendedJSON json = new ExtendedJSON()

        expect:
        json.lookupObjectMarshaller(["test": "map"]) instanceof MapJsonMarshaller
    }

    @Ignore
    void "test map to json marshalling"() {
        given:
        Map map = ["hello": "world", "nullKey": null]
        JSONObject json = (map as ExtendedJSON).toJSONObject()

        expect:
        json.hello == "world"
        !json.containsKey("nullKey")
    }

    void "test map to json marshalling using an includeNulls map"() {
        given:
        IncludeNullsMap map = ["hello": "world", "nullKey": null]
        JSONObject json = (map as ExtendedJSON).toJSONObject()

        expect:
        json.hello == "world"
        json.containsKey("nullKey") && json.nullKey == null
    }

    void "test map to json marshalling where keys are not strings"() {
        given:
        IncludeNullsMap map = [(EnumKeys.KEY1): "value1", (EnumKeys.KEY2): "value2", (EnumKeys.KEY3): "value3"]
        JSONObject json = (map as ExtendedJSON).toJSONObject()

        expect:
        json.KEY1 == 'value1'
        json.KEY2 == 'value2'
        json.KEY3 == 'value3'
    }
}
