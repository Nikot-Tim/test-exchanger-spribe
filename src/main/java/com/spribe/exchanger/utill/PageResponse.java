package com.spribe.exchanger.utill;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Data;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Data
public class PageResponse<T> {

    public static final String DEFAULT_PAGE_SIZE = "2147483647";

    @JsonView({Pagination.class})
    private List<T> content;

    @JsonView({Pagination.class})
    private long perPage;

    @JsonView({Pagination.class})
    private long currentPage;

    @JsonView({Pagination.class})
    private long totalRecords;

    public PageResponse(List<T> content, Pageable pageable, Long totalRecords) {
        this.content = content;
        this.perPage = pageable.getPageSize();
        this.currentPage = pageable.getPageNumber();
        this.totalRecords = totalRecords;
    }

    public static <T> PageResponse<T> of(List<T> content, Pageable pageable, Long totalRecords) {
        return new PageResponse<>(content, pageable, totalRecords);
    }

    public interface Pagination {

    }
}