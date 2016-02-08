package io.codearte.accurest.assertion

import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import groovy.transform.EqualsAndHashCode
import net.minidev.json.JSONArray

/**
 * @author Marcin Grzejszczak
 */
@EqualsAndHashCode
class JsonPathAssertionEntry {

	private final DocumentContext parsedJson
	private final StringBuffer jsonPathBuffer = new StringBuffer()
	private final StringBuffer methodsBuffer = new StringBuffer()

	JsonPathAssertionEntry(DocumentContext parsedJson) {
		this.parsedJson = parsedJson
	}

	static JsonPathAssertionEntry assertThat(String body) {
		DocumentContext parsedJson = JsonPath.parse(body)
		return new JsonPathAssertionEntry(parsedJson)
	}

	FieldAssertion root() {
		jsonPathBuffer.append('$')
		return new FieldAssertion(parsedJson, jsonPathBuffer, methodsBuffer,  '')
	}

	FieldAssertion field(String value) {
		jsonPathBuffer.append(".$value")
		methodsBuffer.append(".field('$value')")
		return new FieldAssertion(parsedJson, jsonPathBuffer, methodsBuffer, value)
	}

	ArrayAssertion array(String value) {
		jsonPathBuffer.append(".$value[*]")
		methodsBuffer.append(".array('$value')")
		return new ArrayAssertion(parsedJson, jsonPathBuffer, methodsBuffer, value)
	}

	class FieldAssertion extends Asserter {

		protected FieldAssertion(DocumentContext parsedJson, StringBuffer jsonPathBuffer, StringBuffer methodsBuffer, String fieldName) {
			super(parsedJson, jsonPathBuffer, methodsBuffer, fieldName)
		}


	}

	class ArrayFieldAssertion extends Asserter {

		protected ArrayFieldAssertion(DocumentContext parsedJson, StringBuffer jsonPathBuffer, StringBuffer methodsBuffer, String fieldName) {
			super(parsedJson, jsonPathBuffer, methodsBuffer, fieldName)
		}

	}

	class ArrayAssertion extends Asserter {

		protected ArrayAssertion(DocumentContext parsedJson, StringBuffer jsonPathBuffer, StringBuffer methodsBuffer, String arrayName) {
			super(parsedJson, jsonPathBuffer, methodsBuffer, arrayName)
		}

	}

	/**
	 * TODO: Should be trait or mixin - don't remember which one we support
	 */
	class Asserter {
		protected final DocumentContext parsedJson
		final StringBuffer jsonPathBuffer
		final StringBuffer methodsBuffer
		protected final String fieldName

		protected Asserter(DocumentContext parsedJson, StringBuffer jsonPathBuffer, StringBuffer methodsBuffer, String fieldName) {
			this.parsedJson = parsedJson
			this.jsonPathBuffer = new StringBuffer(jsonPathBuffer.toString())
			this.methodsBuffer = new StringBuffer(methodsBuffer.toString())
			this.fieldName = fieldName
		}

		ArrayFieldAssertion contains(String value) {
			methodsBuffer.append(".contains('$value')")
			return new ArrayFieldAssertion(parsedJson, jsonPathBuffer, methodsBuffer, value)
		}

		FieldAssertion field(String value) {
			jsonPathBuffer.append(".$value")
			methodsBuffer.append(".field('$value')")
			return new FieldAssertion(parsedJson, jsonPathBuffer, methodsBuffer, value)
		}

		FieldAssertion fieldBeforeMatching(String value) {
			methodsBuffer.append(".field('$value')")
			return new FieldAssertion(parsedJson, jsonPathBuffer, methodsBuffer, value)
		}

		ArrayAssertion array(String value) {
			methodsBuffer.append(".array('$value')")
			return new ArrayAssertion(parsedJson, jsonPathBuffer, methodsBuffer, value)
		}

		Asserter isEqualTo(String value) {
			jsonPathBuffer.append("""[?(@.$fieldName == '$value')]""")
			methodsBuffer.append(".isEqualTo('$value')")
			return this
		}

		Asserter isEqualTo(Object value) {
			return isEqualTo(value as String)
		}

		Asserter isEqualTo(Number value) {
			jsonPathBuffer.append("""[?(@.$fieldName == $value)]""")
			methodsBuffer.append(".isEqualTo($value)")
			return this
		}

		Asserter matches(String value) {
			jsonPathBuffer.append("""[?(@.$fieldName =~ /$value/)]""")
			methodsBuffer.append(".matches('$value')")
			return this
		}

		Asserter isEqualTo(Boolean value) {
			jsonPathBuffer.append("""[?(@.$fieldName == $value)]""")
			methodsBuffer.append(".isEqualTo($value)")
			return this
		}

		void check() {
			!parsedJson.read(jsonPathBuffer.toString(), JSONArray).empty
		}

	}
}
