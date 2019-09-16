package org.unidue.ub.libintel.getter.getter;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class MABGetterFactory implements FactoryBean<MABGetter> {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public MABGetter getObject() {
        return new MABGetter(jdbcTemplate);
    }

    @Override
    public Class<?> getObjectType() {
        return MABGetter.class;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }
}
