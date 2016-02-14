package com.blogspot.toomuchcoding.jsonpathassert

import groovy.json.JsonSlurper
import io.codearte.accurest.util.JsonPaths
import io.codearte.accurest.util.JsonToJsonPathsConverter
import spock.lang.Specification

import java.util.regex.Pattern

import static com.blogspot.toomuchcoding.jsonpathassert.JsonPathAssertion.assertThat

/**
 * TODO: Rewrite these tests to ensure that there is no reference to accurest. We need to test only jsonpaths
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
				it.jsonPath() == """\$.some.nested[?(@.anothervalue == 4)]"""
			}
			pathAndValues.find {
				it.jsonPath() == """\$.some.nested.withlist[*][?(@.name == 'name1')]"""
			}
			pathAndValues.find {
				it.jsonPath() == """\$.some.nested.withlist[*][?(@.name == 'name2')]"""
			}
			pathAndValues.find {
				it.jsonPath() == """\$.some.nested[?(@.json == 'with "val\\'ue')]"""
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
				it.jsonPath() == """\$[?(@.property1 == 'a')]"""
			}
			pathAndValues.find {
				it.jsonPath() == """\$[?(@.property2 == 'b')]"""
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
				it.jsonPath() == """\$[?(@.property1 == 'true')]"""
			}
			pathAndValues.find {
				it.jsonPath() == """\$[?(@.property2 == null)]"""
			}
			pathAndValues.find {
				it.jsonPath() == """\$[?(@.property3 == false)]"""
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
				it.jsonPath() == """\$[?(@.property1 == 'a')]"""
			}
			pathAndValues.find {
				it.jsonPath() == """\$.property2[*][?(@.a == 'sth')]"""
			}
			pathAndValues.find {
				it.jsonPath() == """\$.property2[*][?(@.b == 'sthElse')]"""
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
				it.jsonPath() == """\$.property[?(@.7 == 0.0)]"""
			}
			pathAndValues.find {
				it.jsonPath() == """\$.property[?(@.14 == 0.0)]"""
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
				it.jsonPath() == """\$[*][?(@.property1 == 'a')]"""
			}
			pathAndValues.find {
				it.jsonPath() == """\$[*][?(@.property2 == 'b')]"""
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
				it.jsonPath() == """\$.property1[*][?(@.property2 == 'test1')]"""
			}
			pathAndValues.find {
				it.jsonPath() == """\$.property1[*][?(@.property3 == 'test2')]"""
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
				it.jsonPath() == """\$.property2[?(@.property3 == 'b')]"""
			}
			pathAndValues.find {
				it.jsonPath() == """\$[?(@.property1 == 'a')]"""
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
				it.jsonPath() == """\$[?(@.property2 =~ /[0-9]{3}/)]"""
			}
			pathAndValues.find {
				it.jsonPath() == """\$[?(@.property1 == 'a')]"""
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
				it.jsonPath() == """\$[?(@.property2 =~ /\\d+/)]"""
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
			it.jsonPath() == """\$.errors[*][?(@.property == 'bank_account_number')]"""
		}
		pathAndValues.find {
			it.jsonPath() == """\$.errors[*][?(@.message == 'incorrect_format')]"""
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
				it.jsonPath() == """\$[*].place.bounding_box.coordinates[*][*][?(@ == 38.995548)]"""
			}
			pathAndValues.find {
				it.jsonPath() == """\$[*].place.bounding_box.coordinates[*][*][?(@ == -77.119759)]"""
			}
			pathAndValues.find {
				it.jsonPath() == """\$[*].place.bounding_box.coordinates[*][*][?(@ == -76.909393)]"""
			}
			pathAndValues.find {
				it.jsonPath() == """\$[*].place.bounding_box.coordinates[*][*][?(@ == 38.791645)]"""
			}
		and:
			pathAndValues.size() == 4
		and:
			pathAndValues.each {
				it.check()
			}
	}

}