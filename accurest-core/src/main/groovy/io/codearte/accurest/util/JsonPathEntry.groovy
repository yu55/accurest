package io.codearte.accurest.util

import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import groovy.transform.EqualsAndHashCode
import net.minidev.json.JSONArray
/**
 * @author Marcin Grzejszczak
 */
@EqualsAndHashCode
class JsonPathEntry {

	private final DocumentContext parsedJson
	private final StringBuffer jsonPathBuffer = new StringBuffer()
	private final StringBuffer methodsBuffer = new StringBuffer()

	protected JsonPathEntry(DocumentContext parsedJson) {
		this.parsedJson = parsedJson
	}

	static JsonPathEntry assertThat(String body) {
		DocumentContext parsedJson = JsonPath.parse(body)
		return new JsonPathEntry(parsedJson)
	}

	protected RootFieldAssertion root() {
		Asserter asserter = new RootFieldAssertion(parsedJson, jsonPathBuffer, methodsBuffer,  '')
		asserter.jsonPathBuffer.append('$')
		return asserter
	}

	FieldAssertion field(String value) {
		Asserter asserter = new FieldAssertion(parsedJson, jsonPathBuffer, methodsBuffer, value)
		asserter.field(value);
		return asserter
	}

	ArrayAssertion array() {
		Asserter asserter = new ArrayAssertion(parsedJson, jsonPathBuffer, methodsBuffer)
		asserter.jsonPathBuffer.append("[*]")
		asserter.methodsBuffer.append(".array()")
		return asserter
	}

	protected class RootFieldAssertion extends FieldAssertion {
		protected RootFieldAssertion(DocumentContext parsedJson, StringBuffer jsonPathBuffer,
								 StringBuffer methodsBuffer, String fieldName) {
			super(parsedJson, jsonPathBuffer, methodsBuffer, fieldName)
		}

		ArrayAssertion array() {
			Asserter asserter = new ArrayAssertion(parsedJson, jsonPathBuffer, methodsBuffer)
			asserter.jsonPathBuffer.append("[*]")
			asserter.methodsBuffer.append(".array()")
			return asserter
		}
	}

	protected class FieldAssertion extends Asserter {
		protected FieldAssertion(DocumentContext parsedJson, StringBuffer jsonPathBuffer,
								 StringBuffer methodsBuffer, String fieldName) {
			super(parsedJson, jsonPathBuffer, methodsBuffer, fieldName)
		}
	}

	protected class ArrayAssertion extends Asserter {
		protected ArrayAssertion(DocumentContext parsedJson, StringBuffer jsonPathBuffer,
								 StringBuffer methodsBuffer, String arrayName) {
			super(parsedJson, jsonPathBuffer, methodsBuffer, arrayName)
		}

		protected ArrayAssertion(DocumentContext parsedJson, StringBuffer jsonPathBuffer,
								 StringBuffer methodsBuffer) {
			super(parsedJson, jsonPathBuffer, methodsBuffer, null)
		}

		@Override
		FieldAssertion fieldBeforeMatching(String value) {
			Asserter asserter = new FieldAssertion(parsedJson, jsonPathBuffer, methodsBuffer, value)
			asserter.methodsBuffer.append(".contains('$value')")
			return asserter
		}

	}

	protected class ArrayValueAssertion extends FieldAssertion {
		protected ArrayValueAssertion(DocumentContext parsedJson, StringBuffer jsonPathBuffer,
									  StringBuffer methodsBuffer, String arrayName) {
			super(parsedJson, jsonPathBuffer, methodsBuffer, arrayName)
		}

		@Override
		ArrayValueAssertion contains(Object value) {
			Asserter asserter = new ArrayValueAssertion(parsedJson, jsonPathBuffer, methodsBuffer, value)
			asserter.methodsBuffer.append(".contains(${wrapValue(value)})")
			return asserter
		}

		Asserter value(String value) {
			jsonPathBuffer.append("""[?(@ == '$value')]""")
			methodsBuffer.append(".value()")
			return this
		}

		@Override
		Asserter isEqualTo(String value) {
			jsonPathBuffer.append("""[?(@ == '$value')]""")
			methodsBuffer.append(".value()")
			return this
		}

		@Override
		Asserter isEqualTo(Number value) {
			jsonPathBuffer.append("""[?(@ == $value)]""")
			methodsBuffer.append(".value()")
			return this
		}

		@Override
		Asserter matches(String value) {
			jsonPathBuffer.append("""[?(@ =~ /$value/)]""")
			methodsBuffer.append(".value()")
			return this
		}

		@Override
		Asserter isEqualTo(Boolean value) {
			jsonPathBuffer.append("""[?(@ == $value)]""")
			methodsBuffer.append(".value()")
			return this
		}

	}

	@EqualsAndHashCode(includeFields = true)
	class Asserter {
		protected final DocumentContext parsedJson
		protected final StringBuffer jsonPathBuffer
		protected final StringBuffer methodsBuffer
		protected final String fieldName

		protected Asserter(DocumentContext parsedJson, StringBuffer jsonPathBuffer, StringBuffer methodsBuffer, String fieldName) {
			this.parsedJson = parsedJson
			this.jsonPathBuffer = new StringBuffer(jsonPathBuffer.toString())
			this.methodsBuffer = new StringBuffer(methodsBuffer.toString())
			this.fieldName = fieldName
		}

		FieldAssertion contains(Object value) {
			Asserter asserter = new FieldAssertion(parsedJson, jsonPathBuffer, methodsBuffer, value)
			asserter.methodsBuffer.append(".contains(${wrapValue(value)})")
			return asserter
		}

		protected String wrapValue(Object value) {
			return value instanceof String ? "'$value'" : value
		}

		FieldAssertion field(String value) {
			Asserter asserter = new FieldAssertion(parsedJson, jsonPathBuffer, methodsBuffer, value)
			asserter.jsonPathBuffer.append(".$value")
			asserter.methodsBuffer.append(".field('$value')")
			return asserter
		}

		protected FieldAssertion fieldBeforeMatching(String value) {
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

		protected ArrayValueAssertion arrayField(String value) {
			Asserter asserter = new ArrayValueAssertion(parsedJson, jsonPathBuffer, methodsBuffer, value)
			asserter.jsonPathBuffer.append(".$value")
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

		String methodWithAppendedCheck() {
			return methodsBuffer.append(".check()").toString()
		}

		String jsonPath() {
			return jsonPathBuffer.toString()
		}

		void check() {
			assert !parsedJson.read(jsonPathBuffer.toString(), JSONArray).empty
		}

		boolean equals(o) {
			if (this.is(o)) return true
			if (getClass() != o.class) return false

			Asserter asserter = (Asserter) o

			if (fieldName != asserter.fieldName) return false
			if (jsonPathBuffer != asserter.jsonPathBuffer) return false
			if (methodsBuffer != asserter.methodsBuffer) return false

			return true
		}

		@Override
		public String toString() {
			return """\
Asserter{
    jsonPathBuffer=$jsonPathBuffer,
    methodsBuffer=$methodsBuffer
}"""
		}
	}
}
