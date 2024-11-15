package com.jumbodinosaurs.mongoloidbot.models;

public enum DiscordANSITextHelper
{
    //https://gist.github.com/kkrypt0nn/a02506f3712ff2d1c8ca7c9e0aed7c06
    GRAY("\u001B[30m", "\u001B[0m"),
    RED("\u001B[31m", "\u001B[0m"),
    GREEN("\u001B[32m", "\u001B[0m"),
    YELLOW("\u001B[33m", "\u001B[0m"),
    BLUE("\u001B[34m", "\u001B[0m"),
    PINK("\u001B[35m", "\u001B[0m"),
    CYAN("\u001B[36m", "\u001B[0m"),
    WHITE("\u001B[37m", "\u001B[0m"),
    BOLD("\u001B[1;37m", "\u001B[0m"),
    UNDERLINE("\u001B[4;37m", "\u001B[0m");

    public static String ansiOpen = "```ansi\n\n";
    public static String ansiClose = "```";
    public String openTag;
    public String closeTag;

    DiscordANSITextHelper(String openTag, String closeTag)
    {
        this.openTag = openTag;
        this.closeTag = closeTag;
    }

    public static String finalWrap(String message)
    {
        return ansiOpen + message + ansiClose;
    }

    public String wrap(String message)
    {
        return this.openTag + message + this.closeTag;
    }
}
