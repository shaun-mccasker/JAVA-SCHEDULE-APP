package BillboardControlPanel.ServerUtilities;

import BillboardServer.Misc.ReadNetworkProps;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDateTime;

/**
 * This class has lots of methods for requesting the server to perform actions
 * Most methods create a string array where the first element is the command, and the rest the function params.
 * This array is the main method of communication between the client and the server. A string array is sent, and a string array is returned.
 * The way the string array is sent should be abstracted out by just using the provided methods
 * See the comment for sendQuery below for information on the array which is returned (This is not finalised, suggestions are welcome)
 */
public class ServerRequestClient  {

    public static Socket initServerConnect(){
        try{
            Socket socket = new Socket (ReadNetworkProps.getHost(), ReadNetworkProps.getPort());
            socket.setSoTimeout(4000); // Set a two second timeout on read operations. After two seconds of nothing being read, an exception will be thrown
            return socket;
        } catch (IOException e){
            System.out.println(e);
            return  null;
        }
    }

    /**
     * Sends a given string array in a specific format, then listens for and returns the result
     * This is called from all methods below, and you do not need to called this directly
     * @param queryArray The array to be sent to the server
     *
     * @return Null if the server timed out, otherwise a string array which is formatted:
     *      "true" or "false" (Did the command run successfully on the server or not? This describes the state the database operation) it's a sting representation of a bool,
     *      "A string of results" Not many functions use this currently, getBillboard does as an example, it would return the xml string from the database. For a function like delete user, this will just be "".
     *      "Optional message that could be displayed to the user/debugging purposes", Just print this to the console for greater verbosity
     * See the SendBackData function on the server for more the code responsible
     */
    public static String[] sendQuery(String[] queryArray){
        try {
            Socket socket = new Socket (ReadNetworkProps.getHost(), ReadNetworkProps.getPort());
            socket.setSoTimeout(4000); // Set a two second timeout on read operations. After two seconds of nothing being read, an exception will be thrown
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            oos.writeObject(queryArray);
            oos.flush();
            // Now listen for a response, no loop is required because there will only be a single response from the server for now, then return the results
            Object Response = null;
            try{
                Response = ois.readObject();
            }
            catch(Exception e){
                System.out.println(e.getMessage());
                return null;
            }
            try{
                return (String[])Response;
            }
            catch (Exception e ){
                System.out.println(e.getMessage());
                return null;
            }
        } catch (IOException e){ //This is caught from lots of the networking functions, probably going to happen if the server isn't up, or network props file doesn't exist and a weird socket is made
            e.printStackTrace();
            return  null;
        }
    }

