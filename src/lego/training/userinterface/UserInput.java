package lego.training.userinterface;

import lego.util.Constants;

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

    public static void waitForEnter(boolean displayMessage){
        if(displayMessage){
            Print.color("[Press ENTER to continue]", Constants.COLOR_TAG_INTERACTION);
        }
        scanner.nextLine();
    }

    public static boolean askQuestion(String question){
        Print.color("[Question] (yes/no): ", Constants.COLOR_TAG_INTERACTION);
        Print.text(question);
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
