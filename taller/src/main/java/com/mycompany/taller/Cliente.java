/*
 * @cliente.java         
 *
 * @author             Kattya Desiderio 
 * Fecha Creación      04/05/2017
 * Fecha Modificación  26/01/18
 * @version            1.2
 */
package com.mycompany.taller;

/**
 *
 * @author Kattya
 */
public class Cliente {
    
    String name;
    int sector;
    //Pago pago;

    /**
     *
     * @param name
     * @param sector
     * @param pago
     */
    public Cliente(String name, int sector) {
        this.name = name;
        this.sector = sector;
        //this.pago = pago;
    }
    
    /**
     *
     * @return names
     */
    public String getNombres() {
        return name;
    }
 
    public String getSector() {
        switch (this.sector) {
            case 1:
                return "Norte";
            case 2:
                return "Centro";
            case 3:
                return "Sur";
            default:
                break;
        }
        return null;
    }
    
    public String getInfoCliente() {
        return "Usted ingreso la siguiente informacion" + "\nNombres:" + name + "\nSector=" + sector;
    }

    @Override
    public String toString() {
        return "Informacion correcta";
    }

}
