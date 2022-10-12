# Notes

##  Propagating found Annotations

Example (in *spring-core*): 

In [AnnotationVisitor](https://github.com/spring-projects/spring-framework/blob/main/spring-core/src/main/java/org/springframework/asm/AnnotationVisitor.java)

```java
public abstract class AnnotationVisitor {
    ...
    public AnnotationVisitor visitAnnotation(final String name, final String descriptor) {
        if (av != null) {
            return av.visitAnnotation(name, descriptor);
        }
        return null;
    }
    ...
}
```

Then in its subclass [MergedAnnotationReadingVisitor](https://github.com/spring-projects/spring-framework/blob/main/spring-core/src/main/java/org/springframework/core/type/classreading/MergedAnnotationReadingVisitor.java):

```java
class MergedAnnotationReadingVisitor<A extends Annotation> extends AnnotationVisitor {
    ...
    @Override
    @Nullable
    public AnnotationVisitor visitAnnotation(String name, String descriptor) {
        return visitAnnotation(descriptor, annotation -> this.attributes.put(name, annotation));
    }
    ...
}

```

Note that we are overriding with a more general return type here. This could be fixed by adding the `@Nullable` annotation to the overidden method (which would make sense also as it actually returns `null` if the delegate is not set).

Similar cases:

| supertype | subtype | method | issue | module |
|-----------|---------|--------| ------|--------|
| [AnnotationVisitor](https://github.com/spring-projects/spring-framework/blob/main/spring-core/src/main/java/org/springframework/asm/AnnotationVisitor.java) | [MergedAnnotationReadingVisitor](https://github.com/spring-projects/spring-framework/blob/main/spring-core/src/main/java/org/springframework/core/type/classreading/MergedAnnotationReadingVisitor.java) | `visitAnnotation` | return       | `spring-core` |
| [AnnotationVisitor](https://github.com/spring-projects/spring-framework/blob/main/spring-core/src/main/java/org/springframework/asm/AnnotationVisitor.java) | [MergedAnnotationReadingVisitor$ArrayVisitor](https://github.com/spring-projects/spring-framework/blob/main/spring-core/src/main/java/org/springframework/core/type/classreading/MergedAnnotationReadingVisitor.java) | `visitAnnotation` | return       | `spring-core` |
| [ClassVisitor](https://github.com/spring-projects/spring-framework/blob/main/spring-core/src/main/java/org/springframework/asm/ClassVisitor.java) | [SimpleAnnotationMetadataReadingVisitor.java](https://github.com/spring-projects/spring-framework/blob/main/spring-core/src/main/java/org/springframework/core/type/classreading/SimpleAnnotationMetadataReadingVisitor.java) | `visitMethod` | return       | `spring-core` |
| [ClassVisitor](https://github.com/spring-projects/spring-framework/blob/main/spring-core/src/main/java/org/springframework/asm/ClassVisitor.java) | [LocalVariableTableParameterNameDiscoverer$ParameterNameDiscoveringVisitor](https://github.com/spring-projects/spring-framework/blob/main/spring-core/src/main/java/org/springframework/core/LocalVariableTableParameterNameDiscoverer.java)| `visitMethod` | return       | `spring-core` |
| [ClassVisitor](https://github.com/spring-projects/spring-framework/blob/main/spring-core/src/main/java/org/springframework/asm/ClassVisitor.java) | ClassMetadataReadingVisitor (src?) | `visitAnnotation` | return       | `spring-core` |
| [ClassVisitor](https://github.com/spring-projects/spring-framework/blob/main/spring-core/src/main/java/org/springframework/asm/ClassVisitor.java) | [SimpleAnnotationMetadataReadingVisitor.java](https://github.com/spring-projects/spring-framework/blob/main/spring-core/src/main/java/org/springframework/core/type/classreading/SimpleAnnotationMetadataReadingVisitor.java) | `visitAnnotation` | return       | `spring-core` |
| [ClassVisitor](https://github.com/spring-projects/spring-framework/blob/main/spring-core/src/main/java/org/springframework/asm/ClassVisitor.java) | AnnotationMetadataReadingVisitor (src?) | `visitAnnotation` | return       | `spring-core` |
| [MethodVisitor](https://github.com/spring-projects/spring-framework/blob/main/spring-core/src/main/java/org/springframework/asm/MethodVisitor.java) | [SimpleMethodMetadataReadingVisitor](https://github.com/spring-projects/spring-framework/blob/main/spring-core/src/main/java/org/springframework/core/type/classreading/SimpleMethodMetadataReadingVisitor.java) | `visitAnnotation` | return       | `spring-core` |
| [MethodVisitor](https://github.com/spring-projects/spring-framework/blob/main/spring-core/src/main/java/org/springframework/asm/MethodVisitor.java) | MethodMetadataReadingVisitor (src) | `visitAnnotation` | return       | `spring-core` |


Note that the respective [pull request](https://github.com/spring-projects/spring-framework/pull/28852) was declined as those classes are shaded. In response to this, a [JSON file](shaded.json) containing shaded packages has been created based on a grep seach for `relocate` in gradle file.


## Inferred Annotations must themselves be sanitized

The `@Nullable` return type of `LocalVariableTableParameterNameDiscoverer$ParameterNameDiscoveringVisitor` is propagated to the overridden method `ClassVisitor::visitMethod`,
however, this class is in a shaded package. 

```
className = "org.springframework.asm.ClassVisitor"
methodName = "visitMethod"
descriptor = "(ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;[Ljava/lang/String;)Lorg/springframework/asm/MethodVisitor;"
kind = "RETURN_VALUE"
provenanceType = "INFERRED"
argsIndex = -1
context = "springframework-core"
stacktrace = null
parent = 
    className = "org.springframework.core.LocalVariableTableParameterNameDiscoverer$ParameterNameDiscoveringVisitor"
    methodName = "visitMethod"
    descriptor = "(ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;[Ljava/lang/String;)Lorg/springframework/asm/MethodVisitor;"
    kind = "RETURN_VALUE"
    provenanceType = "OBSERVED"
    argsIndex = -1
    context = "springframework-core"
    stacktrace = ..
    parent = null
```  

## Measuring Coverage

1. open springframework in IntelliJ
2. IntelliJ IDEA 2022.2 (Ultimate Edition), Build #IU-222.3345.118, built on July 26, 2022
3. navigate to `<module>/src/test`
4. run tests in <module> with coverage
5. when testing finishs, select option `replace active suits`
6. inspect coverage value next to project file tree node in `<module>/src/main/java`






