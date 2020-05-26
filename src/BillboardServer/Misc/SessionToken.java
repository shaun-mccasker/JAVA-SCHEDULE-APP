package BillboardServer.Misc;

import java.time.Instant;
import java.util.HashMap;
import java.util.Random;

// Git may say I wrote this but it's because I copy pasted Brandon's code

public class SessionToken {
    private static final HashMap<String, Long> sessionTokenStorage = new HashMap<>(); // Used for storing the session token generated in login case + time of login.
    private static final HashMap<String, String> usersSessionTokens = new HashMap<>(); // Used to keep track of the session token for each username.
    private static final Random rng = new Random(); // Used for generating random bytes in createSessionToken method.
    /**
     * Converts bytes to a string. This is used in the createSessionToken method to convert the randomly generated bytes to a string.
     * @param hash
     * @return Returns a string.
     */
    // From week 10 assignment Q&A lecture.
    private static String bytesToString(byte[] hash){
        StringBuffer sb = new StringBuffer();
        for (byte b : hash){
            sb.append(String.format("%02x", b & 0xFF));
        }
        return sb.toString();
    }

    public static String getUser(String token){
        return usersSessionTokens.get(token);
    }

    /**
     * Creates a session token, which is used to verify that the user currently logged in was logged in properly.
     * This session token is then used in each request to check that the user is still logged in.
     * It also commits this token to the storage
     */
    // From Week 10 Assignment Q&A Lecture.
    public static String createSessionToken(String username){
        byte[] randomBytes = new byte[32];
        rng.nextBytes(randomBytes);
        String session_token = bytesToString(randomBytes); // Go back to see the bytestoString method.
        Long loginUnixTime = Instant.now().getEpochSecond(); // Gets the time elapsed in seconds since unix epoch.
        sessionTokenStorage.put(session_token, loginUnixTime); // Need to store the current time.
        usersSessionTokens.put(session_token, username); // Store the username too
        return session_token;
    }

    /**
     * Checks if the session token sent by the control panel is valid or not.
     * Used in verifying user before commands in the Parse method.
     * @param sessionToken The session token that the client uses is entered here.
     * @return Returns the boolean returnStatement. If the hashmap validSessionToken contains @param sessionToken, then this is true.
     */
    // Based on week 10 assignment Q&A lecture.
    public static boolean isSessionTokenValid(String sessionToken){
        boolean returnStatment = false;
        // If the sessionToken entered by client exists, checks whether 24 hours has passed from the current command attempt timestamp, from the sessionToken timestamp, measured in seconds.
        if (sessionTokenStorage.containsKey(sessionToken)){
            Long loginTimeLong = sessionTokenStorage.get(sessionToken); // Gets the login time value associated to the session token entered, from the hashmap validSessionToken.
            Long commandTimeUnix = Instant.now().getEpochSecond(); // Gets the time elapsed in seconds since unix epoch for the current command.
            Long differenceBetweenTimes = commandTimeUnix - loginTimeLong;
            returnStatment = differenceBetweenTimes < 86400;
        }

        return returnStatment;
    }

    /**
     * Used in the deleteUser case. Ensures that a logged in user cannot delete themselves from the system.
     * Checks the usersSessionTokens hashmap to see if the sessionToken sent by the client belongs to the username to be deleted.
     * If it does, then the user is attempting to delete themselves, which means that deleteUser case will not execute.
     * @param username The username entered by the client that is to be checked if it can be deleted.
     * @param sessionToken The session token sent by the client.
     * @return
     */
    public static boolean doesUserMatchSessionTokenFromClient(String sessionToken, String username){
        boolean returnStatement = false;
        // Checks that the sessionToken of the current user is not a value of the current users name in the hashmap usersSessionTokens.
        returnStatement = (usersSessionTokens.get(sessionToken).equals(username));
//        System.out.println(usersSessionTokens.get(username));
//        System.out.println("in new bool function");
        return returnStatement;
    }

}
