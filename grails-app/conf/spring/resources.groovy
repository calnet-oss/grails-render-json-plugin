import edu.berkeley.render.json.JSONString
import edu.berkeley.render.json.marshallers.DomainJsonMarshaller
import edu.berkeley.render.json.marshallers.MapJsonMarshaller

beans = {
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
