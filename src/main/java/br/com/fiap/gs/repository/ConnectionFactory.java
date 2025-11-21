package br.com.fiap.gs.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionFactory {

    private String user = "RM561342";
    private String pwd = "110706";
    private String jdbc = "jdbc:oracle:thin:@oracle.fiap.com.br:1521:orcl";

    public Connection getConnection() throws SQLException {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(jdbc, user, pwd);
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
        return conn;
    }
}
