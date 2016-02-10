package io.codearte.accurest.assertion

import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
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
		Asserter asserter = new FieldAssertion(parsedJson, jsonPathBuffer, methodsBuffer,  '')
		asserter.jsonPathBuffer.append('$')
		return asserter
	}

	FieldAssertion field(String value) {
		Asserter asserter = new FieldAssertion(parsedJson, jsonPathBuffer, methodsBuffer, value)
		asserter.jsonPathBuffer.append(".$value")
		asserter.methodsBuffer.append(".field('$value')")
		return asserter
	}

	ArrayAssertion array(String value) {
		Asserter asserter = new ArrayAssertion(parsedJson, jsonPathBuffer, methodsBuffer, value)
		asserter.jsonPathBuffer.append(".$value[*]")
		asserter.methodsBuffer.append(".array('$value')")
		return asserter
	}

	class FieldAssertion extends Asserter {

		protected FieldAssertion(DocumentContext parsedJson, StringBuffer jsonPathBuffer, StringBuffer methodsBuffer, String fieldName) {
			super(parsedJson, jsonPathBuffer, methodsBuffer, fieldName)
		}

	}

	class ArrayAssertion extends Asserter {

		protected ArrayAssertion(DocumentContext parsedJson, StringBuffer jsonPathBuffer, StringBuffer methodsBuffer, String arrayName) {
			super(parsedJson, jsonPathBuffer, methodsBuffer, arrayName)
		}

		@Override
		FieldAssertion field(String value) {
			Asserter asserter = new FieldAssertion(parsedJson, jsonPathBuffer, methodsBuffer, value)
			asserter.jsonPathBuffer.append(".$value")
			asserter.methodsBuffer.append(".contains('$value')")
			return asserter
		}

		@Override
		FieldAssertion fieldBeforeMatching(String value) {
			Asserter asserter = new FieldAssertion(parsedJson, jsonPathBuffer, methodsBuffer, value)
			asserter.methodsBuffer.append(".contains('$value')")
			return asserter
		}

	}

	/**
	 * TODO: Should be trait or mixin - don't remember which one we support
	 */
	@ToString(includePackage = false)
	@EqualsAndHashCode
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

		FieldAssertion contains(String value) {
			Asserter asserter = new FieldAssertion(parsedJson, jsonPathBuffer, methodsBuffer, value)
			asserter.methodsBuffer.append(".contains('$value')")
			return asserter
		}

		FieldAssertion field(String value) {
			Asserter asserter = new FieldAssertion(parsedJson, jsonPathBuffer, methodsBuffer, value)
			asserter.jsonPathBuffer.append(".$value")
			asserter.methodsBuffer.append(".field('$value')")
			return asserter
		}

		FieldAssertion fieldBeforeMatching(String value) {
			Asserter asserter = new FieldAssertion(parsedJson, jsonPathBuffer, methodsBuffer, value)
			asserter.methodsBuffer.append(".field('$value')")
			return asserter
		}

		ArrayAssertion array(String value) {
			Asserter asserter = new ArrayAssertion(parsedJson, jsonPathBuffer, methodsBuffer, value)
			asserter.jsonPathBuffer.append(".$value[*]")
			asserter.methodsBuffer.append(".array('$value')")
			return asserter
		}

		Asserter isEqualTo(String value) {
			jsonPathBuffer.append("""[?(@.$fieldName == '$value')]""")
			methodsBuffer.append(".isEqualTo('''$value''')")
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
			methodsBuffer.append(".matches('''$value''')")
			return this
		}

		Asserter isEqualTo(Boolean value) {
			jsonPathBuffer.append("""[?(@.$fieldName == $value)]""")
			methodsBuffer.append(".isEqualTo($value)")
			return this
		}

		void check() {
			assert !parsedJson.read(jsonPathBuffer.toString(), JSONArray).empty
		}

	}
}
