package me.sirius.sbooks;

import me.sirius.core.storage.Storage;
import me.sirius.core.text.Text;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class BookStore {

    private final SBooks plugin;
    private final Storage storage;
    private final BookCommandRegistry commands;
    private final Map<String, BookData> cache = new ConcurrentHashMap<>();

    public BookStore(SBooks plugin, Storage storage, BookCommandRegistry commands) {
        this.plugin = plugin;
        this.storage = storage;
        this.commands = commands;
        storage.execute("""
                CREATE TABLE IF NOT EXISTS sbooks_books (
                    id VARCHAR(64) PRIMARY KEY,
                    title VARCHAR(64),
                    author VARCHAR(64),
                    command VARCHAR(64),
                    permission VARCHAR(128),
                    pages TEXT
                )""");
    }

    public void loadAll() {
        plugin.scheduler().supplyAsync(this::queryAll).thenAccept(books ->
                plugin.scheduler().sync(() -> {
                    cache.clear();
                    commands.unregisterAll();
                    for (BookData book : books) {
                        cache.put(book.id(), book);
                        commands.register(book);
                    }
                    commands.syncToClients();
                    plugin.getLogger().info("Cargados " + cache.size() + " libro(s).");
                }));
    }

    private List<BookData> queryAll() {
        return storage.queryAll(
                "SELECT id, title, author, command, permission, pages FROM sbooks_books",
                row -> new BookData(
                        row.getString("id"),
                        row.getString("title"),
                        row.getString("author"),
                        row.getString("command"),
                        row.getString("permission"),
                        BookData.deserializePages(row.getString("pages"))));
    }

    public boolean save(BookData book) {
        cache.put(book.id(), book);
        boolean primaryFree = commands.register(book);
        commands.syncToClients();

        plugin.scheduler().async(() -> storage.execute(
                "REPLACE INTO sbooks_books (id, title, author, command, permission, pages) VALUES (?, ?, ?, ?, ?, ?)",
                book.id(), book.title(), book.author(), book.command(),
                book.permission(), book.serializedPages()));
        return primaryFree;
    }

    public boolean delete(String id) {
        BookData removed = cache.remove(id.toLowerCase());
        if (removed == null) {
            return false;
        }
        commands.unregister(removed.command());
        commands.syncToClients();
        plugin.scheduler().async(() -> storage.execute(
                "DELETE FROM sbooks_books WHERE id = ?", removed.id()));
        return true;
    }

    public void open(Player player, String id) {
        BookData book = cache.get(id.toLowerCase());
        if (book == null) {
            Text.send(player, message("not-found"));
            return;
        }
        if (book.isProtected() && !player.hasPermission(book.permission())) {
            Text.send(player, message("no-permission"));
            return;
        }
        ItemStack item = build(book);
        // La entidad correcta en Folia es el propio jugador; fuera de Folia esto corre
        // sincrónico en el hilo principal.
        plugin.scheduler().entity(player, () -> player.openBook(item));
    }

    public ItemStack build(BookData book) {
        ItemStack item = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) item.getItemMeta();
        meta.setTitle(trim(book.title()));
        meta.setAuthor(book.author());

        List<String> pages = book.pages().isEmpty() ? List.of(" ") : book.pages();
        meta.setPages(pages);

        item.setItemMeta(meta);
        return item;
    }

    private String trim(String title) {
        if (title == null) {
            return "Libro";
        }
        return title.length() > 32 ? title.substring(0, 32) : title;
    }

    public BookData get(String id) {
        return cache.get(id.toLowerCase());
    }

    public List<BookData> all() {
        return new ArrayList<>(cache.values());
    }

    public boolean exists(String id) {
        return cache.containsKey(id.toLowerCase());
    }

    public String message(String key) {
        return plugin.getConfig().getString("messages." + key, key);
    }
}
