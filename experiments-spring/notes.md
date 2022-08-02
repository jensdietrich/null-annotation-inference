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








