package com.blogspot.toomuchcoding.jsonpathassert;

import com.jayway.jsonpath.DocumentContext;

import net.minidev.json.JSONArray;

class ReadyToCheckAsserter extends JsonPathAsserter {

	public ReadyToCheckAsserter(DocumentContext parsedJson, StringBuffer jsonPathBuffer,
			StringBuffer methodsBuffer, Object fieldName) {
		super(parsedJson, jsonPathBuffer, methodsBuffer, fieldName);
	}

	public void check() {
		assert !parsedJson.read(jsonPathBuffer.toString(), JSONArray.class).isEmpty();
	}

	@Override
	public boolean isReadyToCheck() {
		return true;
	}
}