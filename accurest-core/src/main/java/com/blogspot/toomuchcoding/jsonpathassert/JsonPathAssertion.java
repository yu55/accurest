package com.blogspot.toomuchcoding.jsonpathassert;

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

	private JsonPathAssertion(DocumentContext parsedJson) {
		this.parsedJson = parsedJson;
	}

	public static JsonPathAssertion assertThat(String body) {
		DocumentContext parsedJson = JsonPath.parse(body);
		return new JsonPathAssertion(parsedJson);
	}

	public static JsonPathAsserter root(String body) {
		JsonPathAssertion jsonPathAssertion = assertThat(body);
		return jsonPathAssertion.root();
	}

	protected JsonPathAsserter root() {
		NamelessArrayHavingFieldAssertion asserter = new NamelessArrayHavingFieldAssertion(parsedJson, jsonPathBuffer,
				methodsBuffer, "");
		asserter.jsonPathBuffer.append("$");
		return asserter;
	}

	public void matchesJsonPath(String jsonPath) {
		assert !parsedJson.read(jsonPath, JSONArray.class).isEmpty();
	}

	public JsonPathAsserter field(Object value) {
		FieldAssertion asserter = new FieldAssertion(parsedJson, jsonPathBuffer, methodsBuffer,
				value);
		asserter.field(value);
		return asserter;
	}

	public JsonPathAsserter array() {
		ArrayAssertion asserter = new ArrayAssertion(parsedJson, jsonPathBuffer, methodsBuffer);
		asserter.jsonPathBuffer.append("[*]");
		asserter.methodsBuffer.append(".array()");
		return asserter;
	}

}
