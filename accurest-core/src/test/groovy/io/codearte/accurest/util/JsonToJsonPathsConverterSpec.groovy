package io.codearte.accurest.util
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import com.jayway.jsonpath.Configuration
import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.Option
import net.minidev.json.JSONArray
import spock.lang.Specification
import spock.lang.Unroll

import java.util.regex.Pattern

class JsonToJsonPathsConverterSpec extends Specification {

	@Unroll
	def 'should convert a json with list as root to a map of path to value'() {
		when:
			JsonPaths pathAndValues = JsonToJsonPathsConverter.transformToJsonPathWithTestsSideValues(new JsonSlurper().parseText(json))
		then:
			pathAndValues.find {
				it.method() == """.array().field("some").field("nested").field("json").isEqualTo("with value")""" &&
				it.jsonPath() == '''$[*].some.nested[?(@.json == 'with value')]'''
			}
			pathAndValues.find {
				it.method() == """.array().field("some").field("nested").field("anothervalue").isEqualTo(4)""" &&
				it.jsonPath() == '''$[*].some.nested[?(@.anothervalue == 4)]'''
			}
			pathAndValues.find {
				it.method() == """.array().field("some").field("nested").array("withlist").contains("name").isEqualTo("name1")""" &&
				it.jsonPath() == '''$[*].some.nested.withlist[*][?(@.name == 'name1')]'''
			}
			pathAndValues.find {
				it.method() == """.array().field("some").field("nested").array("withlist").contains("name").isEqualTo("name2")""" &&
				it.jsonPath() == '''$[*].some.nested.withlist[*][?(@.name == 'name2')]'''
			}
			pathAndValues.find {
				it.method() == """.array().field("some").field("nested").array("withlist").field("anothernested").field("name").isEqualTo("name3")""" &&
				it.jsonPath() == '''$[*].some.nested.withlist[*].anothernested[?(@.name == 'name3')]'''
			}
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
			JsonPaths pathAndValues = JsonToJsonPathsConverter.transformToJsonPathWithTestsSideValues(new JsonSlurper().parseText(json))
		then:
		pathAndValues.find {
			it.method() == """.field("some").field("nested").field("json").isEqualTo("with value")""" &&
			it.jsonPath() == '''$.some.nested[?(@.json == 'with value')]'''
		}
		pathAndValues.find {
			it.method() == """.field("some").field("nested").field("anothervalue").isEqualTo(4)""" &&
			it.jsonPath() == '''$.some.nested[?(@.anothervalue == 4)]'''
		}
		pathAndValues.find {
			it.method() == """.field("some").field("nested").array("withlist").contains("name").isEqualTo("name1")""" &&
			it.jsonPath() == '''$.some.nested.withlist[*][?(@.name == 'name1')]'''
		}
		pathAndValues.find {
			it.method() == """.field("some").field("nested").array("withlist").contains("name").isEqualTo("name2")""" &&
			it.jsonPath() == '''$.some.nested.withlist[*][?(@.name == 'name2')]'''
		}
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
			JsonPaths pathAndValues = JsonToJsonPathsConverter.transformToJsonPathWithTestsSideValues(new JsonSlurper().parseText(json))
		then:
			pathAndValues.find {
				it.method() == """.array("items").contains("HOP").value()""" &&
				it.jsonPath() == '''$.items[?(@ == 'HOP')]'''
			}
		and:
			assertThatJsonPathsInMapAreValid(json, pathAndValues)
		}

	def 'should convert a json with null and boolean values'() {
		given:
			String json = '''
					 {
							"property1" : null,
							"property2" : true
					}
'''
		when:
			JsonPaths pathAndValues = JsonToJsonPathsConverter.transformToJsonPathWithTestsSideValues(new JsonSlurper().parseText(json))
		then:
			pathAndValues.find {
				it.method() == """.field("property1").isNull()""" &&
				it.jsonPath() == '''$[?(@.property1 == null)]'''
			}
			pathAndValues.find {
				it.method() == """.field("property2").isEqualTo(true)""" &&
				it.jsonPath() == '''$[?(@.property2 == true)]'''
			}
	}

