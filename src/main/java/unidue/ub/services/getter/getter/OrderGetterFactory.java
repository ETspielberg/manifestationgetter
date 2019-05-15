package unidue.ub.services.getter.getter;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderGetterFactory implements FactoryBean<OrderGetter> {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public OrderGetter getObject() {
        return new OrderGetter(jdbcTemplate);
    }

    @Override
    public Class<?> getObjectType() {
        return OrderGetter.class;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }
}
