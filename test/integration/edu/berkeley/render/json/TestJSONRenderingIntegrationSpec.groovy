package edu.berkeley.render.json

import edu.berkeley.render.json.converters.ExtendedJSON
import edu.berkeley.render.json.test.*
import grails.test.spock.IntegrationSpec
import org.springframework.beans.factory.annotation.Autowired
import edu.berkeley.render.json.marshallers.MapJsonMarshaller

import java.nio.charset.Charset

class TestJSONRenderingIntegrationSpec extends IntegrationSpec {

    @Autowired
    TestController testController

    StringWriter writer

    def setup() {
        writer = new StringWriter()
        testController.response.writer.out = writer
    }

    def cleanup() {
    }

    void "test rendering TestPerson object"() {
        /**
         * We're testing the following things here about rendering:
         *
         * - The custom JSON marshallers are excluding null values in the
         *   JSON output.
         * - The custom converter is adding Content-Length header
         * - The custom converter is adding Last-Modified header
         */
        given:
            // 1427398815895 = "2015-03-26T19:40:15Z"
            TestPerson person = new TestPerson(uid: "123", timeUpdated: new Date(1427398815895L))
        when:
            testController.render((person as ExtendedJSON).setLastModified(person.timeUpdated))
        then:
            String expected = '''{"timeUpdated":"2015-03-26T19:40:15Z","uid":"123"}'''
            // verify we got expected json
            writer.toString() == expected
            // verify the Content-Length header was set correctly
            Integer.valueOf(testController.response.getHeader("Content-Length")) == expected.length()
            // verify the Last-Modified header was set correctly
            Long.valueOf(testController.response.getHeader("Last-Modified")) == person.timeUpdated.getTime()
    }

    void "test rendering TestPerson object with UTF-8 characters and excludes"() {
        /**
         * We're testing the proper Content-Length for JSON with UTF-8.
         */
        given:
            // 1427398815895 = "2015-03-26T19:40:15Z"
            TestPerson person = new TestPerson(uid: "123", firstName: "Søren Berg", lastName: "Glasius")
        when:
            assert person.excludes == ["dummyField"]
            ExtendedJSON converter = person as ExtendedJSON
            converter.setLastModified(person.timeUpdated)
            def marshaller = converter.lookupObjectMarshaller(person.properties)
            assert marshaller instanceof MapJsonMarshaller
            testController.render(converter)
        then:
            String expected = '''{"firstName":"Søren Berg","lastName":"Glasius","uid":"123"}'''
            // verify we got expected json
            writer.toString() == expected
            // verify the Content-Length header was set correctly
            Integer.valueOf(testController.response.getHeader("Content-Length")) == expected.getBytes(Charset.forName("UTF-8")).length
    }

    void "test rendering TestPerson object with includes"() {
        /**
         * We're testing the conversion works with ConverterConfig annotation using includes parameter.
         */
        given:
            TestPersonIncludesAnnotation person = new TestPersonIncludesAnnotation(uid: "123", firstName: "John", lastName: "Smith")
        when:
            ExtendedJSON converter = person as ExtendedJSON
            testController.render(converter)
        then:
            String expected = '''{"firstName":"John","lastName":"Smith","uid":"123"}'''
            // verify we got expected json
            writer.toString() == expected
    }

    void "test rendering TestPersonNoAnnotation object"() {
        /**
         * We're testing the conversion works with no ConverterConfig annotation..
         */
        given:
            // 1427398815895 = "2015-03-26T19:40:15Z"
            TestPersonNoAnnotation person = new TestPersonNoAnnotation(uid: "123", firstName: "John", lastName: "Smith")
        when:
            ExtendedJSON converter = person as ExtendedJSON
            testController.render(converter)
        then:
            String expected = '''{"dummyField":"excludeMe","firstName":"John","lastName":"Smith","uid":"123"}'''
            // verify we got expected json
            writer.toString() == expected

    }

    void "test rendering TestPersonEmptyAnnotation object"() {
        /**
         * We're testing the conversion works with an emoty ConverterConfig annotation..
         */
        given:
            // 1427398815895 = "2015-03-26T19:40:15Z"
            TestPersonEmptyAnnotation person = new TestPersonEmptyAnnotation(uid: "123", firstName: "John", lastName: "Smith")
        when:
            ExtendedJSON converter = person as ExtendedJSON
            testController.render(converter)
        then:
            String expected = '''{"dummyField":"excludeMe","firstName":"John","lastName":"Smith","uid":"123"}'''
            // verify we got expected json
            writer.toString() == expected

    }

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