package org.unidue.ub.libintel.getter.getter;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class EventGetterFactory implements FactoryBean<EventGetter> {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public EventGetter getObject() {
        return new EventGetter(jdbcTemplate);
    }

    @Override
    public Class<?> getObjectType() {
        return EventGetter.class;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }
}
