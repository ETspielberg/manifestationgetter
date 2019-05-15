package unidue.ub.services.getter.getter;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class ItemGetterFactory implements FactoryBean<ItemGetter> {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public ItemGetter getObject() {
        return new ItemGetter(jdbcTemplate);
    }

    @Override
    public Class<?> getObjectType() {
        return ItemGetter.class;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }
}
