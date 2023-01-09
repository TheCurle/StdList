package xfacthd.importscrape.clazz;

import java.util.List;

public record ClassEntry(ClassFile srcPath, String fqn, String contents, List<String> importables)
{

}