    // Same as above but returns a string[][] for use in certain functions, like list users
    // result[0] will be the same as the normal return above, all extra indices will be the results
    public static String[][] sendQueryAlt(String[] queryArray){
        try {
            Socket socket = new Socket(ReadNetworkProps.getHost(), ReadNetworkProps.getPort());
            socket.setSoTimeout(4000); // Set a two second timeout on read operations. After two seconds of nothing being read, an exception will be thrown
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            oos.writeObject(queryArray);
            oos.flush();
            // Now listen for a response, no loop is required because there will only be a single response from the server for now, then return the results
            Object Response = null;
            try {
                Response = ois.readObject();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                return null;
            }
            try { // The returned object from the server will be a double string array if the command was executed successfully
                return (String[][]) Response;
            } catch (Exception e) {
                //System.out.println(e.getMessage());
            }
            try { // The returned object from the server will be a single array if the command failed, so it needs to be formatted properly to be returned to the client
                String[] returnedErrorArray = (String[]) Response;
                String[] emptyArray = new String[]{""};
                return new String[][]{returnedErrorArray, emptyArray};
            } catch (Exception e) {
                System.out.println(e.getMessage());
                return null;
            }
        } catch (IOException e){
            e.printStackTrace();
            return  null;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // USERS
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Creates a user in the database, and associated permissions in the permission table
     * @param username username for the new user
     * @param password  password for the new user (currently just send it plaintext)
     * @param create_billboard 0 or 1 for disabled or enabled
     * @param edit_billboard 0 or 1 for disabled or enabled
     * @param schedule_billboard 0 or 1 for disabled or enabled
     * @param edit_user 0 or 1 for disabled or enabled
     * @param sessionToken A session token so the server can authenticate the request
     * @return See ServerRequest.sendQuery
     */
    public static String[] createUser(String username, String password, Integer create_billboard, Integer edit_billboard, Integer schedule_billboard, Integer edit_user, String sessionToken){
        String[] command = {"createUser", username, password, create_billboard.toString(), edit_billboard.toString(), schedule_billboard.toString(), edit_user.toString(), sessionToken};
        return sendQuery(command);
    }

    /**
     * Deletes a user from the database, and associated permissions
     * @param username username of the user to delete
     * @param sessionToken A session token so the server can authenticate the request
     * @return See ServerRequest.sendQuery
     */
    public static String[]  deleteUser(String username, String sessionToken) {
        String[] command = {"deleteUser", username, sessionToken};
        return sendQuery(command);
    }

    /**
     * Lists all users in the DB
     * @param sessionToken A session token so the server can authenticate the request
     * @return See ServerRequest.sendQueryDoubleArray
     */
    public static String[][]  listUsers(String sessionToken) {
        String[] command = {"listUsers", sessionToken};
        return sendQueryAlt(command);
    }

    /**
     * Allows you to login to access other functions
     * @param username The username the password is for
     * @param password The password, which has to match the stored password (only plaintext right now)
     * @return See ServerRequest.sendQuery
     */
    public static String[]  login(String username, String password) {
        String[] command = {"login", username, password};
        return sendQuery(command);
    }

    /**
     * Expires the currently logged in users session token.
     * @param sessionToken The currently logged in users' session token.
     * @return See ServerRequest.sendQuery
     */
    public static String[] logout(String sessionToken) {
        String[] command = {"logout", sessionToken};
        return sendQuery(command);
    }

    /**
     * Edit permissions associated with a username
     * @param username The user to edit permissions for
     * @param create_billboard 0 or 1 for disabled or enabled
     * @param edit_billboard 0 or 1 for disabled or enabled
     * @param schedule_billboard 0 or 1 for disabled or enabled
     * @param edit_user 0 or 1 for disabled or enabled
     * @return See ServerRequest.sendQuery
     */
    public static String[]  editAllPermissions(String username, Integer create_billboard, Integer edit_billboard, Integer schedule_billboard, Integer edit_user, String sessionToken) {
        String[] command = {"editPermission", username, create_billboard.toString(), edit_billboard.toString(), schedule_billboard.toString(), edit_user.toString(), sessionToken};
        return sendQuery(command);
    }

    public static String[][] getPermissions(String username, String sessionToken) {
        String[] command = {"getPermissions", username, sessionToken};
        return sendQueryAlt(command);
    }

    /**
     * This function makes use of the initial use of editing permissions by formatting it in such a way so only one value will be changed
     * @param username The user to edit permissions for
     * @param permission Either create_billboard, edit_billboard, schedule_billboard, or edit_user
     * @param value 1 or 0
     */
    public static String[] editPermission(String username, String permission, Integer value, String sessionToken) {
        switch (permission) {
            case "create_billboard":
                return editAllPermissions(username, value, -1, -1, -1, sessionToken); // -1 is so the value doesn't change on the server side

            case "edit_billboard":
                return editAllPermissions(username, -1, value, -1, -1, sessionToken);

            case "schedule_billboard":
                return editAllPermissions(username, -1, -1, value, -1, sessionToken);

            case "edit_user":
                return editAllPermissions(username, -1, -1, -1, value, sessionToken);

        }
        return new String[]{"false", "", "column " + permission + " doesn't exist"};
    }

    public static String[] setUserPassword(String username, String password, String sessionToken) {
        String[] command = {"setUserPassword", username, password, sessionToken};
        return sendQuery(command);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // BILLBOARD
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Creates a billboard if none exists, edits existing billboard otherwise.
     * @param billboardName The name of the billboard, as a string value.
     * @param xml_string The xml data which you wish to assign to the billboard
     * @param sessionToken A session token so the server can authenticate the request
     * @return See ServerRequest.sendQuery
     */
    public static String[]  createOrEditBillboard(String billboardName, String xml_string, String sessionToken) {
        String[] command = {"createBillboard", billboardName, xml_string, sessionToken};
        return sendQuery(command);
    }

    /**
     * Gets the value under xml_data for the provided billboardName.
     * This will be at index 1 of the results ie. second position in array
     * @param billboardName Name of the billboard.
     * @param sessionToken A session token so the server can authenticate the request
     * @return See ServerRequest.sendQuery
     */
    public static String[]  getBillboard(String billboardName, String sessionToken) {
        String[] command = {"getBillboard", billboardName, sessionToken};
        return sendQuery(command);
    }

    /**
     *  Deletes the billboard with the name entered.
     * @param billboardName Name of the billboard to be deleted
     * @param sessionToken A session token so the server can authenticate the request
     * @return See ServerRequest.sendQuery
     */
    public static String[]  deleteBillboard(String billboardName, String sessionToken) {
        String[] command = {"deleteBillboard", billboardName, sessionToken};
        return sendQuery(command);
    }

    /**
     * Lists all billboards in the DB with creator name
     * @param sessionToken A session token so the server can authenticate the request
     * @return See ServerRequest.sendQueryDoubleArray
     */
    public static String[][]  listBillboards(String sessionToken) {
        String[] command = {"listBillboards", sessionToken};
        return sendQueryAlt(command);
    }

    /**
     * Get the contents of the currently scheduled billboard, or a placeholder
     * @return See ServerRequest.sendQueryDoubleArray
     */
    public static String[]  getCurrentBillboard() {
        String[] command = {"getCurrentBillboard"};
        return sendQuery(command);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // SCHEDULE
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Adds one schedule to the database for one billboard
     * @param billboardName The billboard to associate the schedule with
     * @param startTime When the billboard should start displaying, currently a LocalDateTime object
     * @param durationSec Duration in seconds to be displayed from the startTime
     * @param timeToRecur The time in seconds after which the schedule should repeat
     * @param sessionToken A session token so the server can authenticate the request
     * @return See ServerRequest.sendQuery
     */
    public static String[] createSchedule(String billboardName, LocalDateTime startTime, Integer durationSec, Integer timeToRecur, String sessionToken) {
        String[] command = {"addSchedule", billboardName, startTime.toString(), durationSec.toString(), timeToRecur.toString(), sessionToken};
        return sendQuery(command);
    }

    /**
     *  Deletes a schedule from the database which matches the billboard name and time.
     *  This may delete multiple schedules if they match
     *  (there should probably be restriction, server or client side or both, so you cant schedule two billboards at the exact same time for the exact same length)
     * @param billboardName Name of the billboard the schedule is for
     * @param startTime Time the billboard is scheduled to start
     * @param sessionToken A session token so the server can authenticate the request
     * @return See ServerRequest.sendQuery
     */
    public static String[]  deleteSchedule(String billboardName, LocalDateTime startTime, String sessionToken) {
        String[] command = {"deleteSchedule", billboardName, startTime.toString(), sessionToken};
        return sendQuery(command);
    }

    /**
     * Lists all billboards in the DB with creator name
     * @param sessionToken A session token so the server can authenticate the request
     * @return See ServerRequest.sendQueryDoubleArray
     */
    public static String[][]  listSchedules(String sessionToken) {
        String[] command = {"listSchedules", sessionToken};
        return sendQueryAlt(command);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // OTHER
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static String[][] getColumnNames(String table_name, String sessionToken) {
        String[] command = {"getColumns", table_name, sessionToken};
        return sendQueryAlt(command);
    }


    ////// helpers

    //this function is really scuffed
    public static String[] getFormattedUserColumnNames(String sessionToken){
        String[] user = getColumnNames("user", sessionToken)[1]; //value 0 is header 1 is reponse
        String[] perm = getColumnNames("permission", sessionToken)[1];
        int lengthOfUserArray = user.length -1; //-1 becuase i dont want salt value
        int LengthOfPermArray = perm.length -2; //-2 bceause i dont awnt id or user_id
        int lengthOfArray = lengthOfUserArray + LengthOfPermArray;
        String[] appendedArray = new String[lengthOfArray];
        for(int i = 0; i < lengthOfUserArray; i++) {
            appendedArray[i] = user[i];
        }
        for(int i = lengthOfUserArray; i < lengthOfArray; i++){
            appendedArray[i] = perm[i - lengthOfUserArray + 2]; //scuffed way of not getting first 2 values from an array
        }
        return appendedArray;
    }

    public static String[] getFormattedUserPrivs(String currentLoggedUser, String sessionToken){
        String[][] userPerm = getPermissions(currentLoggedUser, sessionToken);
        userPerm = removeHeaderFromDoubleArray(userPerm);
        String[] formattedUserPerm = userPerm[0];
        return formattedUserPerm;
    }

    public static String[][] removeHeaderFromDoubleArray(String[][] userList){
        try{
            int arrayLength = userList.length - 1; // -1 to compensate for removing header
            int arrayItemLength = userList[1].length;
            String[][] formattedList = new String[arrayLength][arrayItemLength];
            for(int i = 0; i < arrayLength; i ++){
//            System.out.println("Item: " + i);
                for(int j = 0; j < arrayItemLength; j++){
//                System.out.println("Body: " +j + " Data: " + userList[i+1][j]);
                    formattedList[i][j] = userList[i+1][j];               //i+1 to ignore header
                }
            }
            return formattedList;
        } catch (NullPointerException e){
            System.err.println(e);
            return userList;
        }

    }


}
