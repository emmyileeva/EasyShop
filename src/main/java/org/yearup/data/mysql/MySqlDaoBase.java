package org.yearup.data.mysql;

import org.springframework.beans.factory.annotation.Autowired;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public abstract class MySqlDaoBase
{
    private final DataSource dataSource;

    // Gebruik @Autowired op de constructor om de auto-geconfigureerde DataSource te ontvangen
    @Autowired
    public MySqlDaoBase(DataSource dataSource)
    {
        this.dataSource = dataSource;
    }

    protected Connection getConnection() throws SQLException
    {
        return dataSource.getConnection();
    }
}