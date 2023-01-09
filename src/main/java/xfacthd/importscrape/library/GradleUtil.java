package xfacthd.importscrape.library;

import xfacthd.importscrape.library.dep.Dependency;
import xfacthd.importscrape.library.repo.Repository;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class GradleUtil
{
    public static List<Repository> collectRepositories(String gradle)
    {
        List<Repository> repos = new ArrayList<>();

        return repos;
    }

    public static List<Dependency> collectDependencies(String contents)
    {
        List<Dependency> deps = new ArrayList<>();

        return deps;
    }

    public static List<Path> downloadDependencies(List<Repository> repos, List<Dependency> deps)
    {
        List<Path> depJars = new ArrayList<>();
        for (Dependency dep : deps)
        {
            String coord = dep.getMavenCoordinate();

            Path depJar = null;
            for (Repository repo : repos)
            {
                depJar = repo.tryDownload(coord);
                if (depJar != null)
                {
                    depJars.add(depJar);
                    break;
                }
            }

            if (depJar == null)
            {
                System.err.println("Failed to download dependency with coordinate: " + coord);
            }
        }
        return depJars;
    }



    private GradleUtil() { }
}
