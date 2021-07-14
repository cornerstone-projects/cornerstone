package com.example.demo.core.hibernate.domain;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.fasterxml.jackson.annotation.JsonView;

import lombok.Data;

@Data
public class ResultPage<T> {

	@JsonView(Pageable.class)
	private List<T> result;

	@JsonView(Pageable.class)
	private int pageNo;

	@JsonView(Pageable.class)
	private int pageSize;

	@JsonView(Pageable.class)
	private int totalPages;

	@JsonView(Pageable.class)
	private long totalElements;

	public static <E> ResultPage<E> of(Page<E> page) {
		ResultPage<E> resultPage = new ResultPage<>();
		resultPage.setResult(page.getContent());
		resultPage.setPageNo(page.getNumber() + 1);
		resultPage.setPageSize(page.getSize());
		resultPage.setTotalPages(page.getTotalPages());
		resultPage.setTotalElements(page.getTotalElements());
		return resultPage;
	}

}
