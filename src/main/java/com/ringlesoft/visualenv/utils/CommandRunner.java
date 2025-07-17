package com.ringlesoft.visualenv.utils;

public class CommandRunner {

    // TODO Improve this
    public static String runCommand(String command) {
        try {
            Process process = Runtime.getRuntime().exec(command);
            process.waitFor();
            return process.getOutputStream().toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Failed!";
    }
}
