package edu.berkeley.render.xml

import spock.lang.Specification

class XmlBuilderUtilSpec extends Specification {
    void "test map-to-XML building"() {
        given:
            def map = [
                    Person: [
                            uid : "123",
                            name: "Joe Smith"
                    ]
            ]
            String xml = XmlBuilderUtil.mapToXmlString(map)
            println(xml)

        expect:
            xml == """<Person>
  <uid>123</uid>
  <name>Joe Smith</name>
</Person>"""
    }
}
