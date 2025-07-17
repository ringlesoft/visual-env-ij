package com.ringlesoft.visualenv.utils;

import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.*;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.util.Key;
import org.jetbrains.annotations.NotNull;


public class CommandRunner {
    private final Project project;

    public CommandRunner(Project project) {
        this.project = project;
    }

    /**
     *  Different ways to run commands
     */

    public void runCommand(String command) {
        this.runCommand(command, null);
    }

    public void runCommand(String command, ProcessListener processListener) {
        String[] parts = command.split(" ");
        // Capture the two first parts are path, command
        if (parts.length > 2) {
            String[] args = new String[parts.length - 2];
            System.arraycopy(parts, 2, args, 0, args.length);
            runCommandWithOutput(parts[0], parts[1], args, (processListener != null) ? processListener : outputHandler());
        }
    }

    public String runCommandWithOutput(String command) {
        String[] parts = command.split(" ");
        // Capture the two first parts are path, command
        if (parts.length > 2) {
            String[] args = new String[parts.length - 2];
            System.arraycopy(parts, 2, args, 0, args.length);
            StringBuilder output = new StringBuilder();
            runCommandWithOutput(parts[0], parts[1], args, new ProcessListener() {
                @Override
                public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
                    output.append(event.getText());
                }

                @Override
                public void processTerminated(@NotNull ProcessEvent event) {
                    // Do nothing
                }
            });
            return output.toString();
        } else {
            return "Unsupported command: " + command;
        }
    }


//    /**  // THIS REQUIRES an additional dependency  (import org.jetbrains.plugins.terminal.TerminalView;)
//     *  Run command in IJ terminal
//     * @param project
//     * @param command
//     */
//
//    public void runInTerminal(Project project, String command) {
//        ToolWindow terminalToolWindow = ToolWindowManager.getInstance(project)
//                .getToolWindow("Terminal");
//
//        if (terminalToolWindow != null) {
//            terminalToolWindow.activate(() -> {
//                TerminalView terminalView = TerminalView.getInstance(project);
//                terminalView.createLocalShellWidget(project.getBasePath(), null)
//                        .executeCommand(command);
//            });
//        }
//    }


    private void runCommand(String path, String command, String[] args) {
        try {
            GeneralCommandLine commandLine = prepareCommandLine(path, command, args);
            ProcessHandler processHandler = ProcessHandlerFactory.getInstance()
                    .createProcessHandler(commandLine);
            processHandler.startNotify();
        } catch (Exception e) {
            // Handle error
        }
    }


    private void runCommandWithOutput(String path, String command, String[] args, ProcessListener processListener) {
        try {
            GeneralCommandLine commandLine = prepareCommandLine(path, command, args);
            OSProcessHandler processHandler = new OSProcessHandler(commandLine);
            processHandler.addProcessListener(processListener);
            processHandler.startNotify();
        } catch (Exception e) {
            // Handle error
        }
    }

    private GeneralCommandLine prepareCommandLine(String path, String command, String[] args) {
        GeneralCommandLine commandLine = new GeneralCommandLine(path);
        commandLine.addParameter(command);
        if (args.length > 0) {
            commandLine.addParameters(args);
        }
        commandLine.setWorkDirectory(project.getBasePath());
        return commandLine;
    }

    private void runBackgroundCommand(String path, String command, String[] args, ProcessListener processListener) {
        new Task.Backgroundable(project, "Visual env command", true) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                try {
                    GeneralCommandLine commandLine = prepareCommandLine(path, command, args);
                    ProcessHandler processHandler = ProcessHandlerFactory.getInstance()
                            .createProcessHandler(commandLine);
                    processHandler.addProcessListener(processListener);
                    processHandler.startNotify();
                    processHandler.waitFor();
                } catch (Exception e) {
                    // Handle error
                }
            }
        }.queue();
    }

    private ProcessListener outputHandler() {
        StringBuilder output = new StringBuilder();
        return new ProcessListener() {
            @Override
            public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
                output.append(event.getText());
            }

            @Override
            public void processTerminated(@NotNull ProcessEvent event) {
                if (event.getExitCode() == 0) {
                    Notifications.Bus.notify(new Notification(
                            "Visual Env Notification Group",
                            "Success",
                            "Command executed successfully!",
                            NotificationType.INFORMATION
                    ), project);
                } else {
                    Notifications.Bus.notify(new Notification(
                            "Visual Env Notification Group",
                            "Error",
                            "Failed to execute command: " + output,
                            NotificationType.ERROR
                    ), project);
                }
            }
        };
    }
}
