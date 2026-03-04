package com.example.ferreteria.service;

import com.example.ferreteria.model.Venta;
import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class VentaService {

    private static final String BASE_URL = "http://192.168.100.8:5039/api/ventas";
    private final HttpClient client = HttpClient.newHttpClient();

    public void registrarVenta(Venta v) throws Exception {

        String json = "{"
                + "\"fecha\":\"" + escapeJson(v.getFecha()) + "\","
                + "\"idProducto\":" + v.getIdProducto() + ","
                + "\"cantidadVendida\":" + v.getCantidadVendida() + ","
                + "\"total\":" + v.getTotal()
                + "}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("Content-Type", "application/json; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 201 && response.statusCode() != 200) {
            throw new RuntimeException("Error al registrar venta. HTTP " + response.statusCode() + ": " + response.body());
        }
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
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

    private List<Venta> parseVentasJson(String json) {
        List<Venta> list = new ArrayList<>();

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
            String fecha = strVal(obj, "fecha");
            int idProducto = intVal(obj, "idProducto");
            int cantidadVendida = intVal(obj, "cantidadVendida");
            double total = doubleVal(obj, "total");

            list.add(new Venta(id, fecha, idProducto, cantidadVendida, total));
        }

        return list;
    }

    public List<Venta> getVentas() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Error al obtener ventas. HTTP " + response.statusCode() + ": " + response.body());
        }

        return parseVentasJson(response.body());
    }
}
