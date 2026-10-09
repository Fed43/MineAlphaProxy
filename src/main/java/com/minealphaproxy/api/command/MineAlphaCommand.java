package com.minealphaproxy.api.command;

public abstract class MineAlphaCommand {

    private final String name;
    private final String description;
    private final String usage;
    private final String permission;
    private final String[] aliases;

    protected MineAlphaCommand(String name) {
        this(name, "", "/" + name, null, new String[0]);
    }

    protected MineAlphaCommand(String name, String description,
                               String usage, String permission,
                               String... aliases) {
        this.name = name.toLowerCase();
        this.description = description;
        this.usage = usage;
        this.permission = permission;
        this.aliases = aliases;
    }

    public abstract void execute(CommandSource sender, String[] args);

    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getUsage() { return usage; }
    public String getPermission() { return permission; }
    public String[] getAliases() { return aliases; }
}