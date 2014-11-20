import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Private property.
 * User: Darkyen
 * Date: 20/11/14
 * Time: 19:00
 */
public class MapLoader {

    private static final int MAP_NAME = '#';//This char is a special char and will be replaced when generating 8 programs.

    private static final File MAP_POINTER_FILE = new File("mappointer");
    private static final File MAIN_PROGRAM_FILE = new File("NXTProgram.nxj");

    public static void main(String[] args) throws IOException {
        FileOutputStream writer = new FileOutputStream(MAP_POINTER_FILE);
        writer.write(MAP_NAME);
        writer.flush();
        writer.close();

        MAIN_PROGRAM_FILE.exec(); //This actually compiles and works! LeJOS has added this method.
    }
}
