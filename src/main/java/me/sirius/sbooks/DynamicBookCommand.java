package me.sirius.sbooks;

import me.sirius.core.text.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public final class DynamicBookCommand extends Command {

    private final SBooks plugin;
    private final String bookId;

    public DynamicBookCommand(SBooks plugin, String name, String bookId) {
        super(name);
        this.plugin = plugin;
        this.bookId = bookId;
        setDescription("Abre el libro " + bookId);
        setUsage("/" + name);
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            Text.send(sender, plugin.books().message("players-only"));
            return true;
        }
        plugin.books().open(player, bookId);
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        return Collections.emptyList();
    }
}
