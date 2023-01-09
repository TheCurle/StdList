package xfacthd.importscrape;

import xfacthd.importscrape.clazz.*;
import xfacthd.importscrape.library.GradleUtil;
import xfacthd.importscrape.library.dep.Dependency;
import xfacthd.importscrape.library.repo.Repository;
import xfacthd.importscrape.util.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class Main
{
    public static void main(String[] args)
    {
        if (args.length < 1)
        {
            System.err.println("Usage: importscrape <repo path> [lib folders]");
            return;
        }

        Path repo = Path.of(args[0]);
        String[] localLibs = new String[args.length - 1];
        if (args.length > 1)
        {
            System.arraycopy(args, 1, localLibs, 0, args.length - 1);
        }

        List<ClassEntry> srcFiles = new ArrayList<>();
        collectSourceFiles(repo, srcFiles, s -> s.contains("/src/"));
        collectLocalLibrarySourceFiles(repo, localLibs, srcFiles);
        collectGradleLibrarySourceFiles(repo, srcFiles);

        Map<String, List<String>> imports = collectImportsInSource(srcFiles);
        filterOutKnownImports(imports, srcFiles);

        printUnknownImports(repo, imports);
    }

    private static void collectSourceFiles(Path srcPath, List<ClassEntry> srcFiles, Predicate<String> filter)
    {
        List<ClassFile> srcPaths;
        try (Stream<Path> stream = Files.walk(srcPath))
        {
            srcPaths = stream.filter(p ->
            {
                String path = p.toString().replace("\\", "/");
                return filter.test(path) && (path.endsWith(".java") || path.endsWith(".class"));
            }).map(ClassFile::of).toList();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }

        ClassFile.prepare(srcPaths);

        for (ClassFile clazz : srcPaths)
        {
            String contents = clazz.getSource();
            String pkg = ClassReader.findPackage(contents);
            String fqn = pkg + "." + clazz.getClassName();

            ClassEntry entry = new ClassEntry(clazz, fqn, contents, new ArrayList<>());

            //Handle normal import, static import, static wildcard import and top-level inner class import
            entry.importables().add(fqn);
            //Handle normal wildcard import
            entry.importables().add(pkg + ".*");

            ClassReader.locateInnerClasses(fqn, entry);

            srcFiles.add(entry);
        }
    }

    private static void collectLocalLibrarySourceFiles(Path repo, String[] localLibs, List<ClassEntry> srcFiles)
    {
        for (String localLib : localLibs)
        {
            Path libFolder = repo.resolve(localLib);
            try (Stream<Path> paths = Files.walk(libFolder))
            {
                List<Path> depJars = paths.filter(p -> p.toString().endsWith(".jar")).toList();
                collectLibrarySourceFiles(depJars, srcFiles);
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    private static void collectGradleLibrarySourceFiles(Path repo, List<ClassEntry> srcFiles)
    {
        Path gradle = repo.resolve("build.gradle");
        if (!Files.exists(gradle)) { return; }

        String contents = Util.readContents(gradle);
        List<Repository> repos = GradleUtil.collectRepositories(contents);
        List<Dependency> deps = GradleUtil.collectDependencies(contents);
        List<Path> depJars = GradleUtil.downloadDependencies(repos, deps);

        collectLibrarySourceFiles(depJars, srcFiles);
    }

    private static void collectLibrarySourceFiles(List<Path> depJars, List<ClassEntry> srcFiles)
    {
        for (Path jar : depJars)
        {
            try (FileSystem fs = FileSystems.newFileSystem(jar))
            {
                for (Path path : fs.getRootDirectories())
                {
                    collectSourceFiles(path, srcFiles, s -> true);
                }
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    private static Map<String, List<String>> collectImportsInSource(List<ClassEntry> srcFiles)
    {
        Map<String, List<String>> imports = new HashMap<>();
        for (ClassEntry entry : srcFiles)
        {
            List<String> entryImports = ClassReader.collectImports(entry);
            imports.put(entry.fqn(), entryImports);
        }
        return imports;
    }

    private static void filterOutKnownImports(Map<String, List<String>> usedImports, List<ClassEntry> knownClasses)
    {
        Iterator<Map.Entry<String, List<String>>> it = usedImports.entrySet().iterator();
        while (it.hasNext())
        {
            List<String> entry = it.next().getValue();
            knownClasses.stream()
                    .map(ClassEntry::importables)
                    .flatMap(List::stream)
                    .forEach(entry::remove);

            if (entry.isEmpty())
            {
                it.remove();
            }
        }
    }

    private static void printUnknownImports(Path repo, Map<String, List<String>> imports)
    {
        StringBuilder output = new StringBuilder();

        output.append("# Unknown imports\n\n")
                .append("Repository path: ")
                .append(repo.toString())
                .append("\n\n");

        Set<String> overall = new HashSet<>();

        Table table = new Table(2);
        table.setHeader("File", "Import");
        for (Map.Entry<String, List<String>> entry : imports.entrySet())
        {
            String enclosing = entry.getKey();
            for (String imp : entry.getValue())
            {
                table.addLine(enclosing, imp);
                overall.add(imp);
            }
        }

        output.append("## Imports by file\n")
                .append(table.print())
                .append("\n\n");

        table = new Table(1);
        table.setHeader("Import");
        for (String imp : overall)
        {
            table.addLine(imp);
        }

        output.append("## Unique imports\n")
                .append(table.print());

        try
        {
            Files.writeString(Path.of("result.md"), output.toString());
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
}