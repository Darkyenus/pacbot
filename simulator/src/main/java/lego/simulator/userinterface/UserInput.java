package lego.simulator.userinterface;

import java.util.Scanner;

/**
 * Created by jIRKA on 5.10.2014.
 */
public class UserInput {

    private static Scanner scanner = new Scanner(System.in);

    public static Scanner getUserInputScanner(){
        return scanner;
    }

    public static void setNewUserInputScanner(Scanner userInputScanner){
        scanner = userInputScanner;
    }

    public static boolean waitForEnterCancelable(boolean displayMessage){
        if(displayMessage){
            Print.text("[Waiting] ");
            Print.text("Press ENTER to continue or type 'Cancel' ");
        }
        String data = scanner.nextLine();
        return !"cancel".equalsIgnoreCase(data);
    }

    public static void waitForEnter(boolean displayMessage){
        if(displayMessage){
            Print.text("[Waiting] ");
            Print.text("Press ENTER to continue");
        }
        scanner.nextLine();
    }

    public static boolean askQuestion(String question){
        Print.text("[Question] (yes/no): ");
        Print.text(question);
        Print.text(" ");
        String line = scanner.nextLine();
        if("yes".equals(line.toLowerCase())){
            return true;
        }else if("no".equalsIgnoreCase(line.toLowerCase())){
            return false;
        }else{
            Print.error("Invalid answer, please answer yes or no, nothing else.");
            return askQuestion(question);
        }
    }

}
