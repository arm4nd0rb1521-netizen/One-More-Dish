
package modelo;
import modelo.Platillo;
import java.util.ArrayList;


public class Cocina {
    
        public ArrayList<Pedido> pedidos_global = new ArrayList<>();

    public void agregar_pedido(Pedido pedido) {
        
        pedidos_global.add(pedido);
    }

    public ArrayList<Pedido> get_pedidos_global() {
        
        return pedidos_global;
    }

    public boolean actualizar_estado_pedido(int id_pedido, String nuevo_estado) {
        
        for (Pedido p : pedidos_global) {
            
            if (p.getId()== id_pedido) {
                
                p.setEstado(nuevo_estado);
                return true;
            }
        }
        return false;
    }
    
}
