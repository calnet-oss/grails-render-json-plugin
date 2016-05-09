package edu.berkeley.render.json

import edu.berkeley.render.json.converters.ExtendedJSON
import edu.berkeley.render.json.marshallers.DomainJsonMarshaller
import edu.berkeley.render.json.marshallers.MapJsonMarshaller
import grails.test.mixin.TestMixin
import grails.test.mixin.domain.DomainClassUnitTestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import grails.test.mixin.web.ControllerUnitTestMixin
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject

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
    }
}
