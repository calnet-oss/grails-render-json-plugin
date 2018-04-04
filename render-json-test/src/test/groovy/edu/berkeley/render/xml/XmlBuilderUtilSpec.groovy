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

package edu.berkeley.render.xml

import edu.berkeley.render.json.converters.ExtendedJSON
import groovy.util.slurpersupport.NodeChild
import spock.lang.Shared
import spock.lang.Specification

class XmlBuilderUtilSpec extends Specification {
    static doWithSpring = ExtendedJSON.doWithSpringRegisterMarshallersClosure

    @Shared
    def map = [
            Person: [
                    uid        : "123",
                    identifiers: [
                            [
                                    identifierTypeName: "ID1",
                                    identifierValue   : "123"
                            ],
                            [
                                    identifierTypeName: "ID2",
                                    identifierValue   : "456"
                            ]
                    ],
                    names      : [
                            [
                                    nameType: "NT1",
                                    name    : "John Smith"
                            ],
                            [
                                    nameType: "NT2",
                                    name    : "John Joe Smith"
                            ]
                    ]
            ]
    ]

    void "test map-to-XML building with default list entry naming"() {
        given:
        String xml = XmlBuilderUtil.mapToXmlString(map)
        println(xml)

        expect:
        xml == """<Person>
  <uid>123</uid>
  <identifiers>
    <identifiers_entry>
      <identifierTypeName>ID1</identifierTypeName>
      <identifierValue>123</identifierValue>
    </identifiers_entry>
    <identifiers_entry>
      <identifierTypeName>ID2</identifierTypeName>
      <identifierValue>456</identifierValue>
    </identifiers_entry>
  </identifiers>
  <names>
    <names_entry>
      <nameType>NT1</nameType>
      <name>John Smith</name>
    </names_entry>
    <names_entry>
      <nameType>NT2</nameType>
      <name>John Joe Smith</name>
    </names_entry>
  </names>
</Person>"""
    }

    void "test map-to-XML building with overriden list entry naming"() {
        given:
        String xml = XmlBuilderUtil.mapToXmlString(map, [
                identifiers: "identifier",
                names      : "name"
        ])
        println(xml)

        expect:
        xml == """<Person>
  <uid>123</uid>
  <identifiers>
    <identifier>
      <identifierTypeName>ID1</identifierTypeName>
      <identifierValue>123</identifierValue>
    </identifier>
    <identifier>
      <identifierTypeName>ID2</identifierTypeName>
      <identifierValue>456</identifierValue>
    </identifier>
  </identifiers>
  <names>
    <name>
      <nameType>NT1</nameType>
      <name>John Smith</name>
    </name>
    <name>
      <nameType>NT2</nameType>
      <name>John Joe Smith</name>
    </name>
  </names>
</Person>"""
    }

    void "test XML-to-map marshalling"() {
        given:
        String xml = """<Person>
  <uid>123</uid>
  <identifiers>
    <identifier>
      <identifierTypeName>ID1</identifierTypeName>
      <identifierValue>123</identifierValue>
    </identifier>
    <identifier>
      <identifierTypeName>ID2</identifierTypeName>
      <identifierValue>456</identifierValue>
    </identifier>
  </identifiers>
  <names>
    <name>
      <nameType>NT1</nameType>
      <name>John Smith</name>
    </name>
    <name>
      <nameType>NT2</nameType>
      <name>John Joe Smith</name>
    </name>
  </names>
</Person>"""

        when:
        NodeChild root = (NodeChild) new XmlSlurper().parseText(xml)
        Map xmlToMap = (Map) XmlBuilderUtil.xmlSlurperToMap(root)

        then:
        map == xmlToMap
    }

    void "test XML-to-map boolean and number conversion"() {
        given:
        String xml = """<Person>
  <isActive>true</isActive>
  <isExpired>false</isExpired>
  <integer>100</integer>
  <float>100.33</float>
  <ninteger>-100</ninteger>
  <nfloat>-100.33</nfloat>
</Person>"""

        when:
        NodeChild root = (NodeChild) new XmlSlurper().parseText(xml)
        Map xmlToMap = (Map) XmlBuilderUtil.xmlSlurperToMap(
                root,
                root,
                true, // convert booleans
                true // convert numbers
        )

        then:
        xmlToMap == [
                "Person": [
                        "isActive" : true,
                        "isExpired": false,
                        "integer"  : 100L,
                        "float"    : 100.33D,
                        "ninteger" : -100L,
                        "nfloat"   : -100.33D
                ]
        ]
    }
}
