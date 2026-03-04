package com.example.ferreteria.service;

import com.example.ferreteria.model.Proveedor;

import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ProveedorService {

    private static final String BASE_URL = "http://192.168.100.8:5039/api/proveedores";
    private final HttpClient client = HttpClient.newHttpClient();

    // ===================== GET /api/proveedores =====================
    public List<Proveedor> getProveedores() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Error al obtener proveedores. HTTP " + response.statusCode() + ": " + response.body());
        }

        return parseProveedoresJson(response.body());
    }

    // ===================== POST /api/proveedores =====================
    public void addProveedor(Proveedor p) throws Exception {
        String json = "{"
                + "\"nombre\":\"" + escapeJson(p.getNombre()) + "\","
                + "\"telefono\":\"" + escapeJson(p.getTelefono()) + "\","
                + "\"email\":\"" + escapeJson(p.getEmail()) + "\""
                + "}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("Content-Type", "application/json; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 201 && response.statusCode() != 200) {
            throw new RuntimeException("Error al agregar proveedor. HTTP " + response.statusCode() + ": " + response.body());
        }
    }


    public void deleteProveedor(int id) throws Exception {

        if (id <= 0) {
            throw new IllegalArgumentException("El id debe ser mayor que 0 para eliminar.");
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + id))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200 && response.statusCode() != 204) {
            throw new RuntimeException("Error al eliminar proveedor. HTTP "
                    + response.statusCode() + ": " + response.body());
        }
    }


    // ===================== PATCH /api/proveedores/{id} =====================
    // Actualiza SOLO lo que venga (nombre/telefono/email)
    public void patchProveedor(Proveedor p) throws Exception {

        if (p == null || p.getId() <= 0) {
            throw new IllegalArgumentException("El proveedor debe tener un id válido para hacer PATCH.");
        }

        StringBuilder sb = new StringBuilder();
        sb.append("{");

        boolean first = true;

        if (p.getNombre() != null && !p.getNombre().trim().isEmpty()) {
            sb.append("\"nombre\":\"").append(escapeJson(p.getNombre().trim())).append("\"");
            first = false;
        }

        if (p.getTelefono() != null && !p.getTelefono().trim().isEmpty()) {
            if (!first) sb.append(",");
            sb.append("\"telefono\":\"").append(escapeJson(p.getTelefono().trim())).append("\"");
            first = false;
        }

        if (p.getEmail() != null && !p.getEmail().trim().isEmpty()) {
            if (!first) sb.append(",");
            sb.append("\"email\":\"").append(escapeJson(p.getEmail().trim())).append("\"");
            first = false;
        }

        sb.append("}");

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
            throw new RuntimeException("Error al actualizar proveedor (PATCH). HTTP "
                    + response.statusCode() + ": " + response.body());
        }
    }

    // ==========================================================
    // Parser SIMPLE (sin Gson) para JSON tipo:
    // [{"id":1,"nombre":"...","telefono":"...","email":"..."}, ...]
    // ==========================================================
    private List<Proveedor> parseProveedoresJson(String json) {
        List<Proveedor> list = new ArrayList<>();

        json = json.trim();
        if (json.equals("[]")) return list;

        json = json.replaceAll(",\\s*}", "}")
                .replaceAll(",\\s*]", "]");

        if (json.startsWith("[")) json = json.substring(1);
        if (json.endsWith("]")) json = json.substring(0, json.length() - 1);

        String[] objetos = json.split("\\},\\s*\\{");

        for (String obj : objetos) {
            obj = obj.trim();
            if (!obj.startsWith("{")) obj = "{" + obj;
            if (!obj.endsWith("}")) obj = obj + "}";

            int id = intVal(obj, "id");
            String nombre = strVal(obj, "nombre");
            String telefono = strVal(obj, "telefono");
            String email = strVal(obj, "email");

            list.add(new Proveedor(id, nombre, telefono, email));
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
