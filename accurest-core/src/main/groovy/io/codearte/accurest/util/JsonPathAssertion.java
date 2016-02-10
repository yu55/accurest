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

	protected RootFieldAssertion root() {
		Asserter asserter = new RootFieldAssertion(parsedJson, jsonPathBuffer,
				methodsBuffer, "");
		asserter.jsonPathBuffer.append("$");
		return ((RootFieldAssertion) (asserter));
	}

	public void matchesJsonPath(String jsonPath) {
		assert !parsedJson.read(jsonPath, JSONArray.class).isEmpty();
	}

	public FieldAssertion field(Object value) {
		Asserter asserter = new FieldAssertion(parsedJson, jsonPathBuffer, methodsBuffer,
				value);
		asserter.field(value);
		return ((FieldAssertion) (asserter));
	}

	public ArrayAssertion array() {
		Asserter asserter = new ArrayAssertion(parsedJson, jsonPathBuffer, methodsBuffer);
		asserter.jsonPathBuffer.append("[*]");
		asserter.methodsBuffer.append(".array()");
		return ((ArrayAssertion) (asserter));
	}

	protected class RootFieldAssertion extends FieldAssertion {
		protected RootFieldAssertion(DocumentContext parsedJson,
				StringBuffer jsonPathBuffer, StringBuffer methodsBuffer, Object fieldName) {
			super(parsedJson, jsonPathBuffer, methodsBuffer, fieldName);
		}

		public ArrayAssertion array() {
			Asserter asserter = new ArrayAssertion(parsedJson, jsonPathBuffer,
					methodsBuffer);
			asserter.jsonPathBuffer.append("[*]");
			asserter.methodsBuffer.append(".array()");
			return ((ArrayAssertion) (asserter));
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
			Asserter asserter = new FieldAssertion(parsedJson, jsonPathBuffer,
					methodsBuffer, value);
			asserter.methodsBuffer.append(".contains(").append(wrapValue(value))
					.append(")");
			return ((FieldAssertion) (asserter));
		}

	}

	protected class ArrayValueAssertion extends FieldAssertion {
		protected ArrayValueAssertion(DocumentContext parsedJson,
				StringBuffer jsonPathBuffer, StringBuffer methodsBuffer, Object arrayName) {
			super(parsedJson, jsonPathBuffer, methodsBuffer, arrayName);
		}

		@Override
		public ArrayValueAssertion contains(final Object value) {
			Asserter asserter = new ArrayValueAssertion(parsedJson, jsonPathBuffer,
					methodsBuffer, value);
			asserter.methodsBuffer.append(".contains(").append(wrapValue(value))
					.append(")");
			return ((ArrayValueAssertion) (asserter));
		}

		public Asserter value(String value) {
			jsonPathBuffer.append("[?(@ == \'").append(value).append("\')]");
			methodsBuffer.append(".value()");
			return this;
		}

		@Override
		public Asserter isEqualTo(String value) {
			jsonPathBuffer.append("[?(@ == \'").append(value).append("\')]");
			methodsBuffer.append(".value()");
			return this;
		}

		@Override
		public Asserter isEqualTo(Number value) {
			jsonPathBuffer.append("[?(@ == ").append(String.valueOf(value)).append(")]");
			methodsBuffer.append(".value()");
			return this;
		}

		@Override
		public Asserter matches(String value) {
			jsonPathBuffer.append("[?(@ =~ /").append(value).append("/)]");
			methodsBuffer.append(".value()");
			return this;
		}

		@Override
		public Asserter isEqualTo(Boolean value) {
			jsonPathBuffer.append("[?(@ == ").append(String.valueOf(value)).append(")]");
			methodsBuffer.append(".value()");
			return this;
		}

	}

	public class Asserter {
		protected Asserter(DocumentContext parsedJson,
				StringBuffer jsonPathBuffer, StringBuffer methodsBuffer, Object fieldName) {
			this.parsedJson = parsedJson;
			this.jsonPathBuffer = new StringBuffer(jsonPathBuffer.toString());
			this.methodsBuffer = new StringBuffer(methodsBuffer.toString());
			this.fieldName = fieldName;
		}

		public FieldAssertion contains(final Object value) {
			Asserter asserter = new FieldAssertion(parsedJson, jsonPathBuffer,
					methodsBuffer, value);
			asserter.methodsBuffer.append(".contains(").append(wrapValue(value))
					.append(")");
			return ((FieldAssertion) (asserter));
		}

		public String wrapValue(Object value) {
			return value instanceof String ? "\'" + value + "\'" : value.toString();
		}

		public FieldAssertion field(final Object value) {
			Asserter asserter = new FieldAssertion(parsedJson, jsonPathBuffer,
					methodsBuffer, value);
			asserter.jsonPathBuffer.append(".").append(String.valueOf(value));
			asserter.methodsBuffer.append(".field(").append(wrapValue(value)).append(")");
			return ((FieldAssertion) (asserter));
		}

		public FieldAssertion fieldBeforeMatching(final Object value) {
			Asserter asserter = new FieldAssertion(parsedJson, jsonPathBuffer,
					methodsBuffer, value);
			asserter.methodsBuffer.append(".field(").append(wrapValue(value)).append(")");
			return ((FieldAssertion) (asserter));
		}

		public ArrayAssertion array(final Object value) {
			Asserter asserter = new ArrayAssertion(parsedJson, jsonPathBuffer,
					methodsBuffer, value);
			asserter.jsonPathBuffer.append(".").append(String.valueOf(value))
					.append("[*]");
			asserter.methodsBuffer.append(".array(").append(wrapValue(value)).append(")");
			return ((ArrayAssertion) (asserter));
		}

		public ArrayValueAssertion arrayField(final Object value) {
			Asserter asserter = new ArrayValueAssertion(parsedJson, jsonPathBuffer,
					methodsBuffer, value);
			asserter.jsonPathBuffer.append(".").append(String.valueOf(value));
			asserter.methodsBuffer.append(".array(").append(wrapValue(value)).append(")");
			return ((ArrayValueAssertion) (asserter));
		}

		public Asserter isEqualTo(String value) {
			if (value == null) {
				return isNull();
			}
			jsonPathBuffer.append("[?(@.").append(String.valueOf(fieldName))
					.append(" == \'").append(value).append("\')]");
			methodsBuffer.append(".isEqualTo(\'\'\'").append(value).append("\'\'\')");
			return this;
		}

		public Asserter isEqualTo(Object value) {
			if (value == null) {
				return isNull();
			}
			return isEqualTo(value.toString());
		}

		public Asserter isEqualTo(Number value) {
			if (value == null) {
				return isNull();
			}
			jsonPathBuffer.append("[?(@.").append(String.valueOf(fieldName))
					.append(" == ").append(String.valueOf(value)).append(")]");
			methodsBuffer.append(".isEqualTo(").append(String.valueOf(value)).append(")");
			return this;
		}

		public Asserter isNull() {
			jsonPathBuffer.append("[?(@.").append(String.valueOf(fieldName))
					.append(" == null)]");
			methodsBuffer.append(".isNull()");
			return this;
		}

		public Asserter matches(String value) {
			if (value == null) {
				return isNull();
			}
			jsonPathBuffer.append("[?(@.").append(String.valueOf(fieldName))
					.append(" =~ /").append(value).append("/)]");
			methodsBuffer.append(".matches(\'\'\'").append(value).append("\'\'\')");
			return this;
		}

		public Asserter isEqualTo(Boolean value) {
			if (value == null) {
				return isNull();
			}
			jsonPathBuffer.append("[?(@.").append(String.valueOf(fieldName))
					.append(" == ").append(String.valueOf(value)).append(")]");
			methodsBuffer.append(".isEqualTo(").append(String.valueOf(value)).append(")");
			return this;
		}

		public String methodWithAppendedCheck() {
			return methodsBuffer.append(".check()").toString();
		}

		public String jsonPath() {
			return jsonPathBuffer.toString();
		}

		public void check() {
			assert !parsedJson.read(jsonPathBuffer.toString(), JSONArray.class).isEmpty();
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

		protected final DocumentContext parsedJson;
		protected final StringBuffer jsonPathBuffer;
		protected final StringBuffer methodsBuffer;
		protected final Object fieldName;
	}
}
