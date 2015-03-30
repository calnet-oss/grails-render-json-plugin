package edu.berkeley.render.json

import edu.berkeley.render.json.converters.ExtendedJSON
import edu.berkeley.render.json.test.TestController
import edu.berkeley.render.json.test.TestPerson
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

    void "test rendering TestPerson object with UTF-8 characters"() {
        /**
         * We're testing the proper Content-Length for JSON with UTF-8.
         */
        given:
            // 1427398815895 = "2015-03-26T19:40:15Z"
            TestPerson person = new TestPerson(uid: "123", firstName: "Søren Berg", lastName: "Glasius")
        when:
            testController.render((person as ExtendedJSON).setLastModified(person.timeUpdated))
        then:
            String expected = """{"firstName":"Søren Berg","lastName":"Glasius","uid":"123"}"""
            // verify we got expected json
            writer.toString() == expected
            // verify the Content-Length header was set correctly
            Integer.valueOf(testController.response.getHeader("Content-Length")) == expected.getBytes(Charset.forName("UTF-8")).length
    }
}
