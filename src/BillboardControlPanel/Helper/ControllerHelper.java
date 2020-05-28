package BillboardControlPanel.Helper;

import BillboardControlPanel.ServerUtilities.ServerRequestClient;
import BillboardControlPanel.Controller.MainController;
import BillboardControlPanel.View.MainView;
import BillboardControlPanel.View.ManageUserCard;
import BillboardControlPanel.View.MasterView;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.Socket;
import javax.swing.*;
import java.awt.*;
import java.security.*;

public class ControllerHelper {


    public static MainView updateFrame(MainView mainView, MasterView masterView){
        mainView.getMainFrame().getContentPane().removeAll();
        mainView.getMainFrame().getContentPane().add(masterView.getNorthCard(), BorderLayout.NORTH);
        mainView.getMainFrame().getContentPane().add(masterView.getCenterCard(), BorderLayout.CENTER);
        mainView.getMainFrame().getContentPane().add(masterView.getSouthCard(), BorderLayout.SOUTH);
        mainView.getMainFrame().validate();
        mainView.getMainFrame().repaint();
        return mainView;
    }

    public static void resetJTextFields(JTextField[] jTextField){
        for (JTextField item: jTextField) {
            item.setText("");
        }
    }

    public static void refreshUsersTablePanel(){
        MainController.setUserData();
        MainController.setUserColNames();
        MainController.getManageUserController().setSelectedCol(-1);
        MainController.getManageUserController().setSelectedRow(-1);
        MainController.getManageUserController().initView();
        MainController.getManageUserController().initController(MainController.getManageUserController().getManageUserCard());
        ControllerHelper.updateFrame(MainController.getMainView(), MainController.getManageUserController().getManageUserCard());
    }

    public static void refreshBillBoardTablePanel(){
        MainController.setBillData();
        MainController.getManageBillboardController().setSelectedCol(-1);
        MainController.getManageBillboardController().setSelectedRow(-1);
        MainController.getManageBillboardController().initView();
        MainController.getManageBillboardController().initController(MainController.getManageBillboardController().getManageBillboardCard());
        ControllerHelper.updateFrame(MainController.getMainView(), MainController.getManageBillboardController().getManageBillboardCard());
    }

    public static int confirmPopUp(String message){
        int action = JOptionPane.showConfirmDialog(null, message, "confirm", JOptionPane.OK_CANCEL_OPTION);
        return action;
    }

    public static void returnMessage(String message){
        JOptionPane.showMessageDialog(null, message, null, JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * This is a cheat way of implementing a parent for the controllers, it will add the functionality of global use buttons, in this case logout and home button
     * @param masterView current viewCard
     */
    public static void enableGlobalButtons(MasterView masterView){
        masterView.getBtnHome().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ControllerHelper.updateFrame(MainController.getMainView(), MainController.getHomeController().getHomeCard());
            }
        });
        masterView.getBtnLogOut().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //load login panel
                int action = confirmPopUp("Logout?");
                switch (action){
                    case (JOptionPane.OK_OPTION):
                        //reset user variables
                        MainController.setLoggedUser("");
                        MainController.setSessionToken("");
                        ControllerHelper.updateFrame(MainController.getMainView(), MainController.getLoginController().getLoginCard());
                        break;
                    case (JOptionPane.CANCEL_OPTION):
                        break;
                }
            }
        });
    }

    public static Boolean checkConnection(){
            Boolean check = false;
            int timesFailed = 0;
            while(!check) {
                Socket socket = ServerRequestClient.initServerConnect();
                if(socket == null){
                    timesFailed++;
                } else {
                    try{
                        socket.close();
                        return true;
                    } catch (IOException e){
                        System.err.println(e.getMessage());
                    }
                }
                if(timesFailed == 1){
                    return false;
                }
            }
            return null;
    }

    //USER RELATED HELPERS

    public static void setNewPassword(String userToUpdate, ManageUserCard manageUserCard, String sessionToken){
        int passwordBox = ManageUserCard.createFrameTextInputBox(userToUpdate, "password");
        switch (passwordBox) {
            case (JOptionPane.OK_OPTION):
                String newPassword = manageUserCard.getUpdateTextField().getText();
                String newHashedPassword = ControllerHelper.createSecurePassword(newPassword);
                ControllerHelper.confirmPopUp("Password changed to: " + newPassword);
                String[] res = ServerRequestClient.setUserPassword(userToUpdate, newHashedPassword, sessionToken);
                if(res[0].equalsIgnoreCase("false")){
                    System.out.println("failed");
                }
                break;
            case (JOptionPane.CANCEL_OPTION):
                break;
        }
    }

    public static String createSecurePassword(String passwordToHash) {
        String securePassword = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(passwordToHash.getBytes());
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bytes.length; ++i) {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            securePassword = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return securePassword;
    }
}
