package edu.gatech.gui;

import edu.gatech.simulation.SimDriver;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;

public class HomePageForm {

    private JPanel mainPanel;
    private JLabel title;
    private JLabel welcomeText;
    private JButton fileUploadButton;
    private JButton createSimButton;
    private JFileChooser fc;


    public HomePageForm() {
        fc = new JFileChooser();
        fileUploadButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                int returnVal = fc.showOpenDialog(mainPanel);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    SimDriver commandInterpreter = new SimDriver();
                    try {
                        commandInterpreter.runInterpreter(file);
                    } catch (FileNotFoundException fnfe){
                        System.out.println("Sorry, but we couldn't find that file, please try again!");
                    }
                }
            }
        });
        createSimButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                NewSimForm newSimForm = new NewSimForm();
                //hackity hack hack hack just because I know who wrote the GUI means I can do this
                //otherwise I'd have to do a card layout and that is a really weak suit for me
                JButton aButton = (JButton)e.getSource();
                JPanel aPanel = (JPanel)aButton.getParent();
                JLayeredPane aPane = (JLayeredPane)aPanel.getParent();
                JRootPane theRootPane = (JRootPane)aPane.getParent();
                JFrame theFrame =  (JFrame)theRootPane.getParent();
                theFrame.setContentPane(newSimForm.getSimCreatePanel());
                theFrame.pack();
            }
        });
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }
}
