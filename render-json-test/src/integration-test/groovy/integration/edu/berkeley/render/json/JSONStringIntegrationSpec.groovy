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

import grails.converters.JSON
import grails.testing.mixin.integration.Integration
import groovy.json.JsonBuilder
import spock.lang.Specification

@Integration
class JSONStringIntegrationSpec extends Specification {

    void "test JSONString marshalling"() {
        /**
         * Use JsonBuilder.toString to build a JSON string.
         * Then wrap that string with a JSONString instance.
         * Then create an ArrayList containing that JSONString
         * Then use JSON converter to convert that ArrayList to JSON.
         *
         * We are testing that the JSON converter leaves the JSONString alone
         * and outputs it in its raw form (doesn't escape it).
         *
         * JSONString.ToStringJsonMarshaller is registered using Spring in
         * resources.groovy
         */
        given:
            // build an array with a JSONString in it
            def jsonObj = [new JSONString(new JsonBuilder([hello: "world"]).toString())]
            JSON jsonConverter = new JSON(jsonObj)
            StringWriter writer = new StringWriter()

        when:
            // convert the array and write it to a string buffer
            jsonConverter.render(writer)

        then:
            // verify that the JSONString in the array is output in raw form
            writer.toString() == """[{"hello":"world"}]"""
    }
}
