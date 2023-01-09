package xfacthd.importscrape.library.repo;

import java.nio.file.Files;
import java.nio.file.Path;

final class FlatDirRepository implements Repository
{
    private final Path path;

    FlatDirRepository(Path path)
    {
        this.path = path;
    }

    @Override
    public Path tryDownload(String mvnCoord)
    {
        String[] parts = mvnCoord.split(":");
        if (parts.length < 3 || parts.length > 4)
        {
            System.err.println("Invalid maven coordinate: " + mvnCoord);
            return null;
        }

        String fileName = parts[1] + "-" + parts[2];
        if (parts.length == 4)
        {
            //Classifier
            fileName += "-" + parts[3];
        }
        fileName += ".jar";

        Path depJar = path.resolve(fileName);
        if (Files.exists(depJar))
        {
            return depJar;
        }
        return null;
    }
}
