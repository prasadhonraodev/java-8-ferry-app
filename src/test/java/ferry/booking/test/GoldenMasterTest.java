package ferry.booking.test;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

import static ferry.booking.test.GoldenMaster.execute;
import static org.junit.Assert.assertEquals;

public class GoldenMasterTest {

    private static String readFile(Path path) throws IOException {
        byte[] encoded = Files.readAllBytes(path);
        return new String(encoded, Charset.defaultCharset());
    }

    @Test
    public void compare_case_master_to_golden_master() throws IOException {
        executeCase(new GoldenMasterCase("master"));
    }

    @Test
    public void compare_case_availability_to_golden_master() throws IOException {
        executeCase(new GoldenMasterCase("availability"));
    }

    private void executeCase(GoldenMasterCase goldenMasterCase) throws IOException {
        execute(goldenMasterCase.input(), goldenMasterCase.execution());
        String master = readFile(goldenMasterCase.master());
        String tests = readFile(goldenMasterCase.execution());
        assertEquals(tests, master);
    }

}
