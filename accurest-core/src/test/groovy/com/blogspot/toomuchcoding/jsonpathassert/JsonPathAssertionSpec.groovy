package com.blogspot.toomuchcoding.jsonpathassert

import groovy.json.JsonSlurper
import io.codearte.accurest.util.JsonPaths
import io.codearte.accurest.util.JsonToJsonPathsConverter
import spock.lang.Specification

import java.util.regex.Pattern

import static com.blogspot.toomuchcoding.jsonpathassert.JsonPathAssertion.assertThat
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
				it.methodsBuffer.toString() == """.field("some").field("nested").field("anothervalue").isEqualTo(4)""" &&
				it.jsonPathBuffer.toString() == """\$.some.nested[?(@.anothervalue == 4)]"""
			}
			pathAndValues.find {
				it.methodsBuffer.toString() == """.field("some").field("nested").array("withlist").contains("name").isEqualTo("name1")""" &&
				it.jsonPathBuffer.toString() == """\$.some.nested.withlist[*][?(@.name == 'name1')]"""
			}
			pathAndValues.find {
				it.methodsBuffer.toString() == """.field("some").field("nested").array("withlist").contains("name").isEqualTo("name2")""" &&
				it.jsonPathBuffer.toString() == """\$.some.nested.withlist[*][?(@.name == 'name2')]"""
			}
			pathAndValues.find {
				it.methodsBuffer.toString() == """.field("some").field("nested").field("json").isEqualTo("with \\"val'ue")""" &&
				it.jsonPathBuffer.toString() == """\$.some.nested[?(@.json == 'with "val\\'ue')]"""
			}
		and:
			pathAndValues.size() == 4
		and:
			pathAndValues.each {
				it.check()
			}
	}

	def "should generate assertions for simple response body"() {
		given:
			String json =  """{
		"property1": "a",
		"property2": "b"
	}"""
		when:
			JsonPaths pathAndValues = JsonToJsonPathsConverter.transformToJsonPathWithTestsSideValues(new JsonSlurper().parseText(json))
		then:
			pathAndValues.find {
				it.methodsBuffer.toString() == """.field("property1").isEqualTo("a")""" &&
				it.jsonPathBuffer.toString() == """\$[?(@.property1 == 'a')]"""
			}
			pathAndValues.find {
				it.methodsBuffer.toString() == """.field("property2").isEqualTo("b")""" &&
				it.jsonPathBuffer.toString() == """\$[?(@.property2 == 'b')]"""
			}
		and:
			pathAndValues.size() == 2
		and:
			pathAndValues.each {
				it.check()
			}
	}

	def "should generate assertions for null and boolean values"() {
		given:
			String json =  """{
		"property1": "true",
		"property2": null,
		"property3": false
	}"""
		when:
			JsonPaths pathAndValues = JsonToJsonPathsConverter.transformToJsonPathWithTestsSideValues(new JsonSlurper().parseText(json))
		then:
			pathAndValues.find {
				it.methodsBuffer.toString() == """.field("property1").isEqualTo("true")""" &&
				it.jsonPathBuffer.toString() == """\$[?(@.property1 == 'true')]"""
			}
			pathAndValues.find {
				it.methodsBuffer.toString() == """.field("property2").isNull()""" &&
				it.jsonPathBuffer.toString() == """\$[?(@.property2 == null)]"""
			}
			pathAndValues.find {
				it.methodsBuffer.toString() == """.field("property3").isEqualTo(false)""" &&
				it.jsonPathBuffer.toString() == """\$[?(@.property3 == false)]"""
			}
		and:
			pathAndValues.size() == 3
		and:
			pathAndValues.each {
				it.check()
			}
	}

	def "should generate assertions for simple response body constructed from map with a list"() {
		given:
			Map json =  [
					property1: 'a',
					property2: [
							 [a: 'sth'],
							 [b: 'sthElse']
					 ]
			]
		when:
			JsonPaths pathAndValues = JsonToJsonPathsConverter.transformToJsonPathWithTestsSideValues(json)
		then:
			pathAndValues.find {
				it.methodsBuffer.toString() == """.field("property1").isEqualTo("a")""" &&
				it.jsonPathBuffer.toString() == """\$[?(@.property1 == 'a')]"""
			}
			pathAndValues.find {
				it.methodsBuffer.toString() == """.array("property2").contains("a").isEqualTo("sth")""" &&
				it.jsonPathBuffer.toString() == """\$.property2[*][?(@.a == 'sth')]"""
			}
			pathAndValues.find {
				it.methodsBuffer.toString() == """.array("property2").contains("b").isEqualTo("sthElse")""" &&
				it.jsonPathBuffer.toString() == """\$.property2[*][?(@.b == 'sthElse')]"""
			}
		and:
			pathAndValues.size() == 3
		and:
			pathAndValues.each {
				it.check()
			}
	}

	def "should generate assertions for a response body containing map with integers as keys"() {
		given:
			Map json =  [
				property: [
					14: 0.0,
					7 : 0.0
				]
			]
		when:
			JsonPaths pathAndValues = JsonToJsonPathsConverter.transformToJsonPathWithTestsSideValues(json)
		then:
			pathAndValues.find {
				it.methodsBuffer.toString() == """.field("property").field(7).isEqualTo(0.0)""" &&
				it.jsonPathBuffer.toString() == """\$.property[?(@.7 == 0.0)]"""
			}
			pathAndValues.find {
				it.methodsBuffer.toString() == """.field("property").field(14).isEqualTo(0.0)""" &&
				it.jsonPathBuffer.toString() == """\$.property[?(@.14 == 0.0)]"""
			}
		and:
			pathAndValues.size() == 2
		and:
			pathAndValues.each {
				it.check()
			}
	}

	def "should generate assertions for array in response body"() {
		given:
			String json =  """[
	{
		"property1": "a"
	},
	{
		"property2": "b"
	}]"""
		when:
			JsonPaths pathAndValues = JsonToJsonPathsConverter.transformToJsonPathWithTestsSideValues(new JsonSlurper().parseText(json))
		then:
			pathAndValues.find {
				it.methodsBuffer.toString() == """.array().contains("property1").isEqualTo("a")""" &&
				it.jsonPathBuffer.toString() == """\$[*][?(@.property1 == 'a')]"""
			}
			pathAndValues.find {
				it.methodsBuffer.toString() == """.array().contains("property2").isEqualTo("b")""" &&
				it.jsonPathBuffer.toString() == """\$[*][?(@.property2 == 'b')]"""
			}
		and:
			pathAndValues.size() == 2
		and:
			pathAndValues.each {
				it.check()
			}
	}

	def "should generate assertions for array inside response body element"() {
		given:
			String json =  """{
	"property1": [
	{ "property2": "test1"},
	{ "property3": "test2"}
	]
}"""
		when:
			JsonPaths pathAndValues = JsonToJsonPathsConverter.transformToJsonPathWithTestsSideValues(new JsonSlurper().parseText(json))
		then:
			pathAndValues.find {
				it.methodsBuffer.toString() == """.array("property1").contains("property2").isEqualTo("test1")""" &&
				it.jsonPathBuffer.toString() == """\$.property1[*][?(@.property2 == 'test1')]"""
			}
			pathAndValues.find {
				it.methodsBuffer.toString() == """.array("property1").contains("property3").isEqualTo("test2")""" &&
				it.jsonPathBuffer.toString() == """\$.property1[*][?(@.property3 == 'test2')]"""
			}
		and:
			pathAndValues.size() == 2
		and:
			pathAndValues.each {
				it.check()
			}
	}

	def "should generate assertions for nested objects in response body"() {
		given:
			String json =  """{
		"property1": "a",
		"property2": {"property3": "b"}
	}"""
		when:
			JsonPaths pathAndValues = JsonToJsonPathsConverter.transformToJsonPathWithTestsSideValues(new JsonSlurper().parseText(json))
		then:
			pathAndValues.find {
				it.methodsBuffer.toString() == """.field("property2").field("property3").isEqualTo("b")""" &&
				it.jsonPathBuffer.toString() == """\$.property2[?(@.property3 == 'b')]"""
			}
			pathAndValues.find {
				it.methodsBuffer.toString() == """.field("property1").isEqualTo("a")""" &&
				it.jsonPathBuffer.toString() == """\$[?(@.property1 == 'a')]"""
			}
		and:
			pathAndValues.size() == 2
		and:
			pathAndValues.each {
				it.check()
			}
	}

	def "should generate regex assertions for map objects in response body"() {
		given:
			Map json =  [
					property1: "a",
					property2: Pattern.compile('[0-9]{3}')
			]
		when:
			JsonPaths pathAndValues = JsonToJsonPathsConverter.transformToJsonPathWithTestsSideValues(json)
		then:
			pathAndValues.find {
				it.methodsBuffer.toString() == """.field("property2").matches("[0-9]{3}")""" &&
				it.jsonPathBuffer.toString() == """\$[?(@.property2 =~ /[0-9]{3}/)]"""
			}
			pathAndValues.find {
				it.methodsBuffer.toString() == """.field("property1").isEqualTo("a")""" &&
				it.jsonPathBuffer.toString() == """\$[?(@.property1 == 'a')]"""
			}
		and:
			pathAndValues.size() == 2
	}
	
	def "should generate escaped regex assertions for string objects in response body"() {
		given:
			Map json =  [
					property2: Pattern.compile('\\d+')
			]
		when:
			JsonPaths pathAndValues = JsonToJsonPathsConverter.transformToJsonPathWithTestsSideValues(json)
		then:
			pathAndValues.find {
				it.methodsBuffer.toString() == """.field("property2").matches("\\d+")""" &&
				it.jsonPathBuffer.toString() == """\$[?(@.property2 =~ /\\d+/)]"""
			}
		and:
			pathAndValues.size() == 1
	}
	
	
	def "should work with more complex stuff and jsonpaths"() {
		given:
		Map json =  [
			errors: [
				[property: "bank_account_number",
				 message: "incorrect_format"]
			]
		]
		when:
		JsonPaths pathAndValues = JsonToJsonPathsConverter.transformToJsonPathWithTestsSideValues(json)
		then:
		pathAndValues.find {
			it.methodsBuffer.toString() == """.array("errors").contains("property").isEqualTo("bank_account_number")""" &&
			it.jsonPathBuffer.toString() == """\$.errors[*][?(@.property == 'bank_account_number')]"""
		}
		pathAndValues.find {
			it.methodsBuffer.toString() == """.array("errors").contains("message").isEqualTo("incorrect_format")""" &&
			it.jsonPathBuffer.toString() == """\$.errors[*][?(@.message == 'incorrect_format')]"""
		}
		and:
			pathAndValues.size() == 2
		and:
			pathAndValues.each {
				it.check()
			}
	}

	def "should manage to parse a double array"() {
		given:
			String json = '''
						[{
							"place":
							{
								"bounding_box":
								{
									"coordinates":
										[[
											[-77.119759,38.995548],
											[-76.909393,38.791645]
										]]
								}
							}
						}]
					'''
		when:
			JsonPaths pathAndValues = JsonToJsonPathsConverter.transformToJsonPathWithTestsSideValues(new JsonSlurper().parseText(json))
		then:
			pathAndValues.find {
				it.methodsBuffer.toString() == """.array().field("place").field("bounding_box").array("coordinates").array().contains(38.995548).value()""" &&
				it.jsonPathBuffer.toString() == """\$[*].place.bounding_box.coordinates[*][*][?(@ == 38.995548)]"""
			}
			pathAndValues.find {
				it.methodsBuffer.toString() == """.array().field("place").field("bounding_box").array("coordinates").array().contains(-77.119759).value()""" &&
				it.jsonPathBuffer.toString() == """\$[*].place.bounding_box.coordinates[*][*][?(@ == -77.119759)]"""
			}
			pathAndValues.find {
				it.methodsBuffer.toString() == """.array().field("place").field("bounding_box").array("coordinates").array().contains(-76.909393).value()""" &&
				it.jsonPathBuffer.toString() == """\$[*].place.bounding_box.coordinates[*][*][?(@ == -76.909393)]"""
			}
			pathAndValues.find {
				it.methodsBuffer.toString() == """.array().field("place").field("bounding_box").array("coordinates").array().contains(38.791645).value()""" &&
				it.jsonPathBuffer.toString() == """\$[*].place.bounding_box.coordinates[*][*][?(@ == 38.791645)]"""
			}
		and:
			pathAndValues.size() == 4
		and:
			pathAndValues.each {
				it.check()
			}
	}

}