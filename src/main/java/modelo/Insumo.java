
package modelo;

import java.time.LocalDate;

public class Insumo {
    public int id;
    public String nombre;
    public Double stock;
    public LocalDate caducidad;

    public Insumo() {
    }

    public Insumo(int id, String nombre, Double stock, LocalDate caducidad) {
        this.id = id;
        this.nombre = nombre;
        this.stock = stock;
        this.caducidad = caducidad;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Double getStock() {
        return stock;
    }

    public void setStock(Double stock) {
        this.stock = stock;
    }

    public LocalDate getCaducidad() {
        return caducidad;
    }

    public void setCaducidad(LocalDate caducidad) {
        this.caducidad = caducidad;
    }
    
    
}
