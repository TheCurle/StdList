package xfacthd.importscrape.clazz;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ClassReader
{
    private static final Pattern PACKAGE_PATTERN = Pattern.compile("package ([a-zA-Z0-9.]+\\*?);");
    private static final Pattern IMPORT_PATTERN = Pattern.compile("import ([a-zA-Z0-9.]+\\*?);");
    private static final String KEY_STATIC = "static ";

    public static String findPackage(String contents)
    {
        Matcher matcher = PACKAGE_PATTERN.matcher(contents);
        if (matcher.find())
        {
            return matcher.group(1);
        }
        return "";
    }

    public static List<String> collectImports(ClassEntry entry)
    {
        List<String> imports = new ArrayList<>();
        Matcher matcher = IMPORT_PATTERN.matcher(entry.contents());
        while (matcher.find())
        {
            String imp = matcher.group(1);
            if (imp.startsWith(KEY_STATIC))
            {
                imp = imp.substring(KEY_STATIC.length(), imp.lastIndexOf('.'));
            }
            imports.add(imp);
        }
        return imports;
    }

    public static void locateInnerClasses(String outerClass, ClassEntry entry)
    {

    }

    private ClassReader() { }
}
