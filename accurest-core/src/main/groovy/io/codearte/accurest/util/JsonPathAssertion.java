package io.codearte.accurest.util;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import net.minidev.json.JSONArray;

/**
 * @author Marcin Grzejszczak
 */
public class JsonPathAssertion {
	private final DocumentContext parsedJson;
	private final StringBuffer jsonPathBuffer = new StringBuffer();
	private final StringBuffer methodsBuffer = new StringBuffer();

	protected JsonPathAssertion(DocumentContext parsedJson) {
		this.parsedJson = parsedJson;
	}

	public static JsonPathAssertion assertThat(String body) {
		DocumentContext parsedJson = JsonPath.parse(body);
		return new JsonPathAssertion(parsedJson);
	}

	protected NamelessArrayHavingFieldAssertion root() {
		NamelessArrayHavingFieldAssertion asserter = new NamelessArrayHavingFieldAssertion(parsedJson, jsonPathBuffer,
				methodsBuffer, "");
		asserter.jsonPathBuffer.append("$");
		return asserter;
	}

	public void matchesJsonPath(String jsonPath) {
		assert !parsedJson.read(jsonPath, JSONArray.class).isEmpty();
	}

	public FieldAssertion field(Object value) {
		FieldAssertion asserter = new FieldAssertion(parsedJson, jsonPathBuffer, methodsBuffer,
				value);
		asserter.field(value);
		return asserter;
	}

	public ArrayAssertion array() {
		ArrayAssertion asserter = new ArrayAssertion(parsedJson, jsonPathBuffer, methodsBuffer);
		asserter.jsonPathBuffer.append("[*]");
		asserter.methodsBuffer.append(".array()");
		return asserter;
	}

	private static String stringWithEscapedQuotes(Object object) {
		String stringValue = object.toString();
		return stringValue.replaceAll("\"", "\\\\\"");
	}

	private static String stringWithEscapedSingleQuotes(Object object) {
		String stringValue = object.toString();
		return stringValue.replaceAll("'", "\\\\'");
	}

	protected class NamelessArrayHavingFieldAssertion extends FieldAssertion {
		protected NamelessArrayHavingFieldAssertion(DocumentContext parsedJson,
				StringBuffer jsonPathBuffer, StringBuffer methodsBuffer, Object fieldName) {
			super(parsedJson, jsonPathBuffer, methodsBuffer, fieldName);
		}

		public ArrayAssertion namlessArray() {
			ArrayAssertion asserter = new ArrayAssertion(parsedJson, jsonPathBuffer,
					methodsBuffer);
			asserter.jsonPathBuffer.append("[*]");
			asserter.methodsBuffer.append(".array()");
			return asserter;
		}

	}

	protected class FieldAssertion extends Asserter {
		protected FieldAssertion( DocumentContext parsedJson,
				StringBuffer jsonPathBuffer, StringBuffer methodsBuffer, Object fieldName) {
			super(parsedJson, jsonPathBuffer, methodsBuffer, fieldName);
		}
	}

	protected class ArrayAssertion extends Asserter {
		protected ArrayAssertion(DocumentContext parsedJson,
				StringBuffer jsonPathBuffer, StringBuffer methodsBuffer, Object arrayName) {
			super(parsedJson, jsonPathBuffer, methodsBuffer, arrayName);
		}

		protected ArrayAssertion(DocumentContext parsedJson,
				StringBuffer jsonPathBuffer, StringBuffer methodsBuffer) {
			super(parsedJson, jsonPathBuffer, methodsBuffer, null);
		}

		@Override
		public FieldAssertion fieldBeforeMatching(final Object value) {
			FieldAssertion asserter = new FieldAssertion(parsedJson, jsonPathBuffer,
					methodsBuffer, value);
			asserter.methodsBuffer.append(".contains(").append(wrapValueWithQuotes(value))
					.append(")");
			return asserter;
		}

		public ArrayAssertion namlessArray() {
			ArrayAssertion asserter = new ArrayAssertion(parsedJson, jsonPathBuffer,
					methodsBuffer);
			asserter.jsonPathBuffer.append("[*]");
			asserter.methodsBuffer.append(".array()");
			return asserter;
		}

		public ArrayAssertion iterationPassingArray() {
			ArrayAssertion asserter = new ArrayAssertion(parsedJson, jsonPathBuffer,
					methodsBuffer);
			return asserter;
		}

	}

	protected class ArrayValueAssertion extends FieldAssertion {
		protected ArrayValueAssertion(DocumentContext parsedJson,
				StringBuffer jsonPathBuffer, StringBuffer methodsBuffer, Object arrayName) {
			super(parsedJson, jsonPathBuffer, methodsBuffer, arrayName);
		}

