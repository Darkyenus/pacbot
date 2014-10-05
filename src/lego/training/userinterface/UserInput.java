package lego.training.userinterface;

import lego.util.Constants;

import java.io.IOException;
import java.util.Scanner;

/**
 * Created by jIRKA on 5.10.2014.
 */
public class UserInput {

    public static void waitForEnter(boolean displayMessage){
        if(displayMessage){
            Print.color("[Press ENTER to continue]", Constants.COLOR_TAG_INTERACTION);
        }
        try {
            System.in.read();
            if (System.in.available() > 0) {
                System.in.read(new byte[System.in.available()]);
            }
        }catch(IOException e){}
    }

    public static boolean askQuestion(String question){
        Print.color("[Question] (yes/no): ", Constants.COLOR_TAG_INTERACTION);
        Print.text(question);
        Scanner sc = new Scanner(System.in);
        String line = sc.nextLine();
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
