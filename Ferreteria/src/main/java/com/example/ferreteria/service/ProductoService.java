package com.example.ferreteria.service;

import com.example.ferreteria.model.Producto;

import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ProductoService {

    private static final String BASE_URL = "http://192.168.100.8:5039/api/productos";
    private final HttpClient client = HttpClient.newHttpClient();

    // ===================== GET /api/productos =====================
    public List<Producto> getProductos() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Error al obtener productos. HTTP " + response.statusCode() + ": " + response.body());
        }

        return parseProductosJson(response.body());
    }

    //=============paara restar we=============//
    public void restarStock(int idProducto, int cantidadVendida) throws Exception {
        // Primero traemos el producto para saber el stock actual
        Producto actual = getProductos().stream()
                .filter(x -> x.getId() == idProducto)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        int nuevoStock = actual.getStock() - cantidadVendida;
        if (nuevoStock < 0) throw new RuntimeException("Stock insuficiente");

        // PATCH solo stock
        String json = "{ \"stock\": " + nuevoStock + " }";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + idProducto))
                .header("Content-Type", "application/json; charset=UTF-8")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Error al restar stock. HTTP " + response.statusCode() + ": " + response.body());
        }
    }


    // ===================== POST /api/productos =====================
    public void addProducto(Producto p) throws Exception {
        String json = "{"
                + "\"nombre\":\"" + escapeJson(p.getNombre()) + "\","
                + "\"descripcion\":\"" + escapeJson(p.getDescripcion()) + "\","
                + "\"precio\":" + p.getPrecio() + ","
                + "\"stock\":" + p.getStock() + ","
                + "\"idProveedor\":" + p.getIdProveedor()
                + "}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("Content-Type", "application/json; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Normalmente crea: 201 Created
        if (response.statusCode() != 201 && response.statusCode() != 200) {
            throw new RuntimeException("Error al agregar producto. HTTP " + response.statusCode() + ": " + response.body());
        }
    }

    // ===================== PATCH /api/productos/{id} =====================
// Actualiza SOLO los campos que no vengan vacíos (Strings) y los numéricos si se mandan con valores válidos.
    public void patchProducto(Producto p) throws Exception {

        if (p == null || p.getId() <= 0) {
            throw new IllegalArgumentException("El producto debe tener un id válido para hacer PATCH.");
        }

        // Construimos JSON solo con los campos que vienen
        StringBuilder sb = new StringBuilder();
        sb.append("{");

        boolean first = true;

        // Strings: solo si vienen con texto
        if (p.getNombre() != null && !p.getNombre().trim().isEmpty()) {
            sb.append("\"nombre\":\"").append(escapeJson(p.getNombre().trim())).append("\"");
            first = false;
        }

        if (p.getDescripcion() != null && !p.getDescripcion().trim().isEmpty()) {
            if (!first) sb.append(",");
            sb.append("\"descripcion\":\"").append(escapeJson(p.getDescripcion().trim())).append("\"");
            first = false;
        }

        // Números: aquí debes decidir regla:
        // - precio: si quieres permitir 0, usa >= 0; si no, > 0
        if (p.getPrecio() > 0) {
            if (!first) sb.append(",");
            sb.append("\"precio\":").append(p.getPrecio());
            first = false;
        }

        // stock: sí puede ser 0 normalmente, así que permitimos >= 0
        if (p.getStock() >= 0) {
            if (!first) sb.append(",");
            sb.append("\"stock\":").append(p.getStock());
            first = false;
        }

        // idProveedor: solo si es válido
        if (p.getIdProveedor() > 0) {
            if (!first) sb.append(",");
            sb.append("\"idProveedor\":").append(p.getIdProveedor());
            first = false;
        }

        sb.append("}");

        // Si no mandaste ningún campo, no tiene sentido hacer PATCH
        String json = sb.toString();
        if (json.equals("{}")) {
            throw new IllegalArgumentException("No hay campos para actualizar. Llena al menos un campo.");
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + p.getId()))
                .header("Content-Type", "application/json; charset=UTF-8")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Error al actualizar producto (PATCH). HTTP "
                    + response.statusCode() + ": " + response.body());
        }
    }



    // ==========================================================
    // Parser SIMPLE (sin Gson) para JSON tipo:
    // [{"id":1,"nombre":"...","descripcion":"...","precio":8.5,"stock":10,"idProveedor":1}, ...]
    // ==========================================================
    private List<Producto> parseProductosJson(String json) {
        List<Producto> list = new ArrayList<>();

        json = json.trim();
        if (json.equals("[]")) return list;

        json = json.replaceAll(",\\s*}", "}")
                .replaceAll(",\\s*]", "]");


        // quitar [ ]
        if (json.startsWith("[")) json = json.substring(1);
        if (json.endsWith("]")) json = json.substring(0, json.length() - 1);

        // separar objetos (asume JSON simple sin objetos anidados)
        String[] objetos = json.split("\\},\\s*\\{");

        for (String obj : objetos) {
            obj = obj.trim();
            if (!obj.startsWith("{")) obj = "{" + obj;
            if (!obj.endsWith("}")) obj = obj + "}";

            int id = intVal(obj, "id");
            String nombre = strVal(obj, "nombre");
            String descripcion = strVal(obj, "descripcion");
            double precio = doubleVal(obj, "precio");
            int stock = intVal(obj, "stock");
            int idProveedor = intVal(obj, "idProveedor");

            list.add(new Producto(id, nombre, descripcion, precio, stock, idProveedor));
        }

        return list;
    }

    private String strVal(String obj, String key) {
        String pattern = "\"" + key + "\":";
        int i = obj.indexOf(pattern);
        if (i == -1) return "";
        i += pattern.length();

        while (i < obj.length() && Character.isWhitespace(obj.charAt(i))) i++;

        if (i < obj.length() && obj.charAt(i) == '\"') {
            i++;
            int end = obj.indexOf("\"", i);
            if (end == -1) return "";
            return obj.substring(i, end);
        }
        return "";
    }

    private int intVal(String obj, String key) {
        String v = numVal(obj, key);
        if (v.isEmpty()) return 0;
        return Integer.parseInt(v);
    }

    private double doubleVal(String obj, String key) {
        String v = numVal(obj, key);
        if (v.isEmpty()) return 0.0;
        return Double.parseDouble(v);
    }

    private String numVal(String obj, String key) {
        String pattern = "\"" + key + "\":";
        int i = obj.indexOf(pattern);
        if (i == -1) return "";
        i += pattern.length();

        while (i < obj.length() && Character.isWhitespace(obj.charAt(i))) i++;

        int end = i;
        while (end < obj.length()) {
            char c = obj.charAt(end);
            if (c == ',' || c == '}' || Character.isWhitespace(c)) break;
            end++;
        }
        return obj.substring(i, end);
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
