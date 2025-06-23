package org.yearup.configuration;

import org.apache.ibatis.jdbc.ScriptRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;

@Configuration
public class TestDatabaseConfig {

    private final DataSource mainDataSource;

    // Injecteer de DataSource die Spring Boot automatisch configureert
    // op basis van je application.properties.
    @Autowired
    public TestDatabaseConfig(DataSource dataSource) {
        this.mainDataSource = dataSource;
    }

    @Bean
    public DataSource dataSource() throws IOException, SQLException {
        // We gebruiken de auto-geconfigureerde DataSource om het script uit te voeren.
        // Dit zorgt ervoor dat we de juiste database opschonen.
        try (var connection = mainDataSource.getConnection()) {
            ScriptRunner runner = new ScriptRunner(connection);
            
            // Zorg ervoor dat test-data.sql is omgezet naar T-SQL!
            Reader reader = new BufferedReader(new FileReader(new ClassPathResource("test-data.sql").getFile()));
            
            runner.setLogWriter(null); // Optioneel: voorkom dat script output naar de console gaat
            runner.runScript(reader);
        }

        // Maak een specifieke DataSource voor tests die niet automatisch sluit en geen autocommit heeft.
        // Dit is nuttig om transacties na elke test terug te kunnen draaien.
        var testDataSource = new SingleConnectionDataSource();
        testDataSource.setUrl(((org.apache.commons.dbcp2.BasicDataSource) mainDataSource).getUrl());
        testDataSource.setUsername(((org.apache.commons.dbcp2.BasicDataSource) mainDataSource).getUsername());
        testDataSource.setPassword(((org.apache.commons.dbcp2.BasicDataSource) mainDataSource).getPassword());
        testDataSource.setAutoCommit(false);
        testDataSource.setSuppressClose(true);

        return testDataSource;
    }
}