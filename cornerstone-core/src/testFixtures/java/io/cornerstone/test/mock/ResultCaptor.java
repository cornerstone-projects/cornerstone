package io.cornerstone.test.mock;

import java.util.function.UnaryOperator;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class ResultCaptor<T> implements Answer<T> {

	private T result;

	private final UnaryOperator<T> operator;

	public ResultCaptor() {
		this.operator = UnaryOperator.identity();
	}

	public ResultCaptor(UnaryOperator<T> operator) {
		this.operator = operator;
	}

	public T getResult() {
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T answer(InvocationOnMock invocationOnMock) throws Throwable {
		result = operator.apply((T) invocationOnMock.callRealMethod());
		return result;
	}

}