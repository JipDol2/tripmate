package com.tripmate.post;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;

import static com.tripmate.location.QCity.city;
import static com.tripmate.location.QCountry.country;
import static com.tripmate.post.QCompanionPost.companionPost;
import static com.tripmate.user.QUser.user;

public class CompanionPostRepositoryImpl implements CompanionPostRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    public CompanionPostRepositoryImpl(EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    @Override
    public List<CompanionPost> search(String countryCode, String cityCode, LocalDate startDate, LocalDate endDate,
                                      List<CompanionPurpose> purposes) {
        return queryFactory
                .selectFrom(companionPost)
                .distinct()
                .join(companionPost.author, user).fetchJoin()
                .leftJoin(city).on(companionPost.cityCode.eq(city.code))
                .leftJoin(city.country, country)
                .where(
                        countryCodeEq(countryCode),
                        cityCodeEq(cityCode),
                        companionDateEq(startDate, endDate),
                        purposesIn(purposes)
                )
                .orderBy(companionPost.createdAt.desc())
                .fetch();
    }

    private BooleanExpression countryCodeEq(String countryCode) {
        if (countryCode == null || countryCode.isBlank()) {
            return null;
        }
        return country.code.eq(countryCode);
    }

    private BooleanExpression cityCodeEq(String cityCode) {
        if (cityCode == null || cityCode.isBlank()) {
            return null;
        }
        return companionPost.cityCode.eq(cityCode);
    }

    private BooleanExpression companionDateEq(LocalDate startDate, LocalDate endDate) {
        if (startDate == null && endDate == null) {
            return null;
        }
        if (startDate != null && endDate != null) {
            return companionPost.startDate.loe(endDate)
                    .and(companionPost.endDate.coalesce(companionPost.startDate).goe(startDate));
        }
        if (startDate != null) {
            return companionPost.endDate.coalesce(companionPost.startDate).goe(startDate);
        }
        return companionPost.startDate.loe(endDate);
    }

    private BooleanExpression purposesIn(List<CompanionPurpose> purposes) {
        if (purposes == null || purposes.isEmpty()) {
            return null;
        }
        return companionPost.purposes.any().in(purposes);
    }
}