		@Override
		public ArrayValueAssertion contains(Object value) {
			ArrayValueAssertion asserter = new ArrayValueAssertion(parsedJson, jsonPathBuffer,
					methodsBuffer, value);
			asserter.methodsBuffer.append(".contains(").append(wrapValueWithQuotes(value))
					.append(")");
			return asserter;
		}

		@Override
		public ReadyToCheck isEqualTo(String value) {
			ReadyToCheck readyToCheck = new ReadyToCheck(parsedJson, jsonPathBuffer, methodsBuffer, fieldName);
			readyToCheck.jsonPathBuffer.append("[?(@ == ").append(wrapValueWithSingleQuotes(value)).append(")]");
			readyToCheck.methodsBuffer.append(".value()");
			return readyToCheck;
		}

		@Override
		public ReadyToCheck isEqualTo(Number value) {
			ReadyToCheck readyToCheck = new ReadyToCheck(parsedJson, jsonPathBuffer, methodsBuffer, fieldName);
			readyToCheck.jsonPathBuffer.append("[?(@ == ").append(String.valueOf(value)).append(")]");
			readyToCheck.methodsBuffer.append(".value()");
			return readyToCheck;
		}

		@Override
		public ReadyToCheck matches(String value) {
			ReadyToCheck readyToCheck = new ReadyToCheck(parsedJson, jsonPathBuffer, methodsBuffer, fieldName);
			readyToCheck.jsonPathBuffer.append("[?(@ =~ /").append(value).append("/)]");
			readyToCheck.methodsBuffer.append(".value()");
			return readyToCheck;
		}

		@Override
		public ReadyToCheck isEqualTo(Boolean value) {
			ReadyToCheck readyToCheck = new ReadyToCheck(parsedJson, jsonPathBuffer, methodsBuffer, fieldName);
			readyToCheck.jsonPathBuffer.append("[?(@ == ").append(String.valueOf(value)).append(")]");
			readyToCheck.methodsBuffer.append(".value()");
			return readyToCheck;
		}

	}

	public class Asserter {

		protected final DocumentContext parsedJson;
		protected final StringBuffer jsonPathBuffer;
		protected final StringBuffer methodsBuffer;
		protected final Object fieldName;

		protected Asserter(DocumentContext parsedJson,
				StringBuffer jsonPathBuffer, StringBuffer methodsBuffer, Object fieldName) {
			this.parsedJson = parsedJson;
			this.jsonPathBuffer = new StringBuffer(jsonPathBuffer.toString());
			this.methodsBuffer = new StringBuffer(methodsBuffer.toString());
			this.fieldName = fieldName;
		}

		public FieldAssertion contains(final Object value) {
			FieldAssertion asserter = new FieldAssertion(parsedJson, jsonPathBuffer,
					methodsBuffer, value);
			asserter.methodsBuffer.append(".contains(").append(wrapValueWithQuotes(value))
					.append(")");
			return asserter;
		}

		protected String wrapValueWithQuotes(Object value) {
			return value instanceof String ? "\"" + stringWithEscapedQuotes(value) + "\"" :
					value.toString();
		}

		protected String wrapValueWithSingleQuotes(Object value) {
			return value instanceof String ? "'" + stringWithEscapedSingleQuotes(value) + "'" :
					value.toString();
		}

		public FieldAssertion field(final Object value) {
			FieldAssertion asserter = new FieldAssertion(parsedJson, jsonPathBuffer,
					methodsBuffer, value);
			asserter.jsonPathBuffer.append(".").append(String.valueOf(value));
			asserter.methodsBuffer.append(".field(").append(wrapValueWithQuotes(value)).append(")");
			return asserter;
		}

		public FieldAssertion fieldBeforeMatching(final Object value) {
			FieldAssertion asserter = new FieldAssertion(parsedJson, jsonPathBuffer,
					methodsBuffer, value);
			asserter.methodsBuffer.append(".field(").append(wrapValueWithQuotes(value)).append(")");
			return asserter;
		}

		public ArrayAssertion array(final Object value) {
			ArrayAssertion asserter = new ArrayAssertion(parsedJson, jsonPathBuffer,
					methodsBuffer, value);
			asserter.jsonPathBuffer.append(".").append(String.valueOf(value))
					.append("[*]");
			asserter.methodsBuffer.append(".array(").append(wrapValueWithQuotes(value)).append(")");
			return asserter;
		}

		public ArrayValueAssertion arrayField(final Object value) {
			ArrayValueAssertion asserter = new ArrayValueAssertion(parsedJson, jsonPathBuffer,
					methodsBuffer, value);
			asserter.jsonPathBuffer.append(".").append(String.valueOf(value));
			asserter.methodsBuffer.append(".array(").append(wrapValueWithQuotes(value)).append(")");
			return asserter;
		}
		public ArrayValueAssertion namelessArrayField(final Object value) {
			return new ArrayValueAssertion(parsedJson, jsonPathBuffer,
					methodsBuffer, value);
		}

