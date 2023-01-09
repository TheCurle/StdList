package xfacthd.importscrape.library.repo;

import java.nio.file.Path;

public interface Repository
{
    Path tryDownload(String mvnCoord);



    static Repository maven(String url)
    {
        return new MavenRepository(url);
    }

    static Repository flatDir(Path path)
    {
        return new FlatDirRepository(path);
    }
}
