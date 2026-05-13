package com.tripmate.config;

import com.p6spy.engine.spy.appender.MessageFormattingStrategy;
import org.hibernate.engine.jdbc.internal.FormatStyle;

public class P6SpyPrettySqlFormatter implements MessageFormattingStrategy {

    @Override
    public String formatMessage(int connectionId, String now, long elapsed, String category, String prepared, String sql, String url) {
        if (sql == null || sql.trim().isEmpty()) {
            return "";
        }

        String formattedSql = FormatStyle.BASIC.getFormatter().format(sql).trim();
        return "took " + elapsed + "ms | " + category + System.lineSeparator() + formattedSql;
    }
}
