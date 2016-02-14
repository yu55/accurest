package io.codearte.accurest.util;

import com.blogspot.toomuchcoding.jsonpathassert.JsonPathVerifiable;

/**
 * @author Marcin Grzejszczak
 */
public interface MethodBufferingJsonPathVerifiable extends JsonPathVerifiable, MethodBuffering, MethodBufferingReadyToCheck {
	@Override
	MethodBufferingJsonPathVerifiable contains(Object value);

	@Override
	MethodBufferingJsonPathVerifiable field(Object value);

	@Override
	MethodBufferingJsonPathVerifiable fieldBeforeMatching(Object value);

	@Override
	MethodBufferingJsonPathVerifiable array(Object value);

	@Override
	MethodBufferingJsonPathVerifiable arrayField(Object value);

	@Override
	MethodBufferingJsonPathVerifiable namelessArrayField(Object value);

	@Override
	MethodBufferingJsonPathVerifiable namelessArray();

	@Override
	MethodBufferingJsonPathVerifiable iterationPassingArray();

	@Override
	MethodBufferingJsonPathVerifiable isEqualTo(String value);

	@Override
	MethodBufferingJsonPathVerifiable isEqualTo(Object value);

	@Override
	MethodBufferingJsonPathVerifiable isEqualTo(Number value);

	@Override
	MethodBufferingJsonPathVerifiable isNull();

	@Override
	MethodBufferingJsonPathVerifiable matches(String value);

	@Override
	MethodBufferingJsonPathVerifiable isEqualTo(Boolean value);
}
