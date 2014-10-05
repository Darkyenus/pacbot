package lego.training.userinterface;

import lego.util.Constants;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by jIRKA on 4.10.2014.
 */
public class Print {

    private static OutputStream outStream = System.out;
    private static boolean colorsEnabled = true;

    private static OutputStream loggingStream = null;

    /**
     * This feature will enable some color logging, like red for errors and so on. However,
     * keep this feature off when you are not using System.out output stream but some other.
     * Please note, that this feature might not work on some IDE Console outputs (especially talking about IntelliJ IDEA and few others.)
     * @param enabled boolean. True if you want use colors, false otherwise. Default value is false.
     */
    public static void setColorsEnabled(boolean enabled){
        colorsEnabled = enabled;
    }

    /**
     * Returns whenever are colors in console enabled or not.
     * @return true when colors are enable, otherwise false
     */
    public static boolean isColorsEnabled(){
        return colorsEnabled;
    }

    /**
     * Prints message with trailing linebreak
     * @param message
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
     * @param message
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

    public static void color(String message, ConsoleColors color){
        if(RenderPermissions.renderGeneralText()){
            try {
                if(colorsEnabled && color != ConsoleColors.DEFAULT)
                    outStream.write(color.getCode());
                outStream.write(message.getBytes());
                if(colorsEnabled && color != ConsoleColors.DEFAULT)
                    outStream.write(ConsoleColors.DEFAULT.getCode());

                if(loggingStream != null){
                    loggingStream.write(message.getBytes());
                }
            }catch(IOException e){
                System.out.println("[Error] "+e.toString());
            }
        }
    }

    public static void error(String message){
        if(RenderPermissions.renderErrors()) {
            try {
                if (colorsEnabled)
                    outStream.write(Constants.COLOR_TAG_ERROR.getCode());
                outStream.write("[Error] ".getBytes());
                if (colorsEnabled)
                    outStream.write(ConsoleColors.DEFAULT.getCode());
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
                if (colorsEnabled)
                    outStream.write(Constants.COLOR_TAG_SUCCESS.getCode());
                outStream.write("[Success] ".getBytes());
                if (colorsEnabled)
                    outStream.write(ConsoleColors.DEFAULT.getCode());
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
                if (colorsEnabled)
                    outStream.write(Constants.COLOR_TAG_INFO.getCode());
                outStream.write("[Info] ".getBytes());
                if (colorsEnabled)
                    outStream.write(ConsoleColors.DEFAULT.getCode());
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
                if (colorsEnabled)
                    outStream.write(Constants.COLOR_TAG_WARN.getCode());
                outStream.write("[Warning] ".getBytes());
                if (colorsEnabled)
                    outStream.write(ConsoleColors.DEFAULT.getCode());
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
