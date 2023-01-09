package xfacthd.importscrape.util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarFile;
import java.util.zip.ZipOutputStream;

public final class Decompiler
{
    private static final String DEFAULT_DECOMP_PATH = "./forgeflower-1.5.498.29.jar";
    private static final String DECOMP_IN_PATH = "./decomp_in";
    private static final String DECOMP_OUT_PATH = "./decomp_out";

    public static boolean writeInput(String fileName, ThrowingConsumer<ZipOutputStream, IOException> streamConsumer)
    {
        try
        {
            Files.createDirectories(Path.of(DECOMP_IN_PATH));

            File jarFile = new File(DECOMP_IN_PATH + "/" + fileName);

            if (!jarFile.exists() && !jarFile.createNewFile())
            {
                return false;
            }

            FileOutputStream fileStream = new FileOutputStream(jarFile);

            ZipOutputStream zipStream = new ZipOutputStream(fileStream);
            streamConsumer.accept(zipStream);
            zipStream.close();

            fileStream.close();

            return true;
        }
        catch (IOException e)
        {
            return false;
        }
    }

    public static JarFile decompile(String fileName)
    {
        try
        {
            Files.createDirectories(Path.of(DECOMP_OUT_PATH));

            Process decomp = new ProcessBuilder()
                    .command("java", "-jar", DEFAULT_DECOMP_PATH, "-nls=1", DECOMP_IN_PATH + "/" + fileName, DECOMP_OUT_PATH)
                    .start();
            InputStream stream = decomp.getInputStream();
            while (decomp.isAlive())
            {
                if (stream.available() > 0)
                {
                    String output = new String(stream.readAllBytes());
                    System.out.printf("[Decompiler] %s\n", output);
                }
            }
            if (decomp.waitFor() != 0)
            {
                System.err.printf("Decompiler exited with non-zero exit code '%d'\n", decomp.exitValue());
                return null;
            }
        }
        catch (IOException | InterruptedException e)
        {
            System.err.println("Encountered an error while decompiling JAR");
            e.printStackTrace();
            return null;
        }

        File resultFile = new File(DECOMP_OUT_PATH + "/" + fileName);
        if (!resultFile.exists())
        {
            System.err.printf("Decompilation result archive not found for file '%s'\n", fileName);
            return null;
        }

        try
        {
            return new JarFile(resultFile);
        }
        catch (IOException e)
        {
            System.err.printf("Failed to create JarFile for decompilation result in file '%s'\n", fileName);
            return null;
        }
    }

    public static void cleanup(String fileName, JarFile... files)
    {
        try
        {
            for (JarFile file : files)
            {
                if (file != null)
                {
                    file.close();
                }
            }

            Files.deleteIfExists(Path.of(DECOMP_IN_PATH + "/" + fileName));
            Files.deleteIfExists(Path.of(DECOMP_OUT_PATH + "/" + fileName));
        }
        catch (IOException e)
        {
            System.err.printf("Encountered an error while cleaning up decompilation artifacts of '%s'\n", fileName);
        }
    }

    public static boolean checkDecompilerPresent(boolean required)
    {
        if (!Files.exists(Path.of(DEFAULT_DECOMP_PATH)))
        {
            if (required)
            {
                System.err.println("Compiled class files found in libraries but Decompiler could not be located, results will be incomplete!");
            }
            return false;
        }
        return true;
    }



    private Decompiler() { }
}
