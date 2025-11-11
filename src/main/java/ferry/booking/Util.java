package ferry.booking;

import org.apache.commons.io.IOUtils;

import java.io.IOException;

public class Util {
    public static String readFileToString(String path) throws IOException {
        return IOUtils.toString(Util.class.getResourceAsStream(path), "UTF-8");
    }
}
