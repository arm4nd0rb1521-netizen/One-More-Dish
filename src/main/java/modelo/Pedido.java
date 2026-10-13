
package modelo;
import modelo.Platillo;
import java.util.ArrayList;

public class Pedido {
    
    private int id;
    public ArrayList<Platillo> platillos_pedidos = new ArrayList<>();
    public String estado;
    public boolean domicilio;
    public String direccion;
    public int id_cliente;
    public int id_mesa;

    public Pedido() {
    }

    public Pedido(int id, String estado, boolean domicilio, String direccion, int id_cliente, int id_mesa) {
        this.id = id;
        this.estado = estado;
        this.domicilio = domicilio;
        this.direccion = direccion;
        this.id_cliente = id_cliente;
        this.id_mesa = id_mesa;
    }

    public int getId_mesa() {
        return id_mesa;
    }

    public void setId_mesa(int id_mesa) {
        this.id_mesa = id_mesa;
    }



    public int getId() { 
        return id; 
    }
    public void setId(int id) { 
        this.id = id; 
    }

    public boolean getDomicilio() { 
        return domicilio; 
    }
    public void setDomicilio(boolean domicilio) {
        this.domicilio = domicilio; 
    }

    public String getEstado() { 
        return estado;
    }
    public void setEstado(String estado) { 
        this.estado = estado;
    }

    public String getDireccion() {
        return direccion; 
    }
    public void setDireccion(String direccion) { 
        this.direccion = direccion; 
    }

    public int getId_cliente() { 
        return id_cliente; 
    }
    public void setId_cliente(int id_cliente) { 
        this.id_cliente = id_cliente; 
    }

    public ArrayList<Platillo> get_platillos_pedidos() { 
        return platillos_pedidos;
    }

    public void agregar_platillo(Platillo platillo) {
        platillos_pedidos.add(platillo);
    }

    public boolean eliminar_platillo(Platillo platillo) {
        return platillos_pedidos.remove(platillo);
    }

    public void cancelar_pedido() {
        this.estado = "Cancelado";
    }
    

    
    
    
}
