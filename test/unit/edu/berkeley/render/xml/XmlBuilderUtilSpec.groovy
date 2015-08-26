package edu.berkeley.render.xml

import spock.lang.Shared
import spock.lang.Specification

class XmlBuilderUtilSpec extends Specification {
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
}
