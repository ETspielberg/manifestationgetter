package unidue.ub.services.getter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import unidue.ub.services.getter.getter.*;

@Configuration
public class GetterConfiguration {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public GetterConfiguration(JdbcTemplate jdbcTemplate) {this.jdbcTemplate = jdbcTemplate;}

    @Bean
    InvoiceGetter invoiceGetter() {return new InvoiceGetter(jdbcTemplate);}

    @Bean
    OrderGetter orderGetter() {return new OrderGetter(jdbcTemplate);}

    @Bean
    ManifestationGetter manifestationGetter() {return new ManifestationGetter(jdbcTemplate);}

    @Bean
    EventGetter eventGetter() {return new EventGetter(jdbcTemplate);}

    @Bean
    ItemGetter itemGetter() {return new ItemGetter(jdbcTemplate);}

    @Bean
    MABGetter mabGetter() {return new MABGetter(jdbcTemplate);}

    @Bean
    JournalInfoGetter journalInfoGetter() {return new JournalInfoGetter(jdbcTemplate);}
}
