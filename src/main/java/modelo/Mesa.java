
package modelo;
import modelo.Pedido;
import java.util.ArrayList;


public class Mesa {
    
    public int id;
    public String estado;
    public ArrayList<Pedido> pedidos = new ArrayList<>();

    public Mesa() {
    }

    public Mesa(int id, String estado) {
        this.id = id;
        this.estado = estado;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }



    public void agregar_pedido(Pedido pedido) {
        pedidos.add(pedido);
    }

    public ArrayList<Pedido> ver_pedidos() {
        return pedidos;
    }
    
}
