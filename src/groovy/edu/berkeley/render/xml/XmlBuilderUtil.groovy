package edu.berkeley.render.xml

import groovy.xml.MarkupBuilder

class XmlBuilderUtil {
    /**
     * Convert a map to a XML string.
     *
     * @param rootElementName The name of the root element in the XML string.
     * @param mapToConvert The map to convert to XML.

     * @param listEntryNames A map that supplies the list entry element name
     *        for a parent element that represents a list.  The key of this
     *        map is the element with the list in the mapToConvert.  The
     *        value of the map is the element name you wish to surround this
     *        list entry with in the XML.  This is best explained/shown by
     *        the example below.  If a list is encountered with no matching
     *        entry in the listEntryNames map, then the list entries will be
     *        surrounded by "PARENTTAGNAME_entry".
     *
     * @return XML as a String.
     *
     * Example mapToConvert:
     * <code>
     * [
     *     Person: [
     *         uid        : "123",
     *         identifiers: [
     *             [
     *                 identifierTypeName: "ID1",
     *                 identifierValue   : "123"
     *             ],
     *             [
     *                 identifierTypeName: "ID2",
     *                 identifierValue   : "456"
     *             ]
     *         ],
     *         names      : [
     *             [
     *                 nameType: "NT1",
     *                 name    : "John Smith"
     *             ],
     *             [
     *                 nameType: "NT2",
     *                 name    : "John Joe Smith"
     *             ]
     *         ]
     *     ]
     * ]
     * </code>
     *
     * Example listEntryNames:
     * [
     *     identifiers : "identifier"
     *     names : "name"
     * ]
     *
     * This tells the method to make each "identifiers" element tagged with
     * an "identifier" element.
     *
     * Expected XML output:
     * <pre>
     * <Person>
     *   <uid>123</uid>
     *   <identifiers>
     *     <identifier>
     *       <identifierTypeName>ID1</identifierTypeName>
     *       <identifierValue>123</identifierValue>
     *     </identifier>
     *     <identifier>
     *       <identifierTypeName>ID2</identifierTypeName>
     *       <identifierValue>456</identifierValue>
     *     </identifier>
     *   </identifiers>
     *   <names>
     *     <name>
     *       <nameType>NT1</nameType>
     *       <name>John Smith</name>
     *     </name>
     *     <name>
     *       <nameType>NT2</nameType>
     *       <name>John Joe Smith</name>
     *     </name>
     *   </names>
     * </Person>
     * </pre>
     *
     * However, if you passed a null or empty listEntryNames map, the
     * expected XML output would be:
     * <pre>
     * <Person>
     *   <uid>123</uid>
     *   <identifiers>
     *     <identifiers_entry>
     *       <identifierTypeName>ID1</identifierTypeName>
     *       <identifierValue>123</identifierValue>
     *     </identifiers_entry>
     *     <identifiers_entry>
     *       <identifierTypeName>ID2</identifierTypeName>
     *       <identifierValue>456</identifierValue>
     *     </identifiers_entry>
     *   </identifiers>
     *   <names>
     *     <names_entry>
     *       <nameType>NT1</nameType>
     *       <name>John Smith</name>
     *     </names_entry>
     *     <names_entry>
     *       <nameType>NT2</nameType>
     *       <name>John Joe Smith</name>
     *     </names_entry>
     *   </names>
     * </Person>
     * </pre>
     */
    static String mapToXmlString(String rootElementName, Map mapToConvert, Map<String, String> listEntryNames = null) {
        StringWriter sw = new StringWriter()
        new MarkupBuilder(sw).with {
            def listVisitor
            def visitor
            visitor = { k, v ->
                "$k" {
                    if (v instanceof Map) {
                        return v.collect(visitor)
                    } else if (v instanceof Collection) {
                        return v.collect { _v ->
                            String elementName = listEntryNames?.get(k) ?: "${k}_entry"
                            "$elementName" {
                                _v.collect(visitor)
                            }
                        }
                    } else {
                        return mkp.yield(v)
                    }
                }
            }
            "$rootElementName" { mapToConvert.collect(visitor) }
        }
        return sw as String
    }

    /**
     * Convert a map to a XML string.
     *
     * @param mapToConvert The map to convert.  This map must be one entry,
     *        with the key being a String and the value being another Map. 
     *        The root element name will be the key used in this
     *        single-entry passed-in map.  Use mapToXmlString(String
     *        rootElementName, Map map) instead if your map has more than
     *        one entry at the top level.
     * @param listEntryNames See the description and example for
     *        mapToXmlString(String rootElementName, Map mapToConvert, Map<String,String> listEntryNames = null).
     *
     * @return XML as a String.
     */
    static String mapToXmlString(Map mapToConvert, Map<String, String> listEntryNames = null) {
        if (mapToConvert.size() == 1 && mapToConvert.values()[0] instanceof Map) {
            Map.Entry<String, Map> onlyEntry = mapToConvert.entrySet()[0]
            return mapToXmlString(onlyEntry.key, onlyEntry.value, listEntryNames)
        } else {
            throw new IllegalArgumentException("The passed in map has more than one entry so unable to automatically determine the root element name.  Use mapToXmlString(String rootElementName, Map map) instead.")
        }
    }
}
