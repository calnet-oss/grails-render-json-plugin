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
import edu.berkeley.render.json.converters.ExtendedJSON
import grails.util.GrailsUtil
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

class RenderJsonGrailsPlugin {
    public final Log LOG = LogFactory.getLog("edu.berkeley.render.json.RenderJsonGrailsPlugin")

    def group = "edu.berkeley.calnet.grails.plugins"

    // the plugin version
    def version = "1.0.1-SNAPSHOT" // !!! Change in build.gradle too
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "2.4 > *"
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
            "grails-app/views/error.gsp",
            "grails-app/domain/**", // exclude test domain class
            "grails-app/controllers/**" // exclude test controller
    ]

    def dependsOn = [converters: GrailsUtil.getGrailsVersion()]

    def title = "Render JSON Plugin" // Headline display name of the plugin
    def author = "Brian Koehmstedt"
    def authorEmail = "bkoehmstedt@berkeley.edu"
    def description = '''\
Provides extensions to the standard JSON converter to add rendering features.
'''

    // URL to the plugin's documentation
    def documentation = "https://github.com/calnet-oss/grails-render-json-plugin/"

    // Extra (optional) plugin metadata

    // License: one of 'APACHE', 'GPL2', 'GPL3'
    def license = "BSD"

    // Details of company behind the plugin (if there is one)
    def organization = [ name: "University of California, Berkeley", url: "http://www.berkeley.edu/" ]

    // Any additional developers beyond the author specified above.
    def developers = [ [ name: "Søren Berg Glasius", email: "sbglasius@berkeley.edu" ]]

    // Location of the plugin's issue tracker.
    def issueManagement = [ system: "GitHub", url: "https://github.com/calnet-oss/grails-render-json-plugin/issues" ]

    // Online location of the plugin's browseable source code.
    def scm = [ url: "https://github.com/calnet-oss/grails-render-json-plugin" ]

    // we put the doWithSpring closure in ExtendedJSON so that we can 
    // use it in unit tests as well
    def doWithSpring = ExtendedJSON.doWithSpringRegisterMarshallersClosure

    def doWithApplicationContext = { ctx ->
        // we do this here to ensure registration happens after the Grails
        // converters plugin has finished initialization
        LOG.debug("Registering marshallers from doWithApplicationContext using $ctx")
        ctx.getBean("jsonStringMarshaller").registerMarshaller()
        ctx.getBean("domainMarshaller").registerMarshaller()
        ctx.getBean("mapMarshaller").registerMarshaller()
    }
}
