package org.unidue.ub.libintel.getter.getter;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class JournalInfoGetterFactory implements FactoryBean<JournalInfoGetter> {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public JournalInfoGetter getObject() {
        return new JournalInfoGetter(jdbcTemplate);
    }

    @Override
    public Class<?> getObjectType() {
        return JournalInfoGetter.class;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }
}
