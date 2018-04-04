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

package integration.edu.berkeley.render.json

import com.budjb.httprequests.HttpClient
import com.budjb.httprequests.HttpClientFactory
import com.budjb.httprequests.HttpRequest
import grails.testing.mixin.integration.Integration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.http.HttpStatus
import spock.lang.Specification
import spock.lang.Unroll

@Integration
class TestJSONRenderingFunctionalSpec extends Specification {

    private static String RFC_1123_DATE_TIME = "EEE, dd MMM yyyy HH:mm:ss zzz"

    @Autowired
    ApplicationContext applicationContext

    @Autowired
    HttpClientFactory httpClientFactory

    void "validate beans"() {
        expect:
        applicationContext.getBean('jsonStringMarshaller')
        applicationContext.getBean('domainMarshaller')
        applicationContext.getBean('mapMarshaller')
    }

    /**
     * We're testing the following things here about rendering:
     *
     * - The custom JSON marshallers are excluding null values in the
     *   JSON output.
     * - The custom converter is adding Content-Length header
     * - The custom converter is adding Last-Modified header
     */
    @Unroll
    void "test rendering TestPerson object"() {
        when:
        def response = httpClient.get(new HttpRequest("http://localhost:${serverPort}/test/renderTestPerson").addQueryParameters([uid: '123']+params))
        then:
        with(response) {
            status == HttpStatus.OK.value()
            getEntity(String) == expected
            charset == 'UTF-8'
            // Using bytes size, because 'Søren' is a UTF-8 string and is 6 bytes long.
            getHeader('Content-Length').toInteger() == expected.bytes.size()
            if (params.timeUpdated) {
                with(Date.parse(RFC_1123_DATE_TIME, response.getHeader('Last-Modified'))) {
                    time == params.timeUpdated
                }
            }
        }

        where:
        params                                         | expected
        [timeUpdated: 1427398815000L]                  | """{"timeUpdated":"2015-03-26T19:40:15Z","uid":"123"}"""
        [firstName: 'Søren Berg', lastName: 'Glasius'] | '''{"firstName":"Søren Berg","lastName":"Glasius","uid":"123"}'''

        urlParams = "uid=123&${params.collect { "$it.key=${URLEncoder.encode(it.value.toString(), 'UTF-8').replace('+',' ')}" }.join('&')}"
    }

    /**
     * We're testing the conversion works with ConverterConfig annotation using includes parameter.
     */
    void "test rendering TestPerson object with includes"() {
        when:
        def response = httpClient.get {
            // Here we use the serverPort variable.
            uri = "http://localhost:${serverPort}/test/renderTestPersonIncludesAnnotation"
            accept = 'application/json'
        }

        then:

        with(response) {
            status == 200
            getEntity(String) == '''{"firstName":"John","lastName":"Smith","uid":"123"}'''
        }
    }

    /**
     * We're testing the conversion works with no ConverterConfig annotation..
     */
    void "test rendering TestPersonNoAnnotation object"() {

        when:
        def response = httpClient.get {
            // Here we use the serverPort variable.
            uri = "http://localhost:${serverPort}/test/renderTestPersonNoAnnotation"
            accept = 'application/json'
        }

        then:

        with(response) {
            status == 200
            getEntity(String) == '''{"dummyField":"excludeMe","firstName":"John","lastName":"Smith","uid":"123"}'''
        }
    }

    /**
     * We're testing the conversion works with an emoty ConverterConfig annotation..
     */
    void "test rendering TestPersonEmptyAnnotation object"() {
        when:
        def response = httpClient.get {
            // Here we use the serverPort variable.
            uri = "http://localhost:${serverPort}/test/renderTestPersonEmptyAnnotation"
            accept = 'application/json'
        }

        then:

        with(response) {
            status == 200
            getEntity(String) == '''{"dummyField":"excludeMe","firstName":"John","lastName":"Smith","uid":"123"}'''
        }
    }

    /**
     * We're testing that nulls are included when @ConverterConfig includeNulls=true is used.
     */
    void "test rendering TestPerson object with nulls included"() {
        when:
        def response = httpClient.get {
            // Here we use the serverPort variable.
            uri = "http://localhost:${serverPort}/test/renderTestPersonIncludesNulls"
            accept = 'application/json'
        }

        then:

        with(response) {
            status == 200
            getEntity(String) == '''{"dateOfBirth":null,"emailAddress":null,"firstName":"John","lastName":"Smith","timeCreated":null,"timeUpdated":null,"uid":"123"}'''
        }
    }

    HttpClient getHttpClient() {
        httpClientFactory.createHttpClient()
    }
}
