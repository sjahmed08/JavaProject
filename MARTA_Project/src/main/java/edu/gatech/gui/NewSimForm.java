package edu.gatech.gui;

import edu.gatech.simulation.SimDriver;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class NewSimForm {
    private JPanel simCreatePanel;
    private JTabbedPane simTabbedPane;
    private JLabel simCreateTitle;
    private JTextField stopId;
    private JTextField stopName;
    private JTextField numRiders;
    private JTextField lat;
    private JTextField lng;
    private JButton addStopButton;
    private JList stopList;
    private JButton removeSelectedStopSButton;
    private JLabel stopIdLabel;
    private JButton addRouteButton;
    private JButton removeRouteButton;
    private JTextField routeId;
    private JTextField routeNumber;
    private JTextField routeName;
    private JList routeList;
    private JTextField routeStopList;
    private JRadioButton trainSelector;
    private JRadioButton busSelector;
    private JList vehicleList;
    private JButton addVehicleButton;
    private JButton removeVehicleButton;
    private JTextField vehicleId;
    private JTextField vehicleRouteId;
    private JTextField startingStopId;
    private JTextField passengerCapacity;
    private JTextField averageVehicleSpeed;
    private JTextField startTime;
    private JTextField endTime;
    private JLabel routeCount;
    private JLabel stopCount;
    private JLabel trainCount;
    private JLabel busCount;
    private JTextField numSimIterCount;
    private JButton startSimButton;
    private JTable reviewTable;
    private HashMap<Integer, String> stopContent;
    private HashMap<Integer, String> routeContent;
    private ArrayList<String> extendRouteContent;
    private HashMap<Integer, String> vehicleContent;
    private Integer stopCountNum;
    private Integer routeCountNum;
    private Integer trainCountNum;
    private Integer busCountNum;
    private ArrayList<String> commandList;

    public NewSimForm() {
        stopContent = new HashMap<>();
        routeContent = new HashMap<>();
        vehicleContent = new HashMap<>();
        extendRouteContent = new ArrayList<>();
        DefaultListModel stopModel = new DefaultListModel();
        DefaultListModel routeModel = new DefaultListModel();
        DefaultListModel vehicleModel = new DefaultListModel();
        stopList.setModel(stopModel);
        routeList.setModel(routeModel);
        vehicleList.setModel(vehicleModel);
        stopCountNum = routeCountNum = trainCountNum = busCountNum = 0;
        commandList = new ArrayList<>();

        addStopButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                try{
                    if (isInputValid()){
                        String toAdd = "add_stop,";
                        toAdd += stopId.getText() + ",";
                        toAdd += stopName.getText() + ",";
                        toAdd += numRiders.getText() + ",";
                        toAdd += lat.getText() + ",";
                        toAdd += lng.getText();
                        stopContent.put(Integer.parseInt(stopId.getText()), toAdd);
                        stopModel.add(stopModel.size(),  stopId.getText() + ": " + stopName.getText());
                        clearFormData();
                        stopCountNum++;
                        stopCount.setText(stopCountNum.toString());
                    }
                }
                catch (FormIncompleteException fie){
                    JOptionPane.showMessageDialog(null, "Whoops! Looks like you didn't fill in all the fields. Please try again.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
                catch (IdNotUniqueException snue){
                    JOptionPane.showMessageDialog(null, "The Stop ID you entered is not Unique",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
                catch (TooFewRidersException tfre){
                    JOptionPane.showMessageDialog(null, "You've entered too few riders (zero or less) for this stop",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
                catch (NotLatLngFormatException nllfe){
                    JOptionPane.showMessageDialog(null, "Your LAT/LNG coordinates appear to be malformed. Please try again.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }

            }

            private boolean isInputValid() throws IdNotUniqueException,
                    TooFewRidersException,
                    NotLatLngFormatException,
                    FormIncompleteException {
                if (stopId.getText().equals("") || stopName.getText().equals("") || numRiders.getText().equals("") ||
                        lat.getText().equals("") || lng.getText().equals("")){
                    throw new FormIncompleteException();
                }
                if (stopContent.containsKey(Integer.parseInt(stopId.getText()))){
                    throw new IdNotUniqueException();
                }
                if (Integer.parseInt(numRiders.getText()) <= 0 ){
                    throw new TooFewRidersException();
                }
                if (!(lat.getText().contains(".") && lng.getText().contains("."))){
                    throw new NotLatLngFormatException();
                }
                return true;
            }

            private void clearFormData(){
                stopId.setText("");
                stopName.setText("");
                numRiders.setText("");
                lat.setText("");
                lng.setText("");
            }
        });

        removeSelectedStopSButton.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                String[] stopArray = stopList.getSelectedValue().toString().split(":");
                stopModel.remove(stopList.getSelectedIndex());
                stopContent.remove(Integer.parseInt(stopArray[0]));
                stopCountNum--;
                stopCount.setText(stopCountNum.toString());
            }

        });

        addRouteButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                try{
                    if (isInputValid()){
                        String toAdd = "add_route,";
                        toAdd += routeId.getText() + ",";
                        toAdd += routeNumber.getText() + ",";
                        toAdd += routeName.getText();
                        String[] stops = routeStopList.getText().split(",");
                        routeContent.put(Integer.parseInt(routeId.getText()), toAdd);
                        //extend the route by the stop list provided
                        for (String stop : stops){
                            extendRouteContent.add("extend_route,"+routeId.getText()+"," + stop);
                        }
                        routeModel.add(routeModel.size(),  routeId.getText() + ": " + routeName.getText());
                        clearFormData();
                        routeCountNum++;
                        routeCount.setText(routeCountNum.toString());
                    }
                }
                catch (FormIncompleteException fie){
                    JOptionPane.showMessageDialog(null, "Whoops! Looks like you didn't fill in all the fields. Please try again.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
                catch (IdNotUniqueException snue){
                    JOptionPane.showMessageDialog(null, "The Route ID you entered is not Unique",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
                catch (MalformedRouteStopListException mrsle){
                    JOptionPane.showMessageDialog(null, "Bargle! Looks like there was an error in the Stop List you provided. Please try again.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }

            }

            private boolean isInputValid() throws IdNotUniqueException,
                    FormIncompleteException, MalformedRouteStopListException {
                if (routeId.getText().equals("") || routeNumber.getText().equals("") || routeName.getText().equals("") ||
                        routeStopList.getText().equals("")){
                    throw new FormIncompleteException();
                }
                if (routeContent.containsKey(Integer.parseInt(routeId.getText()))){
                    throw new IdNotUniqueException();
                }
                if (!validRouteStopData()){
                    throw new MalformedRouteStopListException();
                }
                return true;
            }

            private void clearFormData(){
                routeId.setText("");
                routeNumber.setText("");
                routeName.setText("");
                routeStopList.setText("");
            }

            private boolean validRouteStopData(){
                boolean flag = true;
                if (!(routeStopList.getText().contains(","))){
                    flag = false;
                }
                String[] stops = routeStopList.getText().split(",");
                for (String stop : stops){
                    if (!(stopContent.containsKey(Integer.parseInt(stop)))){
                        flag = false;
                        break;
                    }
                }
                return flag;
            }

        });
        removeRouteButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                String[] routeArray = routeList.getSelectedValue().toString().split(":");
                routeModel.remove(routeList.getSelectedIndex());
                String toBeRemovedId = routeArray[0];
                routeContent.remove(Integer.parseInt(toBeRemovedId));
                for (String routeExtension : extendRouteContent){
                    String[] content = routeExtension.split(",");
                    //extend_route is in position 0, making route id in position 1
                    if (content[1].equals(toBeRemovedId)){
                        extendRouteContent.remove(routeExtension);
                    }
                }
                routeCountNum--;
                routeCount.setText(routeCountNum.toString());
            }
        });

        addVehicleButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                try{
                    if (isInputValid()){
                        String toAdd = "add_vehicle,";
                        toAdd += vehicleId.getText() + ",";
                        toAdd += vehicleRouteId.getText() + ",";
                        toAdd += startingStopId.getText() + ",";
                        toAdd +=  "0,"; //magic number to represent that the vehicle is starting empty
                        toAdd += passengerCapacity.getText() + ",";
                        toAdd += averageVehicleSpeed.getText() + ",";
                        toAdd += trainSelector.isSelected();
                        vehicleContent.put(Integer.parseInt(vehicleId.getText()), toAdd);
                        vehicleModel.add(vehicleModel.size(),  vehicleId.getText() + ": " + (trainSelector.isSelected() ? "Train" : "Bus"));
                        if(trainSelector.isSelected()){
                            trainCountNum++;
                            trainCount.setText(trainCountNum.toString());
                        }else{
                            busCountNum++;
                            busCount.setText(busCountNum.toString());
                        }
                        clearFormData();
                    }
                }
                catch (FormIncompleteException fie){
                    JOptionPane.showMessageDialog(null, "Whoops! Looks like you didn't fill in all the fields. Please try again.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
                catch (IdNotUniqueException snue){
                    JOptionPane.showMessageDialog(null, "The Route ID you entered is not Unique",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
                catch (TooFewRidersException tfre){
                    JOptionPane.showMessageDialog(null, "Please enter a number greater than zero for Vehicle capacity",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
                catch (TooSlowSpeedException tsse){
                    JOptionPane.showMessageDialog(null, "Please enter a number greater than zero for Vehicle speed",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
                catch (NonExistantRouteException nere){
                    JOptionPane.showMessageDialog(null, "Looks like that route doesn't exist. Check that one more time and try again.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
                catch (NonExistantStopException nese){
                    JOptionPane.showMessageDialog(null, "Looks like that stop doesn't exist. So we can't start the vehicle there.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }

            }

            private boolean isInputValid() throws IdNotUniqueException,
                    FormIncompleteException, TooFewRidersException,
                    NonExistantRouteException, NonExistantStopException, TooSlowSpeedException {
                if (vehicleId.getText().equals("") || vehicleRouteId.getText().equals("") ||
                        startingStopId.getText().equals("") || passengerCapacity.getText().equals("")
                        || averageVehicleSpeed.getText().equals("") || ((!trainSelector.isSelected())
                        && (!busSelector.isSelected()))){

                    throw new FormIncompleteException();
                }
                if (vehicleContent.containsKey(Integer.parseInt(vehicleId.getText()))){
                    throw new IdNotUniqueException();
                }
                if (!(routeContent.containsKey(Integer.parseInt(vehicleRouteId.getText())))){
                    throw new NonExistantRouteException();
                }
                if (!(stopContent.containsKey(Integer.parseInt(startingStopId.getText())))){
                    throw new NonExistantStopException();
                }
                if (Integer.parseInt(passengerCapacity.getText()) <= 0){
                    throw new TooFewRidersException();
                }
                if (Integer.parseInt(averageVehicleSpeed.getText()) <= 0){
                    throw new TooSlowSpeedException();
                }

                return true;
            }

            private void clearFormData(){
                vehicleId.setText("");
                vehicleRouteId.setText("");
                startingStopId.setText("");
                passengerCapacity.setText("");
                averageVehicleSpeed.setText("");
                trainSelector.setSelected(false);
                busSelector.setSelected(false);
            }


        });
        removeVehicleButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                String[] vehicleArray = vehicleList.getSelectedValue().toString().split(":");
                vehicleModel.remove(vehicleList.getSelectedIndex());
                String toBeRemovedId = vehicleArray[0];
                String[] vehicleHashMapArray = vehicleContent.get(Integer.parseInt(toBeRemovedId)).split(",");
                Boolean isTrain = Boolean.parseBoolean(vehicleHashMapArray[vehicleHashMapArray.length - 1]);
                vehicleContent.remove(Integer.parseInt(toBeRemovedId));
                if (isTrain){
                    trainCountNum--;
                    trainCount.setText(trainCountNum.toString());
                } else {
                    busCountNum--;
                    busCount.setText(busCountNum.toString());
                }
            }
        });
        trainSelector.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                if (busSelector.isSelected()){
                    busSelector.setSelected(false);
                }
            }
        });
        busSelector.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                if (trainSelector.isSelected()){
                    trainSelector.setSelected(false);
                }
            }
        });
        startTime.getDocument().addDocumentListener(new DocumentListener() {
            ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);
            @Override
            public void insertUpdate(DocumentEvent e) {
                exec.schedule(new Runnable() {
                    public void run() {
                        warn();
                    }
                }, 1, TimeUnit.SECONDS);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                exec.schedule(new Runnable() {
                    public void run() {
                        warn();
                    }
                }, 1, TimeUnit.SECONDS);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                exec.schedule(new Runnable() {
                    public void run() {
                        warn();
                    }
                }, 1, TimeUnit.SECONDS);
            }
            public void warn() {
                int startTimeNum = Integer.parseInt(startTime.getText());
                if (startTimeNum < 0 || startTimeNum > 23){
                    JOptionPane.showMessageDialog(null,
                            "Error: Please enter a valid hour between 0 and 23", "ERROR",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        endTime.getDocument().addDocumentListener(new DocumentListener() {
            ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);
            @Override
            public void insertUpdate(DocumentEvent e) {
                exec.schedule(new Runnable() {
                    public void run() {
                        warn();
                    }
                }, 1, TimeUnit.SECONDS);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                exec.schedule(new Runnable() {
                    public void run() {
                        warn();
                    }
                }, 1, TimeUnit.SECONDS);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                exec.schedule(new Runnable() {
                    public void run() {
                        warn();
                    }
                }, 1, TimeUnit.SECONDS);
            }
            public void warn() {
                int endTimeNum = Integer.parseInt(endTime.getText());
                if (endTimeNum < 0 || endTimeNum > 23) {
                    JOptionPane.showMessageDialog(null,
                            "Error: Please enter a valid hour between 0 and 23", "ERROR",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        startSimButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                if (isNumIterValid()){
                    constructCommandList();
                    saveCommandListToDisk();
                    File file = new File("system_generated_sim.txt");
                    SimDriver commandInterpreter = new SimDriver();
                    try{
                        commandInterpreter.runInterpreter(file);
                    }catch (FileNotFoundException fnfe){
                        System.out.println("HELP!");
                        JOptionPane.showMessageDialog(null,
                                "Fatal System Error, we were unable to load the simulation.", "ERROR",
                                JOptionPane.ERROR_MESSAGE);
                    }

                }
            }
            private boolean isNumIterValid(){
                return (Integer.parseInt(numSimIterCount.getText()) > 0);
            }
            private void constructCommandList(){
                //stops first
                for (Map.Entry<Integer, String> entry : stopContent.entrySet()) {
                    commandList.add(entry.getValue());
                }
                //then routes
                for (Map.Entry<Integer, String> entry : routeContent.entrySet()) {
                    commandList.add(entry.getValue());
                    commandList.add("add_event,1,adjust_traffic," + entry.getKey());
                }
                //now extend routes
                for (String routeExtension : extendRouteContent){
                    commandList.add(routeExtension+",1.0,70,10");
                }
                //now add vehicles
                for (Map.Entry<Integer, String> entry : vehicleContent.entrySet()) {
                    commandList.add(entry.getValue());
                    commandList.add("add_event,0,move_vehicle,"+entry.getKey());
                }
                // now the schedule
                commandList.add("set_schedule,"+startTime.getText()+","+endTime.getText());

                //finalize with step_multi and system_report
                commandList.add("drop_rider_table");
                commandList.add("add_event,1,add_riders,0");
                commandList.add("create_rider,100");
                commandList.add("step_multi,"+numSimIterCount.getText()+",100,1,1");
                commandList.add("system_report");
                commandList.add("display_model");
                commandList.add("quit");

            }
            private void saveCommandListToDisk(){
                try{
                    FileWriter writer = new FileWriter("system_generated_sim.txt", false);
                    for(String str: commandList) {
                        writer.append(str+"\n");
                    }
                    writer.close();
                } catch (IOException ioe){
                    //help
                }
            }
        });
    }


    public JPanel getSimCreatePanel() {
        return simCreatePanel;
    }


}
