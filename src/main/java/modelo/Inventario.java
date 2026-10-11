
package modelo;
import modelo.Insumo;
import java.util.ArrayList;


public class Inventario {
    
       public ArrayList<Insumo> insumos = new ArrayList<>();

    public void agregar_insumo(Insumo insumo) {
        
        insumos.add(insumo);
    }

    public boolean eliminar_insumo(int id) {
        
        return insumos.removeIf(i -> i.getId()== id);
    }

    public boolean modificar_insumo(Insumo nuevo) {
        
        for (int i = 0; i < insumos.size(); i++) {
            
            if (insumos.get(i).getId()== nuevo.getId()) {
                
                insumos.set(i, nuevo);
                return true;
            }
        }
        return false;
    }

    public ArrayList<Insumo> ver_insumos() {
        
        return insumos;
    }

    public Insumo ver_insumo(int id) {
        
        for (Insumo i : insumos) {
            
            if (i.getId() == id) return i;
        }
        return null;
    }
    
}
