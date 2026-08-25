package me.sirius.sbooks;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public record BookData(
        String id,
        String title,
        String author,
        String command,
        String permission,
        List<String> pages
) {

    public String serializedPages() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < pages.size(); i++) {
            if (i > 0) {
                sb.append('\n');
            }
            sb.append(Base64.getEncoder().encodeToString(pages.get(i).getBytes(StandardCharsets.UTF_8)));
        }
        return sb.toString();
    }

    public static List<String> deserializePages(String data) {
        List<String> out = new ArrayList<>();
        if (data == null || data.isEmpty()) {
            return out;
        }
        // -1 conserva las entradas vacías del final: una página en blanco entre páginas
        // con texto debe sobrevivir, o el contenido se desplazaría al recargar.
        for (String line : data.split("\n", -1)) {
            out.add(new String(Base64.getDecoder().decode(line), StandardCharsets.UTF_8));
        }
        return out;
    }

    public boolean isProtected() {
        return permission != null && !permission.isBlank();
    }
}
