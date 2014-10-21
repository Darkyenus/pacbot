package lego.simulator.userinterface;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by jIRKA on 4.10.2014.
 */
public class Print {

    private static OutputStream outStream = System.out;

    private static OutputStream loggingStream = null;

    /**
     * Prints message with trailing linebreak
     */
    public static void line(String message){
        if(RenderPermissions.renderGeneralText()) {
            try {
                outStream.write(message.getBytes());
                outStream.write('\n');
                if (loggingStream != null) {
                    loggingStream.write(message.getBytes());
                    loggingStream.write('\n');
                }
            } catch (IOException e) {
                error(e.toString());
            }
        }
    }

    /**
     * Prints not message without trailing linebreak.
     */
    public static void text(String message){
        if(RenderPermissions.renderGeneralText()) {
            try {
                outStream.write(message.getBytes());
                if (loggingStream != null) {
                    loggingStream.write(message.getBytes());
                }
            } catch (IOException e) {
                error(e.toString());
            }
        }
    }

    public static void error(String message){
        if(RenderPermissions.renderErrors()) {
            try {
                outStream.write("[Error] ".getBytes());
                outStream.write(message.getBytes());
                outStream.write('\n');

                if (loggingStream != null) {
                    loggingStream.write("[Error] ".getBytes());
                    loggingStream.write(message.getBytes());
                    loggingStream.write('\n');
                }
            } catch (IOException e) {
                System.out.println("[Error] " + e.toString());
            }
        }
    }

    public static void success(String message){
        if(RenderPermissions.renderSuccesses()) {
            try {
                outStream.write("[Success] ".getBytes());
                outStream.write(message.getBytes());
                outStream.write('\n');

                if (loggingStream != null) {
                    loggingStream.write("[Success] ".getBytes());
                    loggingStream.write(message.getBytes());
                    loggingStream.write('\n');
                }
            } catch (IOException e) {
                error(e.toString());
            }
        }
    }

    public static void info(String message){
        if(RenderPermissions.renderInfos()) {
            try {
                outStream.write("[Info] ".getBytes());
                outStream.write(message.getBytes());
                outStream.write('\n');

                if (loggingStream != null) {
                    loggingStream.write("[Info] ".getBytes());
                    loggingStream.write(message.getBytes());
                    loggingStream.write('\n');
                }
            } catch (IOException e) {
                error(e.toString());
            }
        }
    }

    public static void warn(String message){
        if(RenderPermissions.renderWarns()) {
            try {
                outStream.write("[Warning] ".getBytes());
                outStream.write(message.getBytes());
                outStream.write('\n');

                if (loggingStream != null) {
                    loggingStream.write("[Warning] ".getBytes());
                    loggingStream.write(message.getBytes());
                    loggingStream.write('\n');
                }
            } catch (IOException e) {
                error(e.toString());
            }
        }
    }

    /**
     * For print, you can use another stream that only conventional System.out
     * @param out OutputStream. One to be used as new output stream. If this argument is null, System.out will be used.
     */
    public static void changeOutputStream(OutputStream out){
        if(out == null){
            outStream = System.out;
        }else{
            outStream = out;
        }
    }

    /**
     * For logging, you can use another stream. If this stream is non-null every log would be sent to this stream. All color information would be discarded.
     * @param stream OutputStream. One to be used as new logging stream. If this argument is null, no error will be thrown, no logs will be saved.
     */
    public static void setUpLogging(OutputStream stream){
        loggingStream = stream;
    }

}
