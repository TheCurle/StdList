package xfacthd.importscrape.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class Util
{
    public static String readContents(Path path)
    {
        try
        {
            return Files.readString(path);
        }
        catch (IOException e)
        {
            System.out.println("Error reading file");
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] readBinContents(Path path)
    {
        try
        {
            return Files.readAllBytes(path);
        }
        catch (IOException e)
        {
            System.out.println("Error reading file");
            e.printStackTrace();
            return null;
        }
    }

    public static String readDecompSource(JarFile jar, String entryName)
    {
        JarEntry entry = jar.getJarEntry(entryName);
        if (entry == null)
        {
            System.err.println("Failed to get decompiled source file: " + entryName);
            return "null";
        }

        try
        {
            InputStream decompStream = jar.getInputStream(entry);
            String decompSource = new String(decompStream.readAllBytes());
            decompStream.close();
            return decompSource;
        }
        catch (IOException e)
        {
            System.err.println("Failed to read decompiled source file: " + entryName);
            e.printStackTrace();
            return "null";
        }
    }



    private Util() { }
}
