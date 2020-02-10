package edu.gatech.simulation;

import edu.gatech.gui.HomePageForm;

import javax.swing.*;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        System.out.println("Mass Transit Simulation System Starting...");

        JFrame frame = new JFrame("MARTA Simulation");
        frame.setContentPane(new HomePageForm().getMainPanel());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

    }

}
