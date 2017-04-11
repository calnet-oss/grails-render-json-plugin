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

package edu.berkeley.render.xml

import groovy.util.slurpersupport.NodeChild
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

    /**
     * Convert an XML document from XmlSlurper to a Map (which could further
     * converted into JSON).  Because XML inherently contains more data than
     * just name/value pairs (i.e., attributes), this is a rather crude and
     * simplistic approach.  Any attributes in XML tags are disregarded.
     * <p/>
     * The defaults are:
     * <ul>
     *     <li>Convert boolean strings into Boolean objects</li>
     *     <li>Don't convert number strings into Number objects</li>
     * </ul>
     *
     * @param node The node being converted.
     *
     * @return An Object that is either a Map, List or String.  The root XML
     * node being passed in is typically representative of a map, so the the
     * return value will typically be a Map.
     */
    static Object xmlSlurperToMap(NodeChild node) {
        return xmlSlurperToMap(
                node,
                node,
                true, // detect booleans by default
                false // don't detect number strings by default
        )
    }

    /**
     * Convert an XML document from XmlSlurper to a Map (which could further
     * converted into JSON).  Because XML inherently contains more data than
     * just name/value pairs (i.e., attributes), this is a rather crude and
     * simplistic approach.  Any attributes in XML tags are disregarded.
     *
     * @param root Because of a Groovy NodeChild.parent() bug, pass in the
     *        root node that matches the initial node being converted.
     * @param node The node being converted.
     * @param detectBooleans Pass true to convert boolean strings into
     *        Boolean objects in resulting map.
     * @param detectNumbers Pass true to convert number strings into
     *        Number objects in resulting map.
     *
     * @return An Object that is either a Map, List or String.  The root XML
     * node being passed in is typically representative of a map, so the the
     * return value will typically be a Map.
     */
    static Object xmlSlurperToMap(NodeChild root, NodeChild node, boolean detectBooleans, boolean detectNumbers) {
        // Find how many unique child tag names there are so we can
        // determine if we need to treat this node as a map, list or text
        // node.
        def childNodeNameMap = [:]
        node.children().each { NodeChild child ->
            childNodeNameMap[child.name()] = true
        }

        Object objectValue = null
        if (childNodeNameMap.size() > 1) {
            // there are multiple child tag names, so treat children as a map
            def map = node.children().collectEntries() { NodeChild child ->
                [child.name(), xmlSlurperToMap(root, child, detectBooleans, detectNumbers)]
            }
            // get rid of the map name if there's only one map entry
            objectValue = map.size() == 1 ? map.values().first() : map
        } else if (childNodeNameMap.size() == 1 && node.children().size() >= 1) {
            // there is only one unique child tag name, so treat children as
            // a map
            objectValue = node.children().collect { NodeChild child ->
                xmlSlurperToMap(root, child, detectBooleans, detectNumbers)
            }
        } else {
            // not a map nor a list -- must be a text node
            String text = node.localText()?.join()?.trim()
            if (detectBooleans && text?.toLowerCase() in ["true", "false"]) {
                objectValue = Boolean.valueOf(text)
            } else if (text) {
                if (detectNumbers) {
                    // number determination: not that efficient.  faster with a
                    // regexp?
                    try {
                        Double dbl = Double.valueOf(text)
                        // Didn't throw an exception, so it's at least a double.
                        // Can we treat it as a long integer?
                        if (dbl.doubleValue() == dbl.longValue()) {
                            // yes, it's an integer
                            objectValue = dbl.longValue()
                        } else {
                            // not an integer
                            objectValue = dbl
                        }
                    }
                    catch (ignored) {
                        // not a number
                        objectValue = text.toString()
                    }
                } else {
                    // no number detection
                    objectValue = text.toString()
                }
            } else {
                objectValue = ""
            }
        }

        // For the root, we want to add the root node in the map.  In Groovy
        // used with Grails 2.5.4, NodeChild.parent() seems buggy.  That is
        // why we are passing in the root node as a parameter rather than
        // using parent().
        if (node == root && node.name()) {
            return [(node.name().toString()): objectValue] as Map
        } else {
            return objectValue
        }
    }
}
