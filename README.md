## Configuration

In `grails-app/conf/BuildConfig.groovy`:
```
    plugins {
        ...
        compile "edu.berkeley.calnet.plugins:render-json:VERSION"
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

## Additional Notes

This plugin registers the following marshallers that the normal JSON
converter may also utilize if marshalling a domain class or a map.

* `JSONString.ToStringJsonMarshaller`
  * Allow already-marshalled JSONStrings to be included in map values as-is.
* `DomainJsonMarshaller`
  * Marshall domain objects as a regular map (by using `MapJsonMarshaller`)
* `MapJsonMarshaller`
  * Marshalls regular maps
    * This is where the main logic is that strips out null values and checks
      for field inclusions or exclusions.

See `RenderJsonGrailsPlugin` `doWithSpring` and `doWithApplicationContext`
to see how this marshallers are automatically registered when the plugin is
used.
