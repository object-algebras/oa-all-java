#oa-all-java
This is a collection of current projects/libraries related to object algebras using Java. To use these libraries, just include corresponding jar(s) inside `lib/` folder.

Short introductions of these projects are listed below. For detailed intro/instructions, please go to `README.md` inside each project:
* `pretty-printer-generator/README.md`
* `naked-object-algebras/README.md`
* `Shy/README.md`

## naked-object-algebras
Object Algebras decorated with concrete syntax annotations. ANTLR4 parser is generated automatically. 

Provided annotation(s): @Syntax @Level @Skip @Token

## pretty-printer-generator
Generates pretty printers from annotated object algebra interfaces, using annotation processing.

Provided annotation(s): @PP

## Shy
Shy is a framework automatically generating Object Algebras queries and transformations for user-defined generic Object Algebra interfaces annotated with a simple "@Algebra". With these generated classes/interfaces, users can then inherit from them to implement structure-shy traversals by overriding only a few interesting cases. Hence boilerplate traversal code is avoided. The library has been tested in JRE 8 in Eclipse.

Provided annotation(s):@Algebra
