package io.codearte.accurest.util

import com.jayway.jsonpath.Configuration
import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.Option
import groovy.json.JsonSlurper
import net.minidev.json.JSONArray
import spock.lang.Specification

class JsonConverterSpec extends Specification {

	def 'should convert a json with list as root to a map of path to value'() {
		when:
			Map<String, Object> pathAndValues = JsonConverter.transformToJsonPathWithValues(new JsonSlurper().parseText(json))
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
						]
	'''


			]

		}

	def 'should convert a json with a map as tooy to a map of path to value'() {
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
			Map<String, Object> pathAndValues = JsonConverter.transformToJsonPathWithValues(new JsonSlurper().parseText(json))
		then:
			pathAndValues['$.some.nested.json'] == 'with value'
			pathAndValues['$.some.nested.anothervalue'] == 4
			pathAndValues['''$.some.nested.withlist[*][?(@.name == 'name1')]'''] == 'name1'
			pathAndValues['''$.some.nested.withlist[*][?(@.name == 'name2')]'''] == 'name2'
		and:
			assertThatJsonPathsInMapAreValid(json, pathAndValues)

		}

	private void assertThatJsonPathsInMapAreValid(String json, Map<String, Object> pathAndValues) {
		DocumentContext parsedJson = JsonPath.using(Configuration.builder().options(Option.ALWAYS_RETURN_LIST, Option.AS_PATH_LIST).build()).parse(json);
		pathAndValues.entrySet().each {
			assert !(parsedJson.read(it.key, JSONArray).empty)
		}
	}

	JSONArray valueAsJsonArray(Object value) {
		JSONArray jsonArray = new JSONArray()
		jsonArray.add(value)
		return jsonArray
	}
}
