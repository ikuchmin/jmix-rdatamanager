package ru.udya.rdatamanager;

import io.jmix.core.annotation.JmixModule;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import javax.sql.DataSource;

@SpringBootConfiguration
@EnableAutoConfiguration
@Import(RdmConfiguration.class)
@PropertySource("classpath:/ru/udya/rdatamanager/test-app.properties")
@JmixModule(id = "ru.udya.rdatamanager.test", dependsOn = RdmConfiguration.class)
public class RdmTestConfiguration {

//    @Bean
//    @Primary
//    DataSource dataSource() {
//        return new EmbeddedDatabaseBuilder()
//                .generateUniqueName(true)
////                .setType(EmbeddedDatabaseType.HSQL)
//                .setType(EmbeddedDatabaseType.HSQL)
//                .build();
//    }


    private static final String DATABASE_URL = "jdbc:postgresql://localhost:5432/rdb";
    private static final String DATABASE_USERNAME = "erp";
    private static final String DATABASE_PASSWORD = "12345";

    @Bean
    @Primary
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl(DATABASE_URL);
        dataSource.setUsername(DATABASE_USERNAME);
        dataSource.setPassword(DATABASE_PASSWORD);
        return dataSource;
    }
}
