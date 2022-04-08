import entity.archive.Archive;

import java.io.IOException;

public class archive {
    private final Archive archive;

    private archive(Archive archive) {
        this.archive = archive;
    }

    public archive(engine eng) {
        archive = new Archive(eng.getEng());
    }

    public void dump(String fileName) throws IOException {
        archive.dump(fileName);
    }

    public void resume(engine eng) {
        archive.resume(eng.getEng());
    }
}
