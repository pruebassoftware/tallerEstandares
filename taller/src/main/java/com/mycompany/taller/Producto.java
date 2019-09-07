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

    private String nombre;
    private String info; 
    private int cant; 
    private int limite; 
    private double precio;
    private Boolean estado; 

    public Producto() {}

    public Producto(String nombre, String info, int cant, int limite, double precio, boolean estado) {
        this.nombre = nombre;
        this.info = info;
        this.cant = cant;
        this.limite = limite;
        this.precio = precio;
        this.estado = estado;
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
 
    public int validarCant() {
        if (this.getCant() == 0 || this.getCant() < 0 || this.getCant() > 100) {
            return 0;
        }
        return 1;
    }

    public String crearProducto() {
        if (this.validarCant() == 1) {
            System.out.println("Usted ha creado:" + this.toString());

            return "Se creo el producto exitosamente!";
        }
        return "No se pudo crear el producto.Verifique el cant ingresado.";
    }

    @Override
    public String toString() {
        return "Producto{" + "nombre=" + getNombre() + '}';
    }

    /**
     * @param nombre the nombre to set
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * @return the info
     */
    public String getInfo() {
        return info;
    }

    /**
     * @param info the info to set
     */
    public void setInfo(String info) {
        this.info = info;
    }

    /**
     * @param cant the cant to set
     */
    public void setCant(int cant) {
        this.cant = cant;
    }

    /**
     * @return the limite
     */
    public int getLimite() {
        return limite;
    }

    /**
     * @param limite the limite to set
     */
    public void setLimite(int limite) {
        this.limite = limite;
    }

    /**
     * @param precio the precio to set
     */
    public void setPrecio(double precio) {
        this.precio = precio;
    }

    /**
     * @return the estado
     */
    public Boolean getEstado() {
        return estado;
    }

    /**
     * @param estado the estado to set
     */
    public void setEstado(Boolean estado) {
        this.estado = estado;
    }

}
