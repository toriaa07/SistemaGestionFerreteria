package com.example.ferreteria.model;

public class Venta {

    private int id;
    private String fecha; // Puedes cambiar a LocalDate después si quieres
    private int idProducto;
    private int cantidadVendida;
    private double total;

    public Venta() {}

    // Constructor completo
    public Venta(int id, String fecha, int idProducto, int cantidadVendida, double total) {
        this.id = id;
        this.fecha = fecha;
        this.idProducto = idProducto;
        this.cantidadVendida = cantidadVendida;
        this.total = total;
    }

    // Constructor para crear venta (sin id)
    public Venta(String fecha, int idProducto, int cantidadVendida, double total) {
        this.fecha = fecha;
        this.idProducto = idProducto;
        this.cantidadVendida = cantidadVendida;
        this.total = total;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public int getIdProducto() { return idProducto; }
    public void setIdProducto(int idProducto) { this.idProducto = idProducto; }

    public int getCantidadVendida() { return cantidadVendida; }
    public void setCantidadVendida(int cantidadVendida) { this.cantidadVendida = cantidadVendida; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
}
