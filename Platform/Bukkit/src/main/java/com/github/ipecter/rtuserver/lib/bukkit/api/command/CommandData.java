package com.github.ipecter.rtuserver.lib.bukkit.api.command;

public record CommandData(String[] args) {
    public String args(int argIndex) {
        if (args.length <= argIndex) return "";
        return args[argIndex];
    }

    public int length() {
        return args.length;
    }

    public boolean length(int equal) {
        return length() == equal;
    }

    public boolean equals(int arg, String text) {
        return args(arg).equals(text);
    }

    public boolean isEmpty() {
        return args.length == 0;
    }

}
