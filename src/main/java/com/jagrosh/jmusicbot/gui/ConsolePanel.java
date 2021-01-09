// 
// Decompiled by Procyon v0.5.36
// 

package com.jagrosh.jmusicbot.gui;

import javax.swing.*;
import java.awt.*;
import java.io.PrintStream;

public class ConsolePanel extends JPanel
{
    public ConsolePanel() {
        final JTextArea text = new JTextArea();
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setEditable(false);
        final PrintStream con = new PrintStream(new TextAreaOutputStream(text));
        System.setOut(con);
        System.setErr(con);
        final JScrollPane pane = new JScrollPane();
        pane.setViewportView(text);
        super.setLayout(new GridLayout(1, 1));
        super.add(pane);
        super.setPreferredSize(new Dimension(400, 300));
    }
}
