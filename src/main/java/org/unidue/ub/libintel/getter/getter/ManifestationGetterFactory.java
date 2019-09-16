package org.unidue.ub.libintel.getter.getter;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class ManifestationGetterFactory implements FactoryBean<ManifestationGetter> {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public ManifestationGetter getObject() {
        return new ManifestationGetter(jdbcTemplate);
    }

    @Override
    public Class<?> getObjectType() {
        return ManifestationGetter.class;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }
}
