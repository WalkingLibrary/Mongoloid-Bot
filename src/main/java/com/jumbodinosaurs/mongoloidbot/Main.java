package com.jumbodinosaurs.mongoloidbot;

public class Main
{
    public static void main(String[] args)
    {
        if(args.length <= 0)
        {
            System.out.println("Usage: Java -jar jarName.jar discordToken");
            System.exit(1);
        }
    }
}