		public ReadyToCheck isEqualTo(String value) {
			if (value == null) {
				return isNull();
			}
			ReadyToCheck readyToCheck = new ReadyToCheck(parsedJson, jsonPathBuffer, methodsBuffer, fieldName);
			readyToCheck.jsonPathBuffer.append("[?(@.").append(String.valueOf(fieldName))
					.append(" == ").append(wrapValueWithSingleQuotes(value)).append(")]");
			readyToCheck.methodsBuffer.append(".isEqualTo(").append(wrapValueWithQuotes(value)).append(")");
			return readyToCheck;
		}

		public ReadyToCheck isEqualTo(Object value) {
			if (value == null) {
				return isNull();
			}
			return isEqualTo(value.toString());
		}

		public ReadyToCheck isEqualTo(Number value) {
			if (value == null) {
				return isNull();
			}
			ReadyToCheck readyToCheck = new ReadyToCheck(parsedJson, jsonPathBuffer, methodsBuffer, fieldName);
			readyToCheck.jsonPathBuffer.append("[?(@.").append(String.valueOf(fieldName))
					.append(" == ").append(value).append(")]");
			readyToCheck.methodsBuffer.append(".isEqualTo(").append(String.valueOf(value)).append(")");
			return readyToCheck;
		}

		public ReadyToCheck isNull() {
			ReadyToCheck readyToCheck = new ReadyToCheck(parsedJson, jsonPathBuffer, methodsBuffer, fieldName);
			readyToCheck.jsonPathBuffer.append("[?(@.").append(String.valueOf(fieldName))
					.append(" == null)]");
			readyToCheck.methodsBuffer.append(".isNull()");
			return readyToCheck;
		}

		public ReadyToCheck matches(String value) {
			if (value == null) {
				return isNull();
			}
			ReadyToCheck readyToCheck = new ReadyToCheck(parsedJson, jsonPathBuffer, methodsBuffer, fieldName);
			readyToCheck.jsonPathBuffer.append("[?(@.").append(String.valueOf(fieldName))
					.append(" =~ /").append(stringWithEscapedSingleQuotes(value)).append("/)]");
			readyToCheck.methodsBuffer.append(".matches(").append(wrapValueWithQuotes(value)).append(")");
			return readyToCheck;
		}

		public ReadyToCheck isEqualTo(Boolean value) {
			if (value == null) {
				return isNull();
			}
			ReadyToCheck readyToCheck = new ReadyToCheck(parsedJson, jsonPathBuffer, methodsBuffer, fieldName);
			readyToCheck.jsonPathBuffer.append("[?(@.").append(String.valueOf(fieldName))
					.append(" == ").append(String.valueOf(value)).append(")]");
			readyToCheck.methodsBuffer.append(".isEqualTo(").append(String.valueOf(value)).append(")");
			return readyToCheck;
		}

		public String methodWithAppendedCheck() {
			return methodsBuffer.append(".check();").toString();
		}

		public String jsonPath() {
			return jsonPathBuffer.toString();
		}

		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (!getClass().equals(o.getClass()))
				return false;
			Asserter asserter = (Asserter) o;
			if (!fieldName.equals(asserter.fieldName))
				return false;
			return jsonPathBuffer.equals(asserter.jsonPathBuffer) && methodsBuffer
					.equals(asserter.methodsBuffer);

		}

		public int hashCode() {
			int result;
			result = (parsedJson != null ? parsedJson.hashCode() : 0);
			result = 31 * result + (jsonPathBuffer != null ?
					jsonPathBuffer.hashCode() :
					0);
			result = 31 * result + (methodsBuffer != null ?
					methodsBuffer.hashCode() :
					0);
			result = 31 * result + (fieldName != null ? fieldName.hashCode() : 0);
			return result;
		}

		@Override
		public String toString() {
			return "\\nAsserter{\n    "
					+ "jsonPathBuffer=" + String.valueOf(jsonPathBuffer) + ",\n   "
					+ " methodsBuffer=" + String.valueOf(methodsBuffer) + "\n}";
		}
	}

	protected class ReadyToCheck extends Asserter {

		public ReadyToCheck(DocumentContext parsedJson, StringBuffer jsonPathBuffer,
				StringBuffer methodsBuffer, Object fieldName) {
			super(parsedJson, jsonPathBuffer, methodsBuffer, fieldName);
		}

		public void check() {
			assert !parsedJson.read(jsonPathBuffer.toString(), JSONArray.class).isEmpty();
		}
	}
}
