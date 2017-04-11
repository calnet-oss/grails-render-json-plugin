package edu.berkeley.render

import edu.berkeley.render.json.converters.ExtendedJSON
import grails.plugins.*
import grails.util.GrailsUtil
import groovy.util.logging.Slf4j

@Slf4j
class GrailsPlugin extends Plugin {

    def grailsVersion = "3.0.0 > *"
    def dependsOn = [converters: GrailsUtil.getGrailsVersion()]

    def title = "Render JSON Plugin"
    def author = "Brian Koehmstedt"
    def authorEmail = "bkoehmstedt@berkeley.edu"
    def description = '''Provides extensions to the standard JSON converter to add rendering features.'''

    // URL to the plugin's documentation
    def documentation = "https://github.com/calnet-oss/grails-render-json-plugin/"

    // License: one of 'APACHE', 'GPL2', 'GPL3'
    def license = "BSD"

    // Details of company behind the plugin (if there is one)
    def organization = [name: "University of California, Berkeley", url: "http://www.berkeley.edu/"]

    def developers = [ [ name: "Søren Berg Glasius", email: "sbglasius@berkeley.edu" ]]

    def issueManagement = [system: "GitHub", url: "https://github.com/calnet-oss/grails-render-json-plugin/issues"]

    def scm = [url: "https://github.com/calnet-oss/grails-render-json-plugin"]

    Closure doWithSpring() {
        ExtendedJSON.doWithSpringRegisterMarshallersClosure
    }

    void doWithApplicationContext() {
        log.debug("Registering marshallers from doWithApplicationContext using $ctx")
        applicationContext.getBean("jsonStringMarshaller").registerMarshaller()
        applicationContext.getBean("domainMarshaller").registerMarshaller()
        applicationContext.getBean("mapMarshaller").registerMarshaller()
    }

}
