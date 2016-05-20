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
}
