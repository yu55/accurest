package io.codearte.accurest

import io.codearte.accurest.config.AccurestConfigProperties
import io.codearte.accurest.config.TestMode
import io.codearte.accurest.file.Contract
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Unroll

import static io.codearte.accurest.config.TestFramework.JUNIT
import static io.codearte.accurest.config.TestFramework.SPOCK

class SingleTestGeneratorSpec extends Specification {

	@Rule
	TemporaryFolder tmpFolder = new TemporaryFolder()
	File file

	static List<String> jUnitClassStrings = ['package test;', 'import com.jayway.jsonpath.DocumentContext;', 'import com.jayway.jsonpath.JsonPath;',
	                                         'import org.junit.FixMethodOrder;', 'import org.junit.Ignore;', 'import org.junit.Test;', 'import org.junit.runners.MethodSorters;',
	                                         'import static com.blogspot.toomuchcoding.jsonassert.JsonAssertion.assertThatJson;', 'import static com.jayway.restassured.module.mockmvc.RestAssuredMockMvc.*;',
	                                         '@FixMethodOrder(MethodSorters.NAME_ASCENDING)', '@Test', '@Ignore', 'mport com.jayway.restassured.module.mockmvc.specification.MockMvcRequestSpecification;',
																						'import com.jayway.restassured.response.ResponseOptions;', 'import static org.assertj.core.api.Assertions.assertThat;']

	static List<String> spockClassStrings = ['package test', 'import com.jayway.jsonpath.DocumentContext', 'import com.jayway.jsonpath.JsonPath',
	                                         'import spock.lang.Ignore', 'import spock.lang.Specification', 'import spock.lang.Stepwise',
	                                         'import static com.blogspot.toomuchcoding.jsonassert.JsonAssertion.assertThatJson', 'import static com.jayway.restassured.module.mockmvc.RestAssuredMockMvc.*',
	                                         '@Stepwise', '@Ignore']

	def setup() {
		file = tmpFolder.newFile()
		file.write("""
				io.codearte.accurest.dsl.GroovyDsl.make {
					request {
						method 'PUT'
						url 'url'
					}
					response {
						status 200
					}
				}
""")
	}

	@Unroll
	def "should build MockMvc test class for #testFramework"() {
		given:
			AccurestConfigProperties properties = new AccurestConfigProperties();
			properties.targetFramework = testFramework
			Contract contract = new Contract(file.toPath(), true, 1, 2)
			contract.ignored >> true
			contract.order >> 2
			SingleTestGenerator testGenerator = new SingleTestGenerator(properties)

		when:
			String clazz = testGenerator.buildClass([contract], "test", "test")

		then:
			classStrings.each { clazz.contains(it) }

		where:
			testFramework | classStrings
			JUNIT         | jUnitClassStrings
			SPOCK         | spockClassStrings
	}

	@Unroll
	def "should build JaxRs test class for #testFramework"() {
		given:
			AccurestConfigProperties properties = new AccurestConfigProperties();
			properties.testMode = TestMode.JAXRSCLIENT
			properties.targetFramework = testFramework
			Contract contract = new Contract(file.toPath(), true, 1, 2)
			contract.ignored >> true
			contract.order >> 2
			SingleTestGenerator testGenerator = new SingleTestGenerator(properties)

		when:
			String clazz = testGenerator.buildClass([contract], "test", "test")

		then:
			classStrings.each { clazz.contains(it) }

		where:
			testFramework | classStrings
			JUNIT         | ['import static javax.ws.rs.client.Entity.*;', 'import javax.ws.rs.core.Response;']
			SPOCK         | ['import static javax.ws.rs.client.Entity.*;']
	}


}
