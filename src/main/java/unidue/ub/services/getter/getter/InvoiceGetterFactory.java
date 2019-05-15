package unidue.ub.services.getter.getter;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class InvoiceGetterFactory implements FactoryBean<InvoiceGetter> {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public InvoiceGetter getObject() {
        return new InvoiceGetter(jdbcTemplate);
    }

    @Override
    public Class<?> getObjectType() {
        return InvoiceGetter.class;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }
}
