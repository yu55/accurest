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

	private JsonPathAssertion(DocumentContext parsedJson) {
		this.parsedJson = parsedJson;
	}

	public static JsonPathAssertion assertThat(String body) {
		DocumentContext parsedJson = JsonPath.parse(body);
		return new JsonPathAssertion(parsedJson);
	}

	public static JsonPathVerifiable root(String body) {
		JsonPathAssertion jsonPathAssertion = assertThat(body);
		return jsonPathAssertion.root();
	}

	protected JsonPathVerifiable root() {
		NamelessArrayHavingFieldAssertion asserter = new NamelessArrayHavingFieldAssertion(parsedJson, jsonPathBuffer, "");
		asserter.jsonPathBuffer.append("$");
		return asserter;
	}

	public void matchesJsonPath(String jsonPath) {
		assert !parsedJson.read(jsonPath, JSONArray.class).isEmpty();
	}

	public JsonPathVerifiable field(Object value) {
		FieldAssertion asserter = new FieldAssertion(parsedJson, jsonPathBuffer,
				value);
		asserter.field(value);
		return asserter;
	}

	public JsonPathVerifiable array() {
		ArrayAssertion asserter = new ArrayAssertion(parsedJson, jsonPathBuffer);
		asserter.jsonPathBuffer.append("[*]");
		return asserter;
	}

}
