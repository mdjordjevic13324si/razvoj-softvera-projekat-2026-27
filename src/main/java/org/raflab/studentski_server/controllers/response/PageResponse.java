package org.raflab.studentski_server.controllers.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/*
 * Stabilan JSON format za stranicene rezultate:
 * {"content":[...], "page":{"size":10, "number":0, "totalElements":42, "totalPages":5}}
 */
@Data
@AllArgsConstructor
public class PageResponse<T> {

    private List<T> content;
    private PageMetadata page;

    @Data
    @AllArgsConstructor
    public static class PageMetadata {
        private int size;
        private int number;
        private long totalElements;
        private int totalPages;
    }

    public static <E, T> PageResponse<T> of(Page<E> page, Function<E, T> converter) {
        return new PageResponse<>(
                page.getContent().stream().map(converter).toList(),
                new PageMetadata(page.getSize(), page.getNumber(), page.getTotalElements(), page.getTotalPages()));
    }
}
