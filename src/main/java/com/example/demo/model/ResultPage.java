package com.example.demo.model;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.domain.Page;

import lombok.Data;

@Data
public class ResultPage<T> implements Serializable {

	private static final long serialVersionUID = 348675837851343164L;

	private List<T> result;

	private int pageNo;

	private int pageSize;

	private int totalPages;

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
