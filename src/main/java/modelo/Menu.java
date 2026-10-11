
package modelo;
import modelo.Platillo;
import java.util.ArrayList;


public class Menu {
    
     public ArrayList<Platillo> platillos = new ArrayList<>();

    public void agregar_platillo(Platillo platillo) {
        
        platillos.add(platillo);
    }

    // Platillo no tiene id en el diagrama, por eso se busca por nombre
    public boolean eliminar_platillo(String nombre) {
        
        return platillos.removeIf(p -> p.getNombre().equalsIgnoreCase(nombre));
        
    }

    public Platillo ver_platillo(String nombre) {
        
        for (Platillo p : platillos) {
            
            if (p.getNombre().equalsIgnoreCase(nombre)) return p;
        }
        return null;
    }

    public boolean modificar_platillo(String nombre_actual, Platillo nuevo) {
        
        for (int i = 0; i < platillos.size(); i++) {
            if (platillos.get(i).getNombre().equalsIgnoreCase(nombre_actual)) {
                platillos.set(i, nuevo);
                return true;
            }
        }
        return false;
    }

    public ArrayList<Platillo> ver_todos() {
        return platillos;
    }
    
}
