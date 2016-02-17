package io.codearte.accurest.builder

import groovy.json.JsonOutput
import groovy.transform.PackageScope
import groovy.transform.TypeChecked
import io.codearte.accurest.dsl.GroovyDsl
import io.codearte.accurest.dsl.internal.DslProperty
import io.codearte.accurest.dsl.internal.ExecutionProperty
import io.codearte.accurest.dsl.internal.MatchingStrategy
import io.codearte.accurest.dsl.internal.QueryParameter
import io.codearte.accurest.dsl.internal.Request
import io.codearte.accurest.dsl.internal.Response
import io.codearte.accurest.util.ContentType
import io.codearte.accurest.util.JsonPaths
import io.codearte.accurest.util.JsonToJsonPathsConverter
import io.codearte.accurest.util.MapConverter

import static io.codearte.accurest.util.ContentUtils.extractValue
import static io.codearte.accurest.util.ContentUtils.recognizeContentTypeFromContent
import static io.codearte.accurest.util.ContentUtils.recognizeContentTypeFromHeader

/**
 * @author Olga Maciaszek-Sharma
 * @since 2016-02-17
 */
@TypeChecked
@PackageScope
abstract class MethodBodyBuilder {

	protected final Request request
	protected final Response response

	MethodBodyBuilder(GroovyDsl stubDefinition) {
		this.request = stubDefinition.request
		this.response = stubDefinition.response
	}

	protected abstract void validateResponseCodeBlock(BlockBuilder bb)

	protected abstract void validateResponseHeadersBlock(BlockBuilder bb)

	protected void given(BlockBuilder bb) {}

	protected abstract void when(BlockBuilder bb)

	protected abstract String getResponseAsString()

	protected abstract String addCommentSignIfRequired(String baseString)

	protected abstract String addColonIfRequired(String baseString)

	protected abstract String getResponseBodyPropertyComparisonString(String property, String value)

	protected abstract String getMultipartParameterLine(Map.Entry<String, Object> parameter)

	protected abstract void processBodyElement(BlockBuilder blockBuilder, String property, ExecutionProperty exec)

	protected abstract void processBodyElement(BlockBuilder blockBuilder, String property, Map.Entry entry)

	protected abstract String getPropertyInListString(String property, Integer index)

	protected abstract String convertUnicodeEscapesIfRequired(String json)

	void appendTo(BlockBuilder blockBuilder) {
		blockBuilder.startBlock()

		givenBlock(blockBuilder)
		whenBlock(blockBuilder)
		thenBlock(blockBuilder)

		blockBuilder.endBlock()
	}
	protected void thenBlock(BlockBuilder bb) {
		bb.addLine(addCommentSignIfRequired('then:'))
		bb.startBlock()
		then(bb)
		bb.endBlock()
	}

	protected void whenBlock(BlockBuilder bb) {
		bb.addLine(addCommentSignIfRequired('when:'))
		bb.startBlock()
		when(bb)
		bb.endBlock().addEmptyLine()
	}

	protected void givenBlock(BlockBuilder bb) {
		bb.addLine(addCommentSignIfRequired('given:'))
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
			bb.addLine('and:').startBlock()
			validateResponseBodyBlock(bb)
		}
	}

	private void validateResponseBodyBlock(BlockBuilder bb) {
		def responseBody = response.body.serverValue
		ContentType contentType = getResponseContentType()
		if (responseBody instanceof GString) {
			responseBody = extractValue(responseBody, contentType, { DslProperty dslProperty -> dslProperty.serverValue })
		}
		if (contentType == ContentType.JSON) {
			appendJsonPath(bb, responseAsString)
			JsonPaths jsonPaths = JsonToJsonPathsConverter.transformToJsonPathWithTestsSideValues(responseBody)
			jsonPaths.each {
				bb.addLine("assertThat(parsedJson)" + it.method())
			}
			processBodyElement(bb, "", responseBody)
		} else if (contentType == ContentType.XML) {
			bb.addLine("def responseBody = new XmlSlurper().parseText($responseAsString)")
			// TODO xml validation
		}   else {
			bb.addLine("def responseBody = ($responseAsString)")
			processText(bb, "", responseBody as String)
		}
	}

	private ContentType getResponseContentType() {
		ContentType contentType = recognizeContentTypeFromHeader(response.headers)
		if (contentType == ContentType.UNKNOWN) {
			contentType = recognizeContentTypeFromContent(response.body.serverValue)
		}
		return contentType
	}

	protected void appendJsonPath(BlockBuilder blockBuilder, String json) {
		blockBuilder.addLine(addColonIfRequired("DocumentContext parsedJson = JsonPath.parse($json)"))
	}

	protected void processText(BlockBuilder blockBuilder, String property, String value) {
		if (value.startsWith('$')) {
			value = value.substring(1).replaceAll('\\$value', "responseBody$property")
			blockBuilder.addLine(value)
		} else {
			blockBuilder.addLine(getResponseBodyPropertyComparisonString(property, value))
		}
	}

	protected void processBodyElement(BlockBuilder blockBuilder, String property, Object value) {
	}

	protected String getBodyAsString() {
		Object bodyValue = extractServerValueFromBody(request.body.serverValue)
		String json = new JsonOutput().toJson(bodyValue)
		json = convertUnicodeEscapesIfRequired(json)
		return trimRepeatedQuotes(json)
	}

	protected Map<String, Object> getMultipartParameters() {
		return (Map<String, Object>) request?.multipart?.serverValue
	}

	protected String trimRepeatedQuotes(String toTrim) {
		return toTrim.startsWith('"') ? toTrim.replaceAll('"', '') : toTrim   // TODO: implement for JUnit
	}

	protected Object extractServerValueFromBody(bodyValue) {
		if (bodyValue instanceof GString) {
			bodyValue = extractValue(bodyValue, { DslProperty dslProperty -> dslProperty.serverValue })
		} else {
			bodyValue = MapConverter.transformValues(bodyValue, { it instanceof DslProperty ? it.serverValue : it })
		}
		return bodyValue
	}

	protected boolean allowedQueryParameter(QueryParameter param) {
		return allowedQueryParameter(param.serverValue)
	}

	protected boolean allowedQueryParameter(MatchingStrategy matchingStrategy) {
		return matchingStrategy.type != MatchingStrategy.Type.ABSENT
	}

	protected boolean allowedQueryParameter(Object o) {
		return true
	}

	protected String resolveParamValue(QueryParameter param) {
		return resolveParamValue(param.serverValue)
	}

	protected String resolveParamValue(Object value) {
		return value.toString()
	}

	protected String resolveParamValue(MatchingStrategy matchingStrategy) {
		return matchingStrategy.serverValue.toString()
	}

	protected ContentType getRequestContentType() {
		ContentType contentType = recognizeContentTypeFromHeader(request.headers)
		if (contentType == ContentType.UNKNOWN) {
			contentType = recognizeContentTypeFromContent(request.body.serverValue)
		}
		return contentType
	}

	protected String getTestSideValue(Object object) {
		return MapConverter.getTestSideValues(object).toString()
	}

	protected void processBodyElement(BlockBuilder blockBuilder, String property, Map map) {
		map.each {
			processBodyElement(blockBuilder, property, it)
		}
	}

	protected void processBodyElement(BlockBuilder blockBuilder, String property, List list) {
		list.eachWithIndex { listElement, listIndex ->
			String prop = getPropertyInListString(property, listIndex as Integer)
			processBodyElement(blockBuilder, prop, listElement)
		}
	}
}
