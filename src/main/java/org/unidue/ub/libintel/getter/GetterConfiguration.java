package org.unidue.ub.libintel.getter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.unidue.ub.libintel.getter.getter.*;

import javax.sql.DataSource;

@Configuration
public class GetterConfiguration extends WebSecurityConfigurerAdapter {
    

    @Bean
    InvoiceGetter invoiceGetter() {return new InvoiceGetter(jdbcTemplate());}

    @Bean
    OrderGetter orderGetter() {return new OrderGetter(jdbcTemplate());}

    @Bean
    ManifestationGetter manifestationGetter() {return new ManifestationGetter(jdbcTemplate());}

    @Bean
    EventGetter eventGetter() {return new EventGetter(jdbcTemplate());}

    @Bean
    ItemGetter itemGetter() {return new ItemGetter(jdbcTemplate());}

    @Bean
    MABGetter mabGetter() {return new MABGetter(jdbcTemplate());}

    @Bean
    JournalInfoGetter journalInfoGetter() {return new JournalInfoGetter(jdbcTemplate());}

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.httpBasic().disable();
        http.authorizeRequests()
                .anyRequest().hasIpAddress("::1")
                .anyRequest().authenticated()
                .anyRequest().permitAll();
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.aleph")
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    public JdbcTemplate jdbcTemplate() {
        return new JdbcTemplate(dataSource());
    }
}
