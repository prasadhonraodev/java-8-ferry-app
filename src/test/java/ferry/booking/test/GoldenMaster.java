package ferry.booking.test;

import ferry.booking.Program;
import org.apache.commons.io.input.ReaderInputStream;

import java.io.*;
import java.nio.file.Path;

public class GoldenMaster {

    public static void execute(Path input, Path output) {
        PrintStream ps = null;
        try {
            File file = new File(output.toString());
            file.createNewFile();
            ps = new PrintStream(file);
            Program.start(readerFor(input), ps);
        } catch (IOException e) {
        } finally {
            ps.close();
        }
    }

    private static InputStream readerFor(Path file) {
        FileReader reader = null;
        try {
            reader = new FileReader(file.toString());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        return new ReaderInputStream(reader);
    }

}
