package Dao;

import dto.ResumenCliente;
import model.Pedido;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

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


    public void deleteClienteYPedidos(int clienteId) {
        String queryPedidos = "DELETE FROM pedidos WHERE cliente_id = ?";
        String queryCliente = "DELETE FROM cliente WHERE id = ?";
        int filasAfectadas = 0;
        try {
            connection.setAutoCommit(false);
            try (PreparedStatement ps = connection.prepareStatement(queryPedidos)) {
                ps.setInt(1, clienteId);
                filasAfectadas += ps.executeUpdate();
            }

            try (PreparedStatement ps = connection.prepareStatement(queryCliente)) {
                ps.setInt(1, clienteId);
                filasAfectadas += ps.executeUpdate();
            }
            connection.commit();
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            throw new RuntimeException();
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            System.out.println("Filas afectadas: " + filasAfectadas);
        }
    }

    //actividad 5
public void totalAcumuladoCliente(String desde, String hasta){
        String regex = "[1-9]{4}-(0[1-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[0-1])";

        LocalDate fechaInicial = null;
        LocalDate fechaFinal = null;
        Scanner scan = new Scanner(System.in);
        boolean correcto = false;
        while (!correcto){
            System.out.println("Introduce la fecha inicial (yyyy-MM-dd)");
            String fecha = scan.nextLine();
            if (Pattern.matches(regex, fecha)) {
                fechaInicial = LocalDate.parse(fecha);
                System.out.println("Introduce la fecha final (yyyy-MM-dd)");
                fecha = scan.nextLine();
                while (!fecha.matches(regex)){
                    System.out.println("Formato de fecha incorrecto");
                    fecha = scan.nextLine();
                }
                fechaFinal = LocalDate.parse(fecha);
                correcto = true;

            }
            else
                System.out.println("Formato de fecha incorrecto");
        }


        String query = "SELECT c.nombre, COUNT(p.id) as totalPedidos, SUM(p.total) AS Total from cliente c JOIN pedido p ON c.id = p.cliente_id WHERE p.fecha > ? AND p.fecha < ? GROUP BY c.nombre ORDER BY Total DESC, c.nombre ASC";

        try(PreparedStatement ps = connection.prepareStatement(query)){
            ps.setDate(1, Date.valueOf(fechaInicial));
            ps.setDate(2, Date.valueOf(fechaFinal));
            try (ResultSet rs = ps.executeQuery()){
                while (rs.next()){
                    ResumenCliente resumenCliente = new ResumenCliente(rs.getString("nombre"), rs.getInt("totalPedidos"), rs.getDouble("Total"));
                    System.out.println(resumenCliente.toString());
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
}

public void crearIndice(){
        String query = "CREATE INDEX idx_cliente_fecha ON pedidos(cliente_id, fecha)";

        try (Statement statement = connection.createStatement()){
            statement.executeUpdate(query);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
}


    @Override
    public void close() throws Exception {
        if (this.connection != null && !this.connection.isClosed())
            this.connection.close();
    }
}
