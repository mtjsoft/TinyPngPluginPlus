package cn.mtjsoft.tinypng.plugin.gui;

import com.intellij.ui.Gray;
import com.intellij.ui.JBColor;

import javax.swing.*;
import javax.swing.plaf.basic.BasicProgressBarUI;
import java.awt.*;

public class ProgressUI extends BasicProgressBarUI {

    private final JProgressBar jProgressBar;

    public ProgressUI(JProgressBar jProgressBar) {
        this.jProgressBar = jProgressBar;
    }

    @Override
    protected void paintDeterminate(Graphics g, JComponent c) {
        this.jProgressBar.setBackground(Gray._255);
        int progressvalue = this.jProgressBar.getValue();
        if (progressvalue < 20) {
            this.jProgressBar.setForeground(JBColor.BLUE);
        } else if (progressvalue < 40) {
            this.jProgressBar.setForeground(JBColor.YELLOW);
        } else if (progressvalue < 60) {
            this.jProgressBar.setForeground(JBColor.RED);
        } else if (progressvalue < 80) {
            this.jProgressBar.setForeground(JBColor.GREEN);
        } else {
            this.jProgressBar.setForeground(JBColor.CYAN);
        }
        super.paintDeterminate(g, c);
    }

}
