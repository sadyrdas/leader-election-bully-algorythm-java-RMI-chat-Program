package cz.cvut.fel.dsva.semestralka;

import cz.cvut.fel.dsva.semestralka.base.Node;
import cz.cvut.fel.dsva.semestralka.pattern.commandHandler.CommandHandler;
import cz.cvut.fel.dsva.semestralka.pattern.commandHandler.HelpCommandHandler;
import cz.cvut.fel.dsva.semestralka.pattern.commandHandler.PrintStatusCommandHandler;
import cz.cvut.fel.dsva.semestralka.pattern.commandHandler.SendMessageCommandHandler;
import cz.cvut.fel.dsva.semestralka.service.ChatServiceImpl;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class ChatCLI implements Runnable {
    private BufferedReader reader;
    private final Map<String, CommandHandler> commandHandlers;
    private Node node;
    private boolean reading = true;
    private ChatServiceImpl chatService;

    public ChatCLI(Node node) throws RemoteException {
        this.commandHandlers = new HashMap<>();
        this.reader = new BufferedReader(new InputStreamReader(System.in));
        this.node = node;
        this.chatService = new ChatServiceImpl();
        initializeCommandHandlers();
    }

    private void initializeCommandHandlers(){
        commandHandlers.put("help", new HelpCommandHandler(chatService));
        commandHandlers.put("send", new SendMessageCommandHandler(chatService));
        commandHandlers.put("status", new PrintStatusCommandHandler(chatService));
    }

    private void printWelcomeMessage(){
        log.info("Welcome to Chat Program! You are node with id {}", node.getNodeId());
        log.info("Tap 'help' to get available commands");
    }

    private void stop(){
        reading = false;
    }
    private void handleCommand(String command, String[] arguments){
        CommandHandler commandHandler = commandHandlers.get(command);
        if (command != null){
            commandHandler.handle(arguments, node);
        }else{
            log.info("Unknown command, please try again ;3");
        }
    }

    private void parseCommandLine(String commandLine) {
        String[] parts = commandLine.split(" ", 2);
        String command = parts[0].toLowerCase();

        // Split the arguments into an array
        String[] arguments = (parts.length > 1) ? parts[1].split("\\s+") : new String[0];

        handleCommand(command, arguments);
    }



    @Override
    public void run() {
        printWelcomeMessage();
        String commandline;
        while (reading) {
            try {
                System.out.print(System.lineSeparator() + "cmd > ");
                commandline = reader.readLine();

                if (commandline == null || commandline.equalsIgnoreCase("exit")) {
                    log.info("Exiting ChatCLI.");
                    stop();
                    break;
                }

                parseCommandLine(commandline.trim());
            } catch (IOException e) {
                log.error("Error reading console input: {}", e.getMessage());
                stop();


            }
        }
        log.info("Closing ChatCLI.");
    }
}