<div align="center">

# SBooks

### Guarda cualquier libro escrito y ábrelo con su propio comando.

`/reglas`, `/eventos`, `/info`… sin reiniciar el servidor y sin lag al abrirlos.

[![build](https://github.com/JhonTova/SBooks/actions/workflows/build.yml/badge.svg)](https://github.com/JhonTova/SBooks/actions/workflows/build.yml) [![release](https://img.shields.io/github/v/release/JhonTova/SBooks?label=release)](https://github.com/JhonTova/SBooks/releases/latest) ![Minecraft](https://img.shields.io/badge/Minecraft-1.21.11%2B-62B47A) ![Java](https://img.shields.io/badge/Java-21-E76F00)

</div>

---

## Características

- **Comandos al vuelo**: cada libro guardado registra su propio comando (`/reglas`) en
  caliente, y el autocompletado de los jugadores se actualiza sin reconectar.
- **Base de datos con caché**: los libros se leen de SQLite o MySQL al arrancar y quedan en
  memoria, así que abrir uno no toca la base de datos.
- **Animación nativa**: el libro se abre con la interfaz y el paso de página de Minecraft.
- **Permiso por libro**: restringe quién puede abrir cada uno, o déjalo público.
- **Entrega física**: da una copia del libro a un jugador como ítem.
- Mensajes personalizables con MiniMessage (vía SiriusCore).

## Uso rápido

1. Escribe el libro en el juego (libro y pluma, o un libro ya firmado).
2. Con el libro en la mano: `/sbooks save reglas`.
3. Cualquier jugador puede escribir `/reglas` para leerlo.

## Comandos

| Comando | Acción |
|---|---|
| `/sbooks save <id> [comando]` | Guarda el libro que tienes en la mano. Sin `comando`, usa el `id`. |
| `/sbooks open <id> [jugador]` | Abre un libro, a ti o a otro jugador. |
| `/sbooks give <id> [jugador]` | Entrega el libro como ítem. |
| `/sbooks setperm <id> <permiso\|none>` | Restringe quién puede abrirlo. |
| `/sbooks delete <id>` | Borra un libro y su comando. |
| `/sbooks list` | Lista los libros guardados. |
| `/sbooks reload` | Recarga los libros desde la base de datos. |

Alias: `/sbook`. Permiso de administración: `sbooks.admin` (por defecto, OP).

## Configuración

```yaml
storage:
  type: sqlite   # sqlite (un archivo, sin configurar nada) o mysql
  # host, port, database, username, password, ssl y pool-size solo si type: mysql
```

Todos los mensajes están en `config.yml`, en la sección `messages`.

## Requisitos

Requiere **[SiriusCore](https://github.com/JhonTova/SiriusCore)**. Sin él, el plugin no arranca.

## Instalación

`SiriusCore.jar` + `SBooks.jar` en `plugins/`. Spigot / Paper / Purpur 1.21.11+.

## Descarga

El `.jar` de cada versión está en [Releases](https://github.com/JhonTova/SBooks/releases/latest).

## Compilar desde el código

Requiere Java 21 y Maven. SiriusCore no está en un repositorio Maven público, así que
primero se instala en local la versión que declara el `pom.xml`:

```bash
git clone --branch v1.1.1 https://github.com/JhonTova/SiriusCore.git
mvn -f SiriusCore/pom.xml install -DskipTests
mvn package        # en este repositorio; el .jar queda en target/
```

Cada push y pull request se compila en GitHub Actions. Al subir una tag `v*`, el `.jar`
se adjunta solo a la release.
