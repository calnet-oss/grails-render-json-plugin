package edu.berkeley.render.json

import edu.berkeley.render.json.converters.ExtendedJSON
import edu.berkeley.render.json.test.*
import grails.test.spock.IntegrationSpec
import org.springframework.beans.factory.annotation.Autowired

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
            String expected = """{"timeUpdated":"2015-03-26T19:40:15Z","uid":"123"}"""
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
            assert converter.excludes.contains("dummyField")
            assert converter.excludes.contains("includes")
            assert converter.excludes.contains("excludes")
            testController.render(converter)
        then:
            String expected = """{"firstName":"Søren Berg","lastName":"Glasius","uid":"123"}"""
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
            String expected = """{"lastName":"Smith","uid":"123","firstName":"John"}"""
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
            String expected = """{"lastName":"Smith","uid":"123","dummyField":"excludeMe","firstName":"John"}"""
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
            String expected = """{"lastName":"Smith","uid":"123","dummyField":"excludeMe","firstName":"John"}"""
            // verify we got expected json
            writer.toString() == expected

    }

    void "test rendering TestPerson object with no excludes or includes after calling setTarget"() {
        /**
         * We're testing the conversion works after calling setTarget on the converter with different objects
         * with different field configs for the converter.
         */
        given:
            // first an object that is excluding dummyField
            TestPerson personExcludesDummy = new TestPerson(uid: "123", firstName: "John", lastName: "Smith")
            TestPersonEmptyAnnotation personIncludesDummy = new TestPersonEmptyAnnotation(uid: "124", firstName: "John2", lastName: "Smith2")
        when:
            ExtendedJSON converter = personExcludesDummy as ExtendedJSON
            // first verify the first-go-around works as expected
            assert converter.toString() == """{"lastName":"Smith","uid":"123","firstName":"John"}"""
            // now assign a new target object to the converter.
            converter.setTarget(personIncludesDummy)

        then:
            // since the new target didn't exclude dummyField, we should now see it in the json
            converter.toString() == """{"firstName":"John2","dummyField":"excludeMe","uid":"124","lastName":"Smith2"}"""
    }

    void "test rendering TestPerson object with excludes after calling setTarget"() {
        /**
         * We're testing the conversion works after calling setTarget on the converter with different objects
         * with different field configs for the converter.
         *
         * We're reversing the order of the above test.  Now we do the one without exclusions first, then add the exclusion on the second try.
         */
        given:
            // first an object that is excluding dummyField
            TestPerson personExcludesDummy = new TestPerson(uid: "123", firstName: "John", lastName: "Smith")
            TestPersonEmptyAnnotation personIncludesDummy = new TestPersonEmptyAnnotation(uid: "124", firstName: "John2", lastName: "Smith2")
        when:
            ExtendedJSON converter = personIncludesDummy as ExtendedJSON
            // first verify the first-go-around works as expected
            assert converter.toString() == """{"firstName":"John2","dummyField":"excludeMe","uid":"124","lastName":"Smith2"}"""
            // now assign a new target object to the converter.
            converter.setTarget(personExcludesDummy)

        then:
            // since the new target now excludes dummyField, we shouldn't see it in the json
            converter.toString() == """{"lastName":"Smith","uid":"123","firstName":"John"}"""
    }

}
