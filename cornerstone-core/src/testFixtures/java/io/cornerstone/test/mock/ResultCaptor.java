package io.cornerstone.test.mock;

import java.util.function.UnaryOperator;

import lombok.Getter;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class ResultCaptor<T> implements Answer<T> {

	@Getter
	private T result;

	private final UnaryOperator<T> operator;

	public ResultCaptor() {
		this.operator = UnaryOperator.identity();
	}

	public ResultCaptor(UnaryOperator<T> operator) {
		this.operator = operator;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T answer(InvocationOnMock invocationOnMock) throws Throwable {
		this.result = this.operator.apply((T) invocationOnMock.callRealMethod());
		return this.result;
	}

}
