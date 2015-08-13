# ShyFramework
This repository contains a modified version of [__Shy__](https://github.com/JasonCHU/SYBwithOA/tree/master/Shy) to support compilation from command line.

## How to compile from the command line

Now you have Library.jar as a library of __Shy__. The latest version can be found under the root directory.

To compile a java file which includes the "@Algebra" annotation, simply use this command:

```shell
javac -cp Library.jar -proc:only *.java
```

Or you can remove "-proc:only" if you want to have subsequent compilation for generated code.

__Note__ that you shouldn't put that java file under a default(unnamed) package. The file is expected to have a certain package name so that the generated classes can easily import the object algebra interface.

## Change log

- /Shy/src/com/zewei/annotation/processor/AlgebraProcessor.java: When specifying file paths, "/" works in Eclipse but fails in command line compilation with a FilerException. Now "." is used instead.

- /Shy/src/com/zewei/annotation/processor/DeclaredTypeVisitor.java: The method DeclaredTypeVisitor.visitDeclared is slightly modified to ensure correctness in command line compilation.
