package config;

import org.hibernate.jpa.HibernatePersistenceProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@Configuration
public class DatabaseConfig {
    @Bean
    @Primary
    @Qualifier("datasource")
    @ConfigurationProperties(prefix="spring.datasource")
    public DataSource dataSource(){
        return DataSourceBuilder.create().build();
    }

    @Bean
    @Qualifier("universityDatasource")
    @ConfigurationProperties(prefix="spring.universitydatasource")
    public DataSource universityDataSource(){
        return DataSourceBuilder.create().build();
    }

    @Bean
    @Qualifier("sqlDatasource")
    @ConfigurationProperties(prefix="spring.sql-datasource")
    public DataSource sqlDataSource(){
        return DataSourceBuilder.create().build();
    }

    @Bean
    @Qualifier("postgresEntityManagerFactory")
    public EntityManagerFactory postgresEntityManagerFactory(){
        LocalContainerEntityManagerFactoryBean lem=
                new LocalContainerEntityManagerFactoryBean();

        lem.setDataSource(universityDataSource());
        lem.setPackagesToScan("model");
        lem.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        lem.setPersistenceProviderClass(HibernatePersistenceProvider.class);
        lem.afterPropertiesSet();

        return lem.getObject();
    }

    @Bean
    @Primary
    public EntityManagerFactory sqlEntityManagerFactory(){
        LocalContainerEntityManagerFactoryBean lem=
                new LocalContainerEntityManagerFactoryBean();

        lem.setDataSource(sqlDataSource());
        lem.setPackagesToScan("model");
        lem.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        lem.setPersistenceProviderClass(HibernatePersistenceProvider.class);
        lem.afterPropertiesSet();

        return lem.getObject();
    }

    @Bean
    @Primary
    public JpaTransactionManager jpaTransactionManager() {
        JpaTransactionManager jpaTransactionManager = new
                JpaTransactionManager();

        jpaTransactionManager.setDataSource(universityDataSource());
        jpaTransactionManager.setEntityManagerFactory(sqlEntityManagerFactory());

        return jpaTransactionManager;
    }

}
