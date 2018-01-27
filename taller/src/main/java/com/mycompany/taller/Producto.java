/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.taller;

/**
 *
 * @author Stephany
 */
public class Producto {

    String nombre;
    String info; 
    int cant; 
    int limite; 
    double precio;
    Boolean estado; 

    public Producto() {}

    public Producto(String nombre, String info, int cant, int limite, double precio, boolean estado) {
        this.nombre = nombre;
        this.info = info;
        this.cant = cant;
        this.limite = limite;
        this.precio = precio;
        this.estado = true;
    }

    public int getCant() {
        return cant;
    }

    public String getNombre() {
        return nombre;
    }

    public double getPrecio() {
        return precio;
    }
 
    public int validar_cant() {
        if (this.getCant() == 0 || this.getCant() < 0 || this.getCant() > 100) {
            return 0;
        }
        return 1;
    }

    public String crear_producto() {
        if (this.validar_cant() == 1) {
            System.out.println("Usted ha creado:" + this.toString());

            return "Se creo el producto exitosamente!";
        }
        return "No se pudo crear el producto.Verifique el cant ingresado.";
    }

    @Override
    public String toString() {
        return "Producto{" + "nombre=" + nombre + '}';
    }

}
