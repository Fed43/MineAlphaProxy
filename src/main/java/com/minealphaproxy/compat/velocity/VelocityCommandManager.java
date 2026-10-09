package com.minealphaproxy.compat.velocity;

import com.minealphaproxy.util.Logger;
import com.velocitypowered.api.command.*;
import net.kyori.adventure.text.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class VelocityCommandManager implements CommandManager {

    private final Logger logger;
    private final Map<String, Command> commands = new ConcurrentHashMap<>();
    private final Map<String, CommandMeta> metas = new ConcurrentHashMap<>();
    private final Map<String, String> aliases = new ConcurrentHashMap<>();

    public VelocityCommandManager(Logger logger) {
        this.logger = logger;
    }

    @Override
    public CommandMeta.Builder metaBuilder(String alias) {
        return new MetaBuilderImpl(new String[]{alias.toLowerCase()});
    }

    @Override
    public CommandMeta.Builder metaBuilder(String alias, String... otherAliases) {
        String[] all = new String[1 + otherAliases.length];
        all[0] = alias.toLowerCase();
        for (int i = 0; i < otherAliases.length; i++) {
            all[i + 1] = otherAliases[i].toLowerCase();
        }
        return new MetaBuilderImpl(all);
    }

    @Override
    public void register(String alias, Command command, String... otherAliases) {
        String primary = alias.toLowerCase();
        String[] all = new String[1 + otherAliases.length];
        all[0] = primary;
        for (int i = 0; i < otherAliases.length; i++) {
            all[i + 1] = otherAliases[i].toLowerCase();
        }
        registerInternal(all, command, null);
    }

    @Override
    public void register(Command command, String... aliases) {
        if (aliases.length == 0) return;
        String[] all = new String[aliases.length];
        for (int i = 0; i < aliases.length; i++) all[i] = aliases[i].toLowerCase();
        registerInternal(all, command, null);
    }

    @Override
    public void register(CommandMeta meta, Command command) {
        String[] allAliases = meta.getAliases();
        if (allAliases.length == 0) return;
        registerInternal(allAliases, command, meta);
    }

    private void registerInternal(String[] allAliases, Command command, CommandMeta meta) {
        String primary = allAliases[0].toLowerCase();
        for (String a : allAliases) {
            String key = a.toLowerCase();
            commands.put(key, command);
            aliases.put(key, primary);
        }
        metas.put(primary, meta != null ? meta : new MetaImpl(allAliases, null));
        logger.info("Registered command: /{}", primary);
    }

    @Override
    public void unregister(String alias) {
        String key = alias.toLowerCase();
        String primary = aliases.get(key);
        if (primary == null) return;

        Iterator<Map.Entry<String, Command>> it = commands.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Command> e = it.next();
            if (primary.equals(aliases.get(e.getKey()))) {
                aliases.remove(e.getKey());
                it.remove();
            }
        }
        metas.remove(primary);
        logger.info("Unregistered command: /{}", primary);
    }

    @Override
    public boolean hasCommand(String alias) {
        return commands.containsKey(alias.toLowerCase());
    }

    @Override
    public Optional<CommandMeta> getCommandMeta(String alias) {
        String key = alias.toLowerCase();
        String primary = aliases.get(key);
        if (primary == null) return Optional.empty();
        return Optional.ofNullable(metas.get(primary));
    }

    @Override
    public Optional<Command> getCommand(String alias) {
        return Optional.ofNullable(commands.get(alias.toLowerCase()));
    }

    public boolean execute(CommandSource source, String cmdLine) {
        String line = cmdLine.startsWith("/") ? cmdLine.substring(1) : cmdLine;
        String[] parts = line.split("\\s+");
        if (parts.length == 0) return false;

        String name = parts[0].toLowerCase();
        Command command = commands.get(name);
        if (command == null) return false;

        List<String> args = parts.length > 1
                ? List.of(Arrays.copyOfRange(parts, 1, parts.length))
                : List.of();

        try {
            if (command instanceof SimpleCommand simple) {
                simple.execute(new SimpleCommand.Invocation() {
                    @Override public CommandSource source() { return source; }
                    @Override public String alias() { return name; }
                    @Override public List<String> arguments() { return args; }
                });
            } else {
                logger.warn("Unsupported command type: {}", command.getClass().getName());
            }
        } catch (Exception e) {
            logger.error("Command /{} failed", name, e);
            source.sendMessage(Component.text("§cCommand error: " + e.getMessage()));
        }
        return true;
    }

    private static final class MetaBuilderImpl implements CommandMeta.Builder {
        private final String[] aliases;
        private Object plugin;

        MetaBuilderImpl(String[] aliases) {
            this.aliases = aliases;
        }

        @Override
        public CommandMeta.Builder aliases(String... moreAliases) {
            String[] combined = new String[aliases.length + moreAliases.length];
            System.arraycopy(aliases, 0, combined, 0, aliases.length);
            for (int i = 0; i < moreAliases.length; i++) {
                combined[aliases.length + i] = moreAliases[i].toLowerCase();
            }
            return new MetaBuilderImpl(combined);
        }

        @Override
        public CommandMeta.Builder plugin(Object plugin) {
            this.plugin = plugin;
            return this;
        }

        @Override
        public CommandMeta build() {
            return new MetaImpl(aliases, plugin);
        }
    }

    private static final class MetaImpl implements CommandMeta {
        private final String[] aliases;
        private final Object plugin;

        MetaImpl(String[] aliases, Object plugin) {
            this.aliases = aliases;
            this.plugin = plugin;
        }

        @Override public String[] getAliases() { return aliases; }
        @Override public Object plugin() { return plugin; }
    }
}