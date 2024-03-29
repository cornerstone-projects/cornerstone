package io.cornerstone.core.domain;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Data
public class ResultPage<T> {

	@JsonView(Pageable.class)
	private List<T> result;

	@JsonView(Pageable.class)
	private int page;

	@JsonView(Pageable.class)
	private int size;

	@JsonView(Pageable.class)
	private int totalPages;

	@JsonView(Pageable.class)
	private long totalElements;

	public static <E> ResultPage<E> of(Page<E> page) {
		ResultPage<E> resultPage = new ResultPage<>();
		resultPage.setResult(page.getContent());
		resultPage.setPage(page.getNumber() + 1);
		resultPage.setSize(page.getSize());
		resultPage.setTotalPages(page.getTotalPages());
		resultPage.setTotalElements(page.getTotalElements());
		return resultPage;
	}

}
