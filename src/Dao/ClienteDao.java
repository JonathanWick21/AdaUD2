package Dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ClienteDao implements AutoCloseable{
    private final Connection connection;
    private static final String url = "jbdc:mysql://localhost:3306/ada2punto1";
    private static final String user = "root";
    private static final String psw = "admin";

    public ClienteDao(Connection connection) throws SQLException {
        this.connection = DriverManager.getConnection(url, user, psw);
    }

    public boolean updateClienteName(int id, String newName) throws SQLException {
        String query = "UPDATE clientes SET nombre = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)){
            ps.setString(1, newName);
            ps.setInt(2, id);
            ps.execute();
        }
    }



    @Override
    public void close() throws Exception {
        if (this.connection != null && !this.connection.isClosed())
            this.connection.close();
    }
}
