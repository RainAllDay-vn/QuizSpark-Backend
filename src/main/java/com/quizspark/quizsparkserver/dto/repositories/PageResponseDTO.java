package com.quizspark.quizsparkserver.dto.repositories;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

@Getter
@Setter
public class PageResponseDTO<T, R> {
    private final List<R> content;
    private final int pageNumber;
    private final int pageSize;
    private final long totalElements;
    private final int totalPages;

    public PageResponseDTO(Page<T> page, Function<T, R> converter) {
        this.content = page.getContent().stream().map(converter).toList();
        this.pageNumber = page.getNumber();
        this.pageSize = page.getSize();
        this.totalElements = page.getTotalElements();
        this.totalPages = page.getTotalPages();
    }
}
