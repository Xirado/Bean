// 
// Decompiled by Procyon v0.5.36
// 

package com.jagrosh.jmusicbot.entities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.util.Scanner;

public class Prompt
{
    private final String title;
    private final String noguiMessage;
    private boolean nogui;
    private Scanner scanner;
    
    public Prompt(final String title) {
        this(title, null);
    }
    
    public Prompt(final String title, final String noguiMessage) {
        this(title, noguiMessage, "true".equalsIgnoreCase(System.getProperty("nogui")));
    }
    
    public Prompt(final String title, final String noguiMessage, final boolean nogui) {
        this.title = title;
        this.noguiMessage = ((noguiMessage == null) ? "Switching to nogui mode. You can manually start in nogui mode by including the -Dnogui=true flag." : noguiMessage);
        this.nogui = nogui;
    }
    
    public boolean isNoGUI() {
        return this.nogui;
    }
    
    public void alert(final Level level, final String context, final String message) {
        if (this.nogui) {
            final Logger log = LoggerFactory.getLogger(context);
            switch (level) {
                case INFO: {
                    log.info(message);
                    break;
                }
                case WARNING: {
                    log.warn(message);
                    break;
                }
                case ERROR: {
                    log.error(message);
                    break;
                }
                default: {
                    log.info(message);
                    break;
                }
            }
        }
        else {
            try {
                int option = 0;
                switch (level) {
                    case INFO: {
                        option = 1;
                        break;
                    }
                    case WARNING: {
                        option = 2;
                        break;
                    }
                    case ERROR: {
                        option = 0;
                        break;
                    }
                    default: {
                        option = -1;
                        break;
                    }
                }
                JOptionPane.showMessageDialog(null, "<html><body><p style='width: 400px;'>" + message, this.title, option);
            }
            catch (Exception e) {
                this.nogui = true;
                this.alert(Level.WARNING, context, this.noguiMessage);
                this.alert(level, context, message);
            }
        }
    }
    
    public String prompt(final String content) {
        if (this.nogui) {
            if (this.scanner == null) {
                this.scanner = new Scanner(System.in);
            }
            try {
                System.out.println(content);
                if (this.scanner.hasNextLine()) {
                    return this.scanner.nextLine();
                }
                return null;
            }
            catch (Exception e) {
                this.alert(Level.ERROR, this.title, "Unable to read input from command line.");
                e.printStackTrace();
                return null;
            }
        }
        try {
            return JOptionPane.showInputDialog(null, content, this.title, 3);
        }
        catch (Exception e) {
            this.nogui = true;
            this.alert(Level.WARNING, this.title, this.noguiMessage);
            return this.prompt(content);
        }
    }
    
    public enum Level
    {
        INFO, 
        WARNING, 
        ERROR;
    }
}
