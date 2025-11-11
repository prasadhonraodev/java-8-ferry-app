package ferry.booking.test;

import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

import static ferry.booking.test.GoldenMaster.execute;

public class GoldenMasterCreator {

    @Test
    public void creation() {
        String[] testCases = new String[]{"master", "availability"};
        for (String testCase : testCases) {
            GoldenMasterCase master = new GoldenMasterCase(testCase);
            master.deletePreviousRun();
            execute(master.input(), master.master());
            master.acceptResults();
        }
    }

    @Test
    public void availability_creation() {
        String[] ports = new String[]{"1", "2", "3"};
        GoldenMasterCase goldenMasterCase = new GoldenMasterCase("availability");
        File file = goldenMasterCase.input().toFile();
        try {
            try (PrintStream ps = new PrintStream(file)) {
                for (int i = 0; i < ports.length; i++) {
                    for (int j = 0; j < ports.length; j++) {
                        if (i != j) {
                            for (int minute = 0; minute <= 59; minute++) {
                                String searchCommand = String.format("search %s %s 00:%02d", ports[i], ports[j], minute);
                                ps.append(searchCommand).append(System.lineSeparator());
                            }
                            for (int minute = 0; minute <= 41; minute++) {
                                String searchCommand = String.format("search %s %s 01:%02d", ports[i], ports[j], minute);
                                ps.append(searchCommand).append(System.lineSeparator());
                            }
                        }
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


}
