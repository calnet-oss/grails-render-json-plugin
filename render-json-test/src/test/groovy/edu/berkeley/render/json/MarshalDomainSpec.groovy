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
import edu.berkeley.render.json.marshallers.DomainJsonMarshaller
import edu.berkeley.render.json.marshallers.MapJsonMarshaller
import grails.test.mixin.TestMixin
import grails.test.mixin.domain.DomainClassUnitTestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import grails.test.mixin.web.ControllerUnitTestMixin
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject

@TestMixin([GrailsUnitTestMixin, ControllerUnitTestMixin, DomainClassUnitTestMixin])
class MarshalDomainSpec {
    static doWithSpring = ExtendedJSON.doWithSpringRegisterMarshallersClosure

    void setup() {
        mockDomains(TestDomain)
    }

    void "test correct marshallers registered in unit test"() {
        given:
        ExtendedJSON json = new ExtendedJSON()

        expect:
        json.lookupObjectMarshaller(new TestDomain()) instanceof DomainJsonMarshaller
        json.lookupObjectMarshaller(["test": "map"]) instanceof MapJsonMarshaller
        json.lookupObjectMarshaller(new JSONString('{"test" : "jsonmap"}')) instanceof JSONString.ToStringJsonMarshaller
    }

    void "test toJSONObject"() {
        given:
        TestDomain testDomain = new TestDomain()
        JSONObject json = (testDomain as ExtendedJSON).toJSONObject()

        expect:
        json.helloMap?.helloArray instanceof JSONArray
        json.helloMap?.helloArray == ["world1", "world2"]
        !json.containsKey("shouldBeNull")
    }

    void "test toJSONObject, including nulls"() {
        given:
        TestDomainIncludeNulls testDomain = new TestDomainIncludeNulls()
        JSONObject json = (testDomain as ExtendedJSON).toJSONObject()

        expect:
        json.helloMap?.helloArray instanceof JSONArray
        json.helloMap?.helloArray == ["world1", "world2"]
        json.containsKey("shouldBeNull") && json.shouldBeNull == null
    }
}
