package com.spribe.exchanger.utill;

import org.springframework.data.jpa.domain.Specification;

public class RequestSpecificationBuilder {

    public static Specification<String> buildSpecification(String filter, String sortBy, String direction) {
        return (root, query, criteriaBuilder) -> {
            Specification<String> predicate = Specification.where(null);

            if (filter != null && !filter.isEmpty()) {
                predicate = predicate.and((r, q, cb) ->
                        cb.like(cb.lower(r), "%" + filter.toLowerCase() + "%"));
            }

            if (sortBy != null && !sortBy.isEmpty()) {
                if ("desc".equalsIgnoreCase(direction)) {
                    query.orderBy(criteriaBuilder.desc(root.get(sortBy)));
                } else {
                    query.orderBy(criteriaBuilder.asc(root.get(sortBy)));
                }
            }

            return predicate.toPredicate(root, query, criteriaBuilder);
        };
    }

}
