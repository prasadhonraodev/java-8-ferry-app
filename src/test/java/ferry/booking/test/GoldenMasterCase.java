package ferry.booking.test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

class GoldenMasterCase {

    private String caseDescription;

    public GoldenMasterCase(String caseDescription) {
        this.caseDescription = caseDescription;
    }

    public Path input() {
        return pathForFileType("input");
    }

    public Path execution() {
        return pathForFileType("execution");
    }

    public Path master() {
        return pathForFileType("master");
    }

    public void acceptResults() {
        try {
            Files.move(execution(), master());
        } catch (IOException e) {
        }
    }

    public void deletePreviousRun() {
        try {
            Files.delete(execution());
            Files.delete(master());
        } catch (IOException e) {
        }
    }

    private Path pathForFileType(String fileType) {
        return Paths.get("src/test/resources/" + this.caseDescription + "." + fileType + ".txt").toAbsolutePath();
    }
}
