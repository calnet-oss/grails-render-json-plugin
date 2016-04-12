import edu.berkeley.render.json.JSONString
import edu.berkeley.render.json.marshallers.DomainJsonMarshaller
import edu.berkeley.render.json.marshallers.MapJsonMarshaller
import grails.util.GrailsUtil
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

class RenderJsonGrailsPlugin {
    public final Log LOG = LogFactory.getLog("edu.berkeley.render.json.RenderJsonGrailsPlugin")

    def group = "edu.berkeley.calnet.plugins"

    // the plugin version
    def version = "0.9-SNAPSHOT"
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
    //def documentation = "http://grails.org/plugin/grails-render-json"

    // Extra (optional) plugin metadata

    // License: one of 'APACHE', 'GPL2', 'GPL3'
//    def license = "APACHE"

    // Details of company behind the plugin (if there is one)
//    def organization = [ name: "My Company", url: "http://www.my-company.com/" ]

    // Any additional developers beyond the author specified above.
//    def developers = [ [ name: "Joe Bloggs", email: "joe@bloggs.net" ]]

    // Location of the plugin's issue tracker.
//    def issueManagement = [ system: "JIRA", url: "http://jira.grails.org/browse/GPMYPLUGIN" ]

    // Online location of the plugin's browseable source code.
//    def scm = [ url: "http://svn.codehaus.org/grails-plugins/" ]

    def doWithWebDescriptor = { xml ->
        // TODO Implement additions to web.xml (optional), this event occurs before
    }

    def doWithSpring = {
        // Register a custom marshaller for JSONString instances
        jsonStringMarshaller(JSONString.ToStringJsonMarshaller)

        // Register a custom marshaller for Domain class instances that will
        // marshal a domain instance as a Map, at which point the
        // MapJsonMarshaller takes over.
        domainMarshaller(DomainJsonMarshaller)

        // Register a custom marshaller for Map instances that will exclude
        // rendering entries with null values
        mapMarshaller(MapJsonMarshaller)
    }

    def doWithDynamicMethods = { ctx ->
        // TODO Implement registering dynamic methods to classes (optional)
    }

    def doWithApplicationContext = { ctx ->
        // we do this here to ensure registration happens after the Grails
        // converters plugin has finished initialization
        LOG.debug("Registering marshallers from doWithApplicationContext using $ctx")
        ctx.getBean("jsonStringMarshaller").registerMarshaller()
        ctx.getBean("domainMarshaller").registerMarshaller()
        ctx.getBean("mapMarshaller").registerMarshaller()
    }

    def onChange = { event ->
        // TODO Implement code that is executed when any artefact that this plugin is
        // watching is modified and reloaded. The event contains: event.source,
        // event.application, event.manager, event.ctx, and event.plugin.
    }

    def onConfigChange = { event ->
        // TODO Implement code that is executed when the project configuration changes.
        // The event is the same as for 'onChange'.
    }

    def onShutdown = { event ->
        // TODO Implement code that is executed when the application shuts down (optional)
    }
}
