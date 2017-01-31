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
import grails.test.mixin.integration.Integration
import spock.lang.Specification

@Integration
class TestJSONRenderingIntegrationSpec extends Specification {

    void "test rendering unsorted maps"() {
        given:
        // make two maps that are the same except for the ordering of the entry set iteration
        Map map1 = [a: null, b: "123", c: "456", d: "789", e: "abc", f: "def", g: "ghi", h: null] as LinkedHashMap
        // reverse the LinkedHashMap entry order (LinkedHashMap order is order that entries were added)
        Map map2 = [:] as LinkedHashMap
        new ArrayList(map1.entrySet()).reverse().each { map2.put(it.key, it.value) }
        String map1Json = (map1 as ExtendedJSON).toString()
        String map2Json = (map2 as ExtendedJSON).toString()

        expect:
        // two maps should be ordered differently
        map1.toString() != map2.toString()
        // but the JSON should be the same
        map1Json == map2Json

    }

}