	def "should convert numbers map"() {
		given:
			String json = ''' {
                     "extensions": {"7":28.00,"14":41.00,"30":60.00}
                     }
 '''
		when:
			JsonPaths pathAndValues = JsonToJsonPathsConverter.transformToJsonPathWithTestsSideValues(new JsonSlurper().parseText(json))
		then:
			pathAndValues.find {
				it.method() == """.field("extensions").field("7").isEqualTo(28)""" &&
				it.jsonPath() == '''$.extensions[?(@.7 == 28)]'''
			}
			pathAndValues.find {
				it.method() == """.field("extensions").field("14").isEqualTo(41)""" &&
				it.jsonPath() == '''$.extensions[?(@.14 == 41)]'''
			}
			pathAndValues.find {
				it.method() == """.field("extensions").field("30").isEqualTo(60)""" &&
				it.jsonPath() == '''$.extensions[?(@.30 == 60)]'''
			}
		and:
			assertThatJsonPathsInMapAreValid(json, pathAndValues)
	}

	def 'should convert a json with a list of errors'() {
		given:
			String json = '''
					 {
							"errors" : [
								{ "property" : "email", "message" : "inconsistent value" },
								{ "property" : "email", "message" : "inconsistent value2" }
							]
					}
'''
		when:
			JsonPaths pathAndValues = JsonToJsonPathsConverter.transformToJsonPathWithTestsSideValues(new JsonSlurper().parseText(json))
		then:
			pathAndValues.find {
				it.method() == """.array("errors").contains("property").isEqualTo("email")""" &&
				it.jsonPath() == '''$.errors[*][?(@.property == 'email')]'''
			}
			pathAndValues.find {
				it.method() == """.array("errors").contains("message").isEqualTo("inconsistent value")""" &&
				it.jsonPath() == '''$.errors[*][?(@.message == 'inconsistent value')]'''
			}
			pathAndValues.find {
				it.method() == """.array("errors").contains("message").isEqualTo("inconsistent value2")""" &&
				it.jsonPath() == '''$.errors[*][?(@.message == 'inconsistent value2')]'''
			}
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
															  [name: Pattern.compile("[a-zA-Z]+")]
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
			JsonPaths pathAndValues = JsonToJsonPathsConverter.transformToJsonPathWithTestsSideValues(json)
		then:
			pathAndValues.find {
				it.method() == """.array().field("some").field("nested").field("json").isEqualTo("with value")""" &&
				it.jsonPath() == '''$[*].some.nested[?(@.json == 'with value')]'''
			}
			pathAndValues.find {
				it.method() == """.array().field("some").field("nested").field("anothervalue").isEqualTo(4)""" &&
				it.jsonPath() == '''$[*].some.nested[?(@.anothervalue == 4)]'''
			}
			pathAndValues.find {
				it.method() == """.array().field("some").field("nested").array("withlist").contains("name").isEqualTo("name1")""" &&
				it.jsonPath() == '''$[*].some.nested.withlist[*][?(@.name == 'name1')]'''
			}
			pathAndValues.find {
				it.method() == """.array().field("some").field("nested").array("withlist").contains("name").isEqualTo("name2")""" &&
				it.jsonPath() == '''$[*].some.nested.withlist[*][?(@.name == 'name2')]'''
			}
			pathAndValues.find {
				it.method() == """.array().field("some").field("nested").array("withlist").field("anothernested").field("name").matches("[a-zA-Z]+")""" &&
				it.jsonPath() == '''$[*].some.nested.withlist[*].anothernested[?(@.name =~ /[a-zA-Z]+/)]'''
			}
		when:
			json.some.nested.withlist[0][2].anothernested.name = "Kowalski"
		then:
			assertThatJsonPathsInMapAreValid(JsonOutput.prettyPrint(JsonOutput.toJson(json)), pathAndValues)
		}

	private void assertThatJsonPathsInMapAreValid(String json, JsonPaths pathAndValues) {
		DocumentContext parsedJson = JsonPath.using(Configuration.builder().options(Option.ALWAYS_RETURN_LIST).build()).parse(json);
		pathAndValues.each {
			assert !parsedJson.read(it.jsonPath(), JSONArray).empty
		}
	}

}
