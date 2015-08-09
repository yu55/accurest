package io.codearte.accurest.builder

import groovy.transform.PackageScope
import groovy.transform.TypeChecked
import io.codearte.accurest.dsl.GroovyDsl
import io.codearte.accurest.dsl.internal.DslProperty
import io.codearte.accurest.dsl.internal.ExecutionProperty
import io.codearte.accurest.util.ContentType

import java.util.regex.Pattern

import static io.codearte.accurest.util.ContentUtils.extractValue

/**
 * @author Jakub Kubrynski
 * @author Olga Maciaszek-Sharma
 */
@PackageScope
@TypeChecked
abstract class JUnitMethodBodyBuilder extends MethodBodyBuilder {


	private BodyTypeBuilder bodyTypeBuilder = new BodyTypeBuilder()

	private List<GString> assertions = new ArrayList<>()

	JUnitMethodBodyBuilder(GroovyDsl stubDefinition) {
		super(stubDefinition)
	}

	@Override
	protected void thenBlock(BlockBuilder bb) {
		bb.addLine('// then:')
		bb.startBlock()
		then(bb)
		bb.endBlock().addEmptyLine()
	}

	@Override
	protected void whenBlock(BlockBuilder bb) {
		bb.addLine('// when:')
		bb.startBlock()
		when(bb)
		bb.endBlock().addEmptyLine()
	}

	@Override
	protected void givenBlock(BlockBuilder bb) {
		bb.addLine('// given:')
		bb.startBlock()
		given(bb)
		bb.endBlock().addEmptyLine()
	}

	protected void then(BlockBuilder bb) {
		validateResponseCodeBlock(bb)
		if (response.headers) {
			validateResponseHeadersBlock(bb)
		}
		if (response.body) {
			bb.endBlock()
					.startBlock()
			validateResponseBodyBlock(bb)
		}
	}

	protected void validateResponseBodyBlock(BlockBuilder bb) {
		def responseBody = response.body.serverValue
		ContentType contentType = getResponseContentType()
		if (responseBody instanceof GString) {
			responseBody = extractValue(responseBody, contentType, { DslProperty dslProperty -> dslProperty.serverValue })
		}
		if (contentType == ContentType.JSON) {
			processBodyElement(assertions, "", responseBody)
			bb.addLine("${bodyTypeBuilder.build()} responseBody = (${bodyTypeBuilder.build()}) new JsonSlurper().parseText($responseAsString);")
			bb.addLines(assertions)
		} else if (contentType == ContentType.XML) {
			bb.addLine("${bodyTypeBuilder.build()}responseBody = (${bodyTypeBuilder.build()}) new XmlSlurper().parseText($responseAsString);")
			// TODO xml validation
		} else {
			bb.addLine("String responseBody = ($responseAsString);")
			processBodyElement(assertions, "", responseBody)
		}
	}


	protected void processBodyElement(List<GString> assertions, String property, Map.Entry entry) {
		processBodyElement(assertions, property + """.get("$entry.key")""", entry.value)
	}


	protected void processBodyElement(List<GString> assertions, String property, Pattern pattern) {
		assertions.add("""assertTrue(java.util.regex.Pattern.matches(java.util.regex.Pattern.compile("${
			pattern.pattern()
		}"), responseBody$property);""")
	}

	protected void processBodyElement(List<GString> assertions, String property, Object value) {
		assertions.add("assertTrue(responseBody${property}.equals(\"${value}\"));")
	}


	protected void processBodyElement(List<GString> assertions, String property, String value) {
		if (value.startsWith('$')) {
			value = value.substring(1).replaceAll('\\$value', "responseBody$property")
			assertions.add(value as GString)
		} else {
			assertions.add("assertTrue(responseBody${property}.equals(\"${value}\"));")
		}
	}

	protected void processBodyElement(List<GString> assertions, String property, DslProperty dslProperty) {
		processBodyElement(assertions, property, dslProperty.serverValue)
	}

	protected void processBodyElement(List<GString> assertions, String property, ExecutionProperty exec) {
		assertions.add("${exec.insertValue("responseBody$property")}")
	}

	protected void processBodyElement(List<GString> assertions, String property, Map map) {
		map.each {
			processBodyElement(assertions, property, it)
					}
			}


	protected void processBodyElement(List<GString> assertions, String property, List list) {
		list.eachWithIndex { listElement, listIndex ->
			String prop = "$property[$listIndex]" ?: ''
			processBodyElement(assertions, prop, listElement)
		}
	}

/*
	given()
	.contentType("application/json")
	.header("Accept", "application/json")
	.body("{'name': 'MyApp', 'description' : 'awesome app'}".replaceAll("'", "\"")).

	expect()
	.statusCode(200).body("id", is(not(nullValue()))).

	when()
	.post(root.toString() + "rest/applications").asString();*/
}
