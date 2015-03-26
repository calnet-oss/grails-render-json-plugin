package edu.berkeley.render.json

import grails.converters.JSON
import grails.test.spock.IntegrationSpec
import groovy.json.JsonBuilder

class JSONStringIntegrationSpec extends IntegrationSpec {

    def setup() {
    }

    def cleanup() {
    }

    void "test JSONString marshalling"() {
        /**
         * Use JsonBuilder.toString to build a JSON string.
         * Then wrap that string with a JSONString instance.
         * Then create an ArrayList containing that JSONString
         * Then use JSON converter to convert that ArrayList to JSON.
         *
         * We are testing that the JSON converter leaves the JSONString alone
         * and outputs it in its raw form (doesn't escape it).
         *
         * JSONString.ToStringJsonMarshaller is registered using Spring in
         * resources.groovy
         */
        given:
            // build an array with a JSONString in it
            def jsonObj = [new JSONString(new JsonBuilder([hello: "world"]).toString())]
            JSON jsonConverter = new JSON(jsonObj)
            StringWriter writer = new StringWriter()

        when:
            // convert the array and write it to a string buffer
            jsonConverter.render(writer)

        then:
            // verify that the JSONString in the array is output in raw form
            writer.toString() == """[{"hello":"world"}]"""
    }
}
