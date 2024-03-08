package com.github.ipecter.rtuserver.lib.plugin.command;

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

}
