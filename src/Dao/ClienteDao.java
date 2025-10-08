package Dao;

import model.Pedido;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
            int filas = ps.executeUpdate();
            return filas == 1;
        }
    }

    public List<Pedido> getPedidosByCliente (int clienteId){
        List<Pedido> pedidos = new ArrayList<>();
        String query = "SELECT * FROM pedidos WHERE cliente_id = ? ORDER BY date ASC, id ASC";
        try (PreparedStatement ps = connection.prepareStatement(query)){
            ps.setInt(1, clienteId);
            try (ResultSet rs = ps.executeQuery()){
                while (rs.next()){
                    Pedido pedido = new Pedido();
                    pedido.setId(rs.getInt("id"));
                    pedido.setClienteId(rs.getInt("id_cliente"));
                    pedido.setFecha(rs.getDate("date").toLocalDate());
                    pedido.setTotal(rs.getDouble("total"));
                    pedidos.add(pedido);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return pedidos;
    }




    @Override
    public void close() throws Exception {
        if (this.connection != null && !this.connection.isClosed())
            this.connection.close();
    }
}
