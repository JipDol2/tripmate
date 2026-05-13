package com.tripmate.post;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CompanionPurposeMigration implements ApplicationRunner {
    private final JdbcTemplate jdbcTemplate;

    public CompanionPurposeMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!legacyPurposeColumnExists()) {
            return;
        }

        jdbcTemplate.update("""
                insert into post_purposes (post_id, purpose)
                select cp.id, upper(cp.purpose)
                from companion_posts cp
                where cp.purpose is not null
                  and not exists (
                      select 1
                      from post_purposes pp
                      where pp.post_id = cp.id
                  )
                  and upper(cp.purpose) in ('FOOD', 'CAFE', 'TOUR', 'SHOPPING', 'NATURE', 'PHOTO', 'CULTURE', 'ACTIVITY', 'NIGHTLIFE', 'RELAXATION')
                """);

        jdbcTemplate.execute("alter table companion_posts drop column purpose");
    }

    private boolean legacyPurposeColumnExists() {
        Boolean exists = jdbcTemplate.queryForObject("""
                select exists (
                    select 1
                    from information_schema.columns
                    where table_name = 'companion_posts'
                      and column_name = 'purpose'
                )
                """, Boolean.class);
        return Boolean.TRUE.equals(exists);
    }
}
