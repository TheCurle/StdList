package xfacthd.importscrape.clazz;

import xfacthd.importscrape.util.Decompiler;
import xfacthd.importscrape.util.Util;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public abstract sealed class ClassFile
{
    private static final String DECOMP_JAR = "ClassFileDecomp.jar";

    protected final Path path;

    protected ClassFile(Path path)
    {
        this.path = path;
    }

    public Path getPath()
    {
        return path;
    }

    public abstract String getClassName();

    public abstract String getSource();

    public abstract void prepareStart(BiConsumer<String, byte[]> codeConsumer);

    public abstract void prepareComplete(JarFile jar);



    public static ClassFile of(Path path)
    {
        String sPath = path.toString();
        if (sPath.endsWith(".java"))
        {
            return new SourceFile(path);
        }
        if (sPath.endsWith(".class"))
        {
            return new BinFile(path);
        }
        throw new IllegalArgumentException("Invalid class file: " + path);
    }

    public static void prepare(List<ClassFile> files)
    {
        AtomicInteger count = new AtomicInteger(0);
        boolean written = Decompiler.writeInput(DECOMP_JAR, zipStream ->
        {
            files.forEach(cf -> cf.prepareStart((name, code) ->
            {
                count.incrementAndGet();

                try
                {
                    zipStream.putNextEntry(new JarEntry(name));
                    zipStream.write(code);
                }
                catch (IOException e)
                {
                    System.err.println("Failed to write code for decomp of " + name);
                    e.printStackTrace();
                }
            }));
        });

        if (!written || count.get() == 0 || !Decompiler.checkDecompilerPresent(true))
        {
            Decompiler.cleanup(DECOMP_JAR);
            return;
        }

        System.out.println("Compiled classes found, decompiling");
        JarFile jar = Decompiler.decompile(DECOMP_JAR);
        if (jar != null)
        {
            files.forEach(cf -> cf.prepareComplete(jar));
        }

        Decompiler.cleanup(DECOMP_JAR, jar);
    }



    static final class SourceFile extends ClassFile
    {
        private final String className;
        private final String source;

        SourceFile(Path path)
        {
            super(path);
            this.className = path.getFileName().toString().replace(".java", "");
            this.source = Util.readContents(path);
        }

        @Override
        public String getClassName()
        {
            return className;
        }

        @Override
        public String getSource()
        {
            return source;
        }

        @Override
        public void prepareStart(BiConsumer<String, byte[]> codeConsumer) { }

        @Override
        public void prepareComplete(JarFile jar) { }
    }

    static final class BinFile extends ClassFile
    {
        private final String className;
        private final String filePath;
        private byte[] bin;
        private String source = null;

        BinFile(Path path)
        {
            super(path);
            this.className = path.getFileName().toString().replace(".class", "");
            this.filePath = path.toString().replaceFirst("/", "").replace(".class", "");
            this.bin = Util.readBinContents(path);
        }

        @Override
        public String getClassName()
        {
            return className;
        }

        @Override
        public String getSource()
        {
            if (bin != null)
            {
                throw new IllegalStateException("BinFile not prepared!");
            }
            return source;
        }

        @Override
        public void prepareStart(BiConsumer<String, byte[]> codeConsumer)
        {
            if (source != null || bin == null) { return; }

            //Assume that compiled files only occur in archives where the
            //path we have will be the package and class name in the decomp jar
            codeConsumer.accept(filePath + ".class", bin);
        }

        @Override
        public void prepareComplete(JarFile jar)
        {
            if (source != null) { return; }

            source = Util.readDecompSource(jar, filePath + ".java");
            bin = null;
        }
    }
}
