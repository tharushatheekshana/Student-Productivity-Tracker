/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tracker.mvc.swing;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JLabel;

/**
 *
 * @author Theekshana
 */
public class FadeLabel extends JLabel {

    private float alpha = 0f; // transparency
    private int arc = 25;     // rounded corners
    private int shadowSize = 8; // shadow thickness

    public FadeLabel(String text) {
        super(text, CENTER);
        setOpaque(false); // we paint custom background
    }

    public void setAlpha(float alpha) {
        this.alpha = Math.max(0f, Math.min(1f, alpha));
        repaint();
    }

    public float getAlpha() {
        return alpha;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();

        // Anti-aliasing
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Apply alpha transparency
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        int w = getWidth();
        int h = getHeight();

        // Draw shadow
        g2.setColor(new Color(0, 0, 0, 60));
        g2.fillRoundRect(shadowSize, shadowSize, w - shadowSize, h - shadowSize, arc, arc);

        // Draw background
        g2.setColor(new Color(0, 180, 0, 220));
        g2.fillRoundRect(0, 0, w - shadowSize, h - shadowSize, arc, arc);

        g2.dispose();
        super.paintComponent(g);
    }
}
