## Configuration

In `grails-app/conf/spring/resources.groovy`:
```
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
    // rendering entries with null values and pay attention to
    // @ConverterConfig annotations on the domain class.
    mapMarshaller(MapJsonMarshaller)
}
```

## To Use

Once configured, you use `ExtendedJSON` like you would use `JSON`.

In a controller:
```
import edu.berkeley.render.json.converters.ExtendedJSON
...
render domainObject as ExtendedJSON
```

You can also do this and it doesn't have to be in a controller:
```
String marshalledJson = (domainObject as ExtendedJSON).toString()
```

## Including/Excluding Domain Class Fields

You can use the `@ConverterConfig` annotation on the domain class to tell
the converter which domain class properties to marshal or not marshal.

See the `@ConverterConfig` section in the README.md at
https://github.com/ucidentity/grails-domain-util.
