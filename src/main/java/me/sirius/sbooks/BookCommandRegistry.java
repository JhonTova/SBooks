package me.sirius.sbooks;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public final class BookCommandRegistry {

    private final SBooks plugin;
    private final String prefix;
    private final CommandMap commandMap;
    private final Map<String, Command> registered = new HashMap<>();

    public BookCommandRegistry(SBooks plugin) {
        this.plugin = plugin;
        this.prefix = plugin.getName().toLowerCase();
        this.commandMap = resolveCommandMap();
    }

    private CommandMap resolveCommandMap() {
        try {
            Server server = Bukkit.getServer();
            Method getCommandMap = server.getClass().getMethod("getCommandMap");
            return (CommandMap) getCommandMap.invoke(server);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(
                    "No se pudo acceder al CommandMap del servidor; SBooks no puede registrar comandos.", e);
        }
    }

    public boolean register(BookData book) {
        String name = book.command().toLowerCase();
        unregister(name);

        Command command = new DynamicBookCommand(plugin, name, book.id());
        boolean primaryFree = commandMap.register(prefix, command);
        registered.put(name, command);
        return primaryFree;
    }

    public void unregister(String commandName) {
        String name = commandName.toLowerCase();
        Command previous = registered.remove(name);
        if (previous != null) {
            previous.unregister(commandMap);
        }
        knownCommands().ifPresent(known -> {
            known.remove(name);
            known.remove(prefix + ":" + name);
        });
    }

    public void unregisterAll() {
        for (String name : new ArrayList<>(registered.keySet())) {
            unregister(name);
        }
    }

    public void syncToClients() {
        try {
            Method sync = Bukkit.getServer().getClass().getDeclaredMethod("syncCommands");
            sync.setAccessible(true);
            sync.invoke(Bukkit.getServer());
        } catch (ReflectiveOperationException ignored) {
            // No todas las plataformas lo exponen; no es crítico.
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.updateCommands();
        }
    }

    @SuppressWarnings("unchecked")
    private java.util.Optional<Map<String, Command>> knownCommands() {
        Class<?> type = commandMap.getClass();
        while (type != null) {
            try {
                Field field = type.getDeclaredField("knownCommands");
                field.setAccessible(true);
                return java.util.Optional.of((Map<String, Command>) field.get(commandMap));
            } catch (NoSuchFieldException e) {
                type = type.getSuperclass();
            } catch (IllegalAccessException e) {
                plugin.getLogger().log(Level.WARNING, "No se pudo leer knownCommands", e);
                return java.util.Optional.empty();
            }
        }
        return java.util.Optional.empty();
    }
}
