package io.codearte.accurest.util

import groovy.json.JsonSlurper
import spock.lang.Specification

import static JsonPathAssertion.assertThat

/**
 * @author Marcin Grzejszczak
 */
public class JsonPathAssertionSpec extends Specification {

	def 'should work'() {
		expect:
			assertThat('body').field('foo').isEqualTo('bar')
			assertThat('body').field('foo').array('nested').contains('withlist').isEqualTo('bar')
	}

	def 'should convert a json with a map as root to a map of path to value'() {
		given:
		String json = '''
					 {
							"some" : {
								"nested" : {
									"json" : "with \\"val'ue",
									"anothervalue": 4,
									"withlist" : [
										{ "name" :"name1"} , {"name": "name2"}
									]
								}
							}
						}
'''
		when:
			JsonPaths pathAndValues = JsonToJsonPathsConverter.transformToJsonPathWithTestsSideValues(new JsonSlurper().parseText(json))
		then:
			pathAndValues.find {
				it.methodsBuffer.toString() == """.field("some").field("nested").field("anothervalue").isEqualTo(4)"""
			}
			pathAndValues.find {
				it.jsonPathBuffer.toString() == """\$.some.nested[?(@.anothervalue == 4)]"""
			}
			pathAndValues.find {
				it.methodsBuffer.toString() == """.field("some").field("nested").array("withlist").contains("name").isEqualTo("name1")"""
			}
			pathAndValues.find {
				it.jsonPathBuffer.toString() == """\$.some.nested.withlist[*][?(@.name == 'name1')]"""
			}
			pathAndValues.find {
				it.methodsBuffer.toString() == """.field("some").field("nested").array("withlist").contains("name").isEqualTo("name2")"""
			}
			pathAndValues.find {
				it.jsonPathBuffer.toString() == """\$.some.nested.withlist[*][?(@.name == 'name2')]"""
			}
			pathAndValues.find {
				it.methodsBuffer.toString() == """.field("some").field("nested").field("json").isEqualTo("with \\"val'ue")"""
			}
			pathAndValues.find {
				it.jsonPathBuffer.toString() == """\$.some.nested[?(@.json == 'with "val\\'ue')]"""
			}
		and:
			pathAndValues.each {
				it.check()
			}
	}

}