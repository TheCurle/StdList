package xfacthd.importscrape.library.repo;

import java.nio.file.Path;

final class MavenRepository implements Repository
{
    private final String url;

    public MavenRepository(String url)
    {
        this.url = url;
    }

    @Override
    public Path tryDownload(String mvnCoord)
    {
        return null;
    }
}
