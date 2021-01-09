// 
// Decompiled by Procyon v0.5.36
// 

package com.jagrosh.jmusicbot.gui;

import com.jagrosh.jmusicbot.Bot;

import javax.swing.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class GUI extends JFrame
{
    private final ConsolePanel console;
    private final Bot bot;
    
    public GUI(final Bot bot) {
        this.bot = bot;
        this.console = new ConsolePanel();
    }
    
    public void init() {
        this.setDefaultCloseOperation(3);
        this.setTitle("JMusicBot");
        final JTabbedPane tabs = new JTabbedPane();
        tabs.add("Console", this.console);
        this.getContentPane().add(tabs);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        this.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(final WindowEvent e) {
            }
            
            @Override
            public void windowClosing(final WindowEvent e) {
                try {
                    GUI.this.bot.shutdown();
                }
                catch (Exception ex) {
                    System.exit(0);
                }
            }
            
            @Override
            public void windowClosed(final WindowEvent e) {
            }
            
            @Override
            public void windowIconified(final WindowEvent e) {
            }
            
            @Override
            public void windowDeiconified(final WindowEvent e) {
            }
            
            @Override
            public void windowActivated(final WindowEvent e) {
            }
            
            @Override
            public void windowDeactivated(final WindowEvent e) {
            }
        });
    }
}
