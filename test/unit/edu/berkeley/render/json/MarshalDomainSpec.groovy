package edu.berkeley.render.json

import edu.berkeley.render.json.converters.ExtendedJSON
import edu.berkeley.render.json.marshallers.DomainJsonMarshaller
import edu.berkeley.render.json.marshallers.MapJsonMarshaller
import grails.test.mixin.TestMixin
import grails.test.mixin.domain.DomainClassUnitTestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import grails.test.mixin.web.ControllerUnitTestMixin
import grails.test.runtime.FreshRuntime

@FreshRuntime
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
}
