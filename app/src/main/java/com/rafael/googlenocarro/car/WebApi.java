package com.rafael.googlenocarro.car;

import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Chamadas de rede (sempre executar fora da thread principal). */
final class WebApi {

    private WebApi() {}

    /** Sugestões de pesquisa do Google (as mesmas do campo de busca). */
    static List<String> googleSuggestions(String query) throws Exception {
        String url = "https://suggestqueries.google.com/complete/search?client=firefox"
                + "&hl=pt-BR&ie=utf-8&oe=utf-8&q=" + Uri.encode(query);
        JSONArray root = new JSONArray(get(url));
        JSONArray arr = root.getJSONArray(1);
        List<String> out = new ArrayList<>();
        for (int i = 0; i < arr.length(); i++) out.add(arr.getString(i));
        return out;
    }

    /** Resumo curto (Wikipédia em português) para mostrar/ler no carro. Pode retornar null. */
    static String[] shortSummary(String query) throws Exception {
        String url = "https://pt.wikipedia.org/w/api.php?action=query&format=json"
                + "&generator=search&gsrlimit=1&prop=extracts&exintro=1&explaintext=1"
                + "&exsentences=3&gsrsearch=" + Uri.encode(query);
        JSONObject root = new JSONObject(get(url));
        JSONObject q = root.optJSONObject("query");
        if (q == null) return null;
        JSONObject pages = q.optJSONObject("pages");
        if (pages == null) return null;
        Iterator<String> keys = pages.keys();
        if (!keys.hasNext()) return null;
        JSONObject page = pages.getJSONObject(keys.next());
        String extract = page.optString("extract", "").trim();
        if (extract.isEmpty()) return null;
        return new String[]{page.optString("title", query), extract};
    }

    private static String get(String urlStr) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL(urlStr).openConnection();
        c.setConnectTimeout(8000);
        c.setReadTimeout(8000);
        c.setRequestProperty("User-Agent", "GoogleNoCarro/1.0 (Android)");
        try (BufferedReader r = new BufferedReader(
                new InputStreamReader(c.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) sb.append(line);
            return sb.toString();
        } finally {
            c.disconnect();
        }
    }
}
