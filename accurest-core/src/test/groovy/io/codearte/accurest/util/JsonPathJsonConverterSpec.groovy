package io.codearte.accurest.util

import jarjar.com.jayway.jsonpath.Configuration
import jarjar.com.jayway.jsonpath.DocumentContext
import jarjar.com.jayway.jsonpath.JsonPath
import jarjar.com.jayway.jsonpath.Option
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import net.minidev.json.JSONArray
import spock.lang.Specification
import spock.lang.Unroll

import java.util.regex.Pattern

class JsonPathJsonConverterSpec extends Specification {

	@Unroll
	def 'should convert a json with list as root to a map of path to value'() {
		when:
			JsonPaths pathAndValues = JsonPathJsonConverter.transformToJsonPathWithValues(new JsonSlurper().parseText(json))
		then:
			pathAndValues['$[*].some.nested.json'] == 'with value'
			pathAndValues['$[*].some.nested.anothervalue'] == 4
			pathAndValues['''$[*].some.nested.withlist[*][?(@.name == 'name1')]'''] == 'name1'
			pathAndValues['''$[*].some.nested.withlist[*][?(@.name == 'name2')]'''] == 'name2'
			pathAndValues['''$[*].some.nested.withlist[*].anothernested[?(@.name == 'name3')]'''] == 'name3'
		and:
			assertThatJsonPathsInMapAreValid(json, pathAndValues)
		where:
			json << [
					'''
						[ {
								"some" : {
									"nested" : {
										"json" : "with value",
										"anothervalue": 4,
										"withlist" : [
											{ "name" :"name1"} , {"name": "name2"}, {"anothernested": { "name": "name3"} }
										]
									}
								}
							},
							{
								"someother" : {
									"nested" : {
										"json" : "with value",
										"anothervalue": 4,
										"withlist" : [
											{ "name" :"name1"} , {"name": "name2"}
										]
									}
								}
							}
						]
	''',
		'''
							[{
								"someother" : {
									"nested" : {
										"json" : "with value",
										"anothervalue": 4,
										"withlist" : [
											{ "name" :"name1"} , {"name": "name2"}
										]
									}
								}
							},
						 {
								"some" : {
									"nested" : {
										"json" : "with value",
										"anothervalue": 4,
										"withlist" : [
											 {"name": "name2"}, {"anothernested": { "name": "name3"} }, { "name" :"name1"}
										]
									}
								}
							}
						]''']
		}

	def 'should convert a json with a map as root to a map of path to value'() {
		given:
			String json = '''
					 {
							"some" : {
								"nested" : {
									"json" : "with value",
									"anothervalue": 4,
									"withlist" : [
										{ "name" :"name1"} , {"name": "name2"}
									]
								}
							}
						}
'''
		when:
			JsonPaths pathAndValues = JsonPathJsonConverter.transformToJsonPathWithValues(new JsonSlurper().parseText(json))
		then:
			pathAndValues['$.some.nested.json'] == 'with value'
			pathAndValues['$.some.nested.anothervalue'] == 4
			pathAndValues['''$.some.nested.withlist[*][?(@.name == 'name1')]'''] == 'name1'
			pathAndValues['''$.some.nested.withlist[*][?(@.name == 'name2')]'''] == 'name2'
		and:
			assertThatJsonPathsInMapAreValid(json, pathAndValues)
		}

	def 'should convert a json with a list'() {
		given:
			String json = '''
					 {
							"items" : ["HOP"]
					}
'''
		when:
			JsonPaths pathAndValues = JsonPathJsonConverter.transformToJsonPathWithValues(new JsonSlurper().parseText(json))
		then:
			pathAndValues['''$.items[?(@ == 'HOP')]'''] == 'HOP'
		and:
			assertThatJsonPathsInMapAreValid(json, pathAndValues)
		}


	def 'should convert a map json with a regex pattern'() {
		given:
			List json = [
					[some:
							 [nested: [
									 json: "with value",
									 anothervalue: 4,
									 withlist:
											 [
													 [name: "name2"],
													 [name: "name1"],
													 [anothernested:
															  [name: Pattern.compile('[a-zA-Z]+')]
													 ],
													 [age: "123456789"]
											 ]
							 ]
							 ]
					],
					[someother:
							 [nested: [
									 json: "with value",
									 anothervalue: 4,
									 withlist:
											 [
													 [name: "name2"],
													 [name: "name1"]
											 ]
							 ]
							 ]
					]
			]
		when:
			JsonPaths pathAndValues = JsonPathJsonConverter.transformToJsonPathWithValues(json)
		then:
			pathAndValues['$[*].some.nested.json'] == 'with value'
			pathAndValues['$[*].some.nested.anothervalue'] == 4
			pathAndValues['''$[*].some.nested.withlist[*][?(@.name == 'name1')]'''] == 'name1'
			pathAndValues['''$[*].some.nested.withlist[*][?(@.name == 'name2')]''']
			(pathAndValues['''$[*].some.nested.withlist[*].anothernested[?(@.name =~ /[a-zA-Z]+/)]'''] as Pattern).pattern() == '[a-zA-Z]+'
		//when:
			//pathAndValues['''$[*].some.nested.withlist[*].anothernested[?(@.name =~ /[a-zA-Z]+/)]'''] = "Kowalski"
			//json.some.nested.withlist[0][2].anothernested.name = "Kowalski"
		and:
			assertThatJsonPathsInMapAreValid(JsonOutput.prettyPrint(JsonOutput.toJson(json)), pathAndValues)
		}

	private void assertThatJsonPathsInMapAreValid(String json, JsonPaths pathAndValues) {
		DocumentContext parsedJson = JsonPath.using(Configuration.builder().options(Option.ALWAYS_RETURN_LIST).build()).parse(json);
		pathAndValues.each {
			assert parsedJson.read(it.jsonPath, JSONArray).getAt(it.optionalSuffix ?: 0) == it.optionalSuffix ? [it.value] : it.value
		}
	}
}
