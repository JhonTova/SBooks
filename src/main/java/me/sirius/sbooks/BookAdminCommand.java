package me.sirius.sbooks;

import me.sirius.core.text.Text;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public final class BookAdminCommand implements TabExecutor {

    private final SBooks plugin;
    private final BookStore books;

    public BookAdminCommand(SBooks plugin, BookStore books) {
        this.plugin = plugin;
        this.books = books;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            Text.send(sender, books.message("usage"));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "save" -> save(sender, args);
            case "delete", "remove" -> delete(sender, args);
            case "open" -> open(sender, args);
            case "give" -> give(sender, args);
            case "setperm" -> setPermission(sender, args);
            case "list" -> list(sender);
            case "reload" -> reload(sender);
            default -> Text.send(sender, books.message("usage"));
        }
        return true;
    }

    private void save(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            Text.send(sender, books.message("players-only"));
            return;
        }
        if (args.length < 2) {
            Text.send(sender, books.message("usage-save"));
            return;
        }

        BookMeta meta = bookInHand(player);
        if (meta == null) {
            Text.send(sender, books.message("not-a-book"));
            return;
        }

        String id = args[1].toLowerCase();
        String openCommand = args.length >= 3 ? args[2].toLowerCase() : id;

        BookData book = new BookData(
                id,
                meta.hasTitle() ? meta.getTitle() : id,
                meta.hasAuthor() ? meta.getAuthor() : player.getName(),
                openCommand,
                "",
                pagesOf(meta));

        boolean primaryFree = books.save(book);
        Text.send(sender, books.message("saved")
                .replace("<id>", id).replace("<command>", openCommand));
        if (!primaryFree) {
            Text.send(sender, books.message("command-taken")
                    .replace("<command>", openCommand)
                    .replace("<plugin>", plugin.getName().toLowerCase()));
        }
    }

    private void delete(CommandSender sender, String[] args) {
        if (args.length < 2) {
            Text.send(sender, books.message("usage-delete"));
            return;
        }
        if (books.delete(args[1])) {
            Text.send(sender, books.message("deleted").replace("<id>", args[1].toLowerCase()));
        } else {
            Text.send(sender, books.message("not-found"));
        }
    }

    private void open(CommandSender sender, String[] args) {
        if (args.length < 2) {
            Text.send(sender, books.message("usage-open"));
            return;
        }
        Player target = args.length >= 3 ? Bukkit.getPlayerExact(args[2])
                : (sender instanceof Player p ? p : null);
        if (target == null) {
            Text.send(sender, books.message("player-not-found"));
            return;
        }
        books.open(target, args[1]);
    }

    private void give(CommandSender sender, String[] args) {
        if (args.length < 2) {
            Text.send(sender, books.message("usage-give"));
            return;
        }
        BookData book = books.get(args[1]);
        if (book == null) {
            Text.send(sender, books.message("not-found"));
            return;
        }
        Player target = args.length >= 3 ? Bukkit.getPlayerExact(args[2])
                : (sender instanceof Player p ? p : null);
        if (target == null) {
            Text.send(sender, books.message("player-not-found"));
            return;
        }
        target.getInventory().addItem(books.build(book));
        Text.send(sender, books.message("given").replace("<id>", book.id())
                .replace("<player>", target.getName()));
    }

    private void setPermission(CommandSender sender, String[] args) {
        if (args.length < 3) {
            Text.send(sender, books.message("usage-setperm"));
            return;
        }
        BookData book = books.get(args[1]);
        if (book == null) {
            Text.send(sender, books.message("not-found"));
            return;
        }
        String permission = args[2].equalsIgnoreCase("none") ? "" : args[2];
        books.save(new BookData(book.id(), book.title(), book.author(),
                book.command(), permission, book.pages()));
        Text.send(sender, books.message("perm-set")
                .replace("<id>", book.id())
                .replace("<permission>", permission.isEmpty() ? "ninguno" : permission));
    }

    private void list(CommandSender sender) {
        List<BookData> all = books.all();
        if (all.isEmpty()) {
            Text.send(sender, books.message("list-empty"));
            return;
        }
        Text.send(sender, books.message("list-header").replace("<count>", String.valueOf(all.size())));
        for (BookData book : all) {
            Text.send(sender, books.message("list-entry")
                    .replace("<id>", book.id())
                    .replace("<command>", book.command())
                    .replace("<permission>", book.isProtected() ? book.permission() : "—"));
        }
    }

    private void reload(CommandSender sender) {
        plugin.reloadConfig();
        books.loadAll();
        Text.send(sender, books.message("reloaded"));
    }

    private BookMeta bookInHand(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        return item.getItemMeta() instanceof BookMeta meta ? meta : null;
    }

    @SuppressWarnings("deprecation") // getPages(): la API de String funciona en Spigot y Paper.
    private List<String> pagesOf(BookMeta meta) {
        return new ArrayList<>(meta.getPages());
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Stream.of("save", "open", "give", "delete", "setperm", "list", "reload")
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .toList();
        }
        String sub = args[0].toLowerCase();
        if (args.length == 2 && (sub.equals("open") || sub.equals("give")
                || sub.equals("delete") || sub.equals("remove") || sub.equals("setperm"))) {
            String prefix = args[1].toLowerCase();
            return books.all().stream().map(BookData::id)
                    .filter(id -> id.startsWith(prefix)).toList();
        }
        if (args.length == 3 && (sub.equals("open") || sub.equals("give"))) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[2].toLowerCase())).toList();
        }
        return List.of();
    }
}
