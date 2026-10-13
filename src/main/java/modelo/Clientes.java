package modelo;

import java.util.ArrayList;
public class Clientes {
    
    public ArrayList<Cliente> clientes = new ArrayList<>();
    
    public void agregar_Cliente (Cliente cliente){
        clientes.add(cliente);
        
    }


    public void eliminar_Cliente(int id) {
        clientes.removeIf(c -> c.getId() == id);
    }

    public Cliente ver_Cliente(int id) {
        for (Cliente c : clientes) {
            if (c.getId() == id) {
                return c;
            }
        }
        return null;
    }

    public void modificar_Cliente(Cliente clienteModificado) {
        for (int i = 0; i < clientes.size(); i++) {
            if (clientes.get(i).getId() == clienteModificado.getId()) {
                clientes.set(i, clienteModificado);
                break;
            }
        }
    }

    public ArrayList<Cliente> verTodos() {
        return clientes;
    }
  

}