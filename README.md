# RepoImportScraper

Scrapes all source files and libraries (currently only in manually specified libs directories) in the given project directory and attempts to find all imports which are not known from either the project's sources or the libraries (mainly Java stdlib imports).

## Invocation

With the compiled jar file in the current folder:

```
java -cp RepoImportScraper-x.y.z.jar xfacthd/importscraper/Main <args>
```

## Args

The arguments required are:

```
<project folder> [libraries]
```

The project folder must contain a `src` (named exactly) folder, which must contain the Java source files at arbitrary depth. The folders will be walked recursively and all Java files scanned for imports.

Libraries added to the list are removed from the import list, as they are "provided", so to speak.