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

| Method In Supertype     | Method is Subtype | @Nullable Issue | Module |
| ----------- | ----------- | ----------- | ----------- |
| [AnnotationVisitor::visitAnnotation](https://github.com/spring-projects/spring-framework/blob/main/spring-core/src/main/java/org/springframework/asm/AnnotationVisitor.java)      | [MergedAnnotationReadingVisitor::visitAnnotation](https://github.com/spring-projects/spring-framework/blob/main/spring-core/src/main/java/org/springframework/core/type/classreading/MergedAnnotationReadingVisitor.java)       | return| `spring-core` |








