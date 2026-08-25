package me.sirius.sbooks;

import me.sirius.core.SiriusPlugin;
import me.sirius.core.storage.Storage;

public final class SBooks extends SiriusPlugin {

    private Storage storage;
    private BookStore books;
    private BookCommandRegistry commands;

    @Override
    protected void onStart() {
        saveDefaultConfig();

        this.storage = Storage.from(settings(), getDataFolder(), "sbooks");
        this.commands = new BookCommandRegistry(this);
        this.books = new BookStore(this, storage, commands);

        command("sbooks", new BookAdminCommand(this, books));

        books.loadAll();
    }

    @Override
    protected void onStop() {
        if (commands != null) {
            commands.unregisterAll();
            commands.syncToClients();
        }
        if (storage != null) {
            storage.close();
        }
    }

    public BookStore books() {
        return books;
    }
}
