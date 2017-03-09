package lemon.ioc.dinjection;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static lemon.ioc.dinjection.annotations.Annotations.generateAnnotation;
import static lemon.ioc.dinjection.annotations.Annotations.isAllDefaultMethods;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

import lemon.ioc.dinjection.annotations.Annotations;

/**
 * Binding key consisting of an injection type and an optional annotation.
 * Matches the type and annotation at a point of injection.
 *
 * <p>For example, {@code Key.get(Service.class, Transactional.class)} will
 * match:
 *
 * <pre>
 *   {@literal @}Inject
 *   public void setService({@literal @}Transactional Service service) {
 *     ...
 *   }
 * </pre>
 *
 * <p>{@code Key} supports generic types via subclassing just like {@link
 * TypeLiteral}.
 *
 * <p>Keys do not differentiate between primitive types (int, char, etc.) and
 * their corresponding wrapper types (Integer, Character, etc.). Primitive
 * types will be replaced with their wrapper types when keys are created.
 *
 * @author crazybob@google.com (Bob Lee)
 */
public class Key<T> {

    private final AnnotationStrategy annotationStrategy;

    private final TypeLiteral<T> typeLiteral;
    private final int hashCode;
    private final Supplier<String> toStringSupplier;

    /**
     * Constructs a new key. Derives the type from this class's type parameter.
     *
     * <p>Clients create an empty anonymous subclass. Doing so embeds the type
     * parameter in the anonymous class's type hierarchy so we can reconstitute it
     * at runtime despite erasure.
     *
     * <p>Example usage for a binding of type {@code Foo} annotated with
     * {@code @Bar}:
     *
     * <p>{@code new Key<Foo>(Bar.class) {}}.
     */
    @SuppressWarnings("unchecked")
    protected Key(Class<? extends Annotation> annotationType) {
        this.annotationStrategy = strategyFor(annotationType);
        this.typeLiteral = MoreTypes.canonicalizeForKey((TypeLiteral<T>) TypeLiteral.fromSuperclassTypeParameter(getClass()));
        this.hashCode = computeHashCode();
        this.toStringSupplier = createToStringSupplier();
    }

    /**
     * Constructs a new key. Derives the type from this class's type parameter.
     *
     * <p>Clients create an empty anonymous subclass. Doing so embeds the type
     * parameter in the anonymous class's type hierarchy so we can reconstitute it
     * at runtime despite erasure.
     *
     * <p>Example usage for a binding of type {@code Foo} annotated with
     * {@code @Bar}:
     *
     * <p>{@code new Key<Foo>(new Bar()) {}}.
     */
    @SuppressWarnings("unchecked")
    protected Key(Annotation annotation) {
        // no usages, not test-covered
        this.annotationStrategy = strategyFor(annotation);
        this.typeLiteral = MoreTypes.canonicalizeForKey((TypeLiteral<T>) TypeLiteral.fromSuperclassTypeParameter(getClass()));
        this.hashCode = computeHashCode();
        this.toStringSupplier = createToStringSupplier();
    }

    /**
     * Constructs a new key. Derives the type from this class's type parameter.
     *
     * <p>Clients create an empty anonymous subclass. Doing so embeds the type
     * parameter in the anonymous class's type hierarchy so we can reconstitute it
     * at runtime despite erasure.
     *
     * <p>Example usage for a binding of type {@code Foo}:
     *
     * <p>{@code new Key<Foo>() {}}.
     */
    @SuppressWarnings("unchecked")
    protected Key() {
        this.annotationStrategy = NullAnnotationStrategy.INSTANCE;
        this.typeLiteral = MoreTypes.canonicalizeForKey((TypeLiteral<T>) TypeLiteral.fromSuperclassTypeParameter(getClass()));
        this.hashCode = computeHashCode();
        this.toStringSupplier = createToStringSupplier();
    }

    /**
     * Unsafe. Constructs a key from a manually specified type.
     */
    @SuppressWarnings("unchecked")
    private Key(Type type, AnnotationStrategy annotationStrategy) {
        this.annotationStrategy = annotationStrategy;
        this.typeLiteral = MoreTypes.canonicalizeForKey((TypeLiteral<T>) TypeLiteral.get(type));
        this.hashCode = computeHashCode();
        this.toStringSupplier = createToStringSupplier();
    }

    /** Constructs a key from a manually specified type. */
    private Key(TypeLiteral<T> typeLiteral, AnnotationStrategy annotationStrategy) {
        this.annotationStrategy = annotationStrategy;
        this.typeLiteral = MoreTypes.canonicalizeForKey(typeLiteral);
        this.hashCode = computeHashCode();
        this.toStringSupplier = createToStringSupplier();
    }

    /**
     * Computes the hash code for this key.
     */
    private int computeHashCode() {
        return typeLiteral.hashCode() * 31 + annotationStrategy.hashCode();
    }

    /**
     * @return a {@link Supplier} which memoizes the value for lazy initialization.
     */
    private Supplier<String> createToStringSupplier() {
        // The performance hit on access is acceptable since the intended use is for non-performance-
        // critical applications such as debugging and logging.
        return Suppliers.memoize(new Supplier<String>() {
            @Override
            public String get() {
                return "Key[type=" + typeLiteral + ", annotation=" + annotationStrategy + "]";
            }
        });
    }

    /**
     * Gets the key type.
     */
    public final TypeLiteral<T> getTypeLiteral() {
        return typeLiteral;
    }

    /**
     * Gets the annotation type.
     */
    public final Class<? extends Annotation> getAnnotationType() {
        return annotationStrategy.getAnnotationType();
    }

    /**
     * Gets the annotation.
     */
    public final Annotation getAnnotation() {
        return annotationStrategy.getAnnotation();
    }

    boolean hasAnnotationType() {
        return annotationStrategy.getAnnotationType() != null;
    }

    String getAnnotationName() {
        Annotation annotation = annotationStrategy.getAnnotation();
        if (annotation != null) {
            return annotation.toString();
        }

        // not test-covered
        return annotationStrategy.getAnnotationType().toString();
    }

    Class<? super T> getRawType() {
        return typeLiteral.getRawType();
    }

    /**
     * Gets the key of this key's provider.
     */
    Key<Provider<T>> providerKey() {
        return ofType(typeLiteral.providerType());
    }

    @Override
    public final boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Key<?>)) {
            return false;
        }
        Key<?> other = (Key<?>) o;
        return annotationStrategy.equals(other.annotationStrategy) && typeLiteral.equals(other.typeLiteral);
    }

    @Override
    public final int hashCode() {
        return this.hashCode;
    }

    @Override
    public final String toString() {
        return toStringSupplier.get();
    }

    /**
     * Gets a key for an injection type and an annotation strategy.
     */
    static <T> Key<T> get(Class<T> type, AnnotationStrategy annotationStrategy) {
        return new Key<T>(type, annotationStrategy);
    }

    /**
     * Gets a key for an injection type.
     */
    public static <T> Key<T> get(Class<T> type) {
        return new Key<T>(type, NullAnnotationStrategy.INSTANCE);
    }

    /**
     * Gets a key for an injection type and an annotation type.
     */
    public static <T> Key<T> get(Class<T> type, Class<? extends Annotation> annotationType) {
        return new Key<T>(type, strategyFor(annotationType));
    }

    /**
     * Gets a key for an injection type and an annotation.
     */
    public static <T> Key<T> get(Class<T> type, Annotation annotation) {
        return new Key<T>(type, strategyFor(annotation));
    }

    /**
     * Gets a key for an injection type.
     */
    public static Key<?> get(Type type) {
        return new Key<Object>(type, NullAnnotationStrategy.INSTANCE);
    }

    /**
     * Gets a key for an injection type and an annotation type.
     */
    public static Key<?> get(Type type, Class<? extends Annotation> annotationType) {
        return new Key<Object>(type, strategyFor(annotationType));
    }

    /**
     * Gets a key for an injection type and an annotation.
     */
    public static Key<?> get(Type type, Annotation annotation) {
        return new Key<Object>(type, strategyFor(annotation));
    }

    /**
     * Gets a key for an injection type.
     */
    public static <T> Key<T> get(TypeLiteral<T> typeLiteral) {
        return new Key<T>(typeLiteral, NullAnnotationStrategy.INSTANCE);
    }

    /**
     * Gets a key for an injection type and an annotation type.
     */
    public static <T> Key<T> get(TypeLiteral<T> typeLiteral, Class<? extends Annotation> annotationType) {
        return new Key<T>(typeLiteral, strategyFor(annotationType));
    }

    /**
     * Gets a key for an injection type and an annotation.
     */
    public static <T> Key<T> get(TypeLiteral<T> typeLiteral, Annotation annotation) {
        return new Key<T>(typeLiteral, strategyFor(annotation));
    }

    /**
     * Returns a new key of the specified type with the same annotation as this
     * key.
     *
     * @since 3.0
     */
    public <T> Key<T> ofType(Class<T> type) {
        return new Key<T>(type, annotationStrategy);
    }

    /**
     * Returns a new key of the specified type with the same annotation as this
     * key.
     *
     * @since 3.0
     */
    public Key<?> ofType(Type type) {
        return new Key<Object>(type, annotationStrategy);
    }

    /**
     * Returns a new key of the specified type with the same annotation as this
     * key.
     *
     * @since 3.0
     */
    public <T> Key<T> ofType(TypeLiteral<T> type) {
        return new Key<T>(type, annotationStrategy);
    }

    /**
     * Returns true if this key has annotation attributes.
     *
     * @since 3.0
     */
    public boolean hasAttributes() {
        return annotationStrategy.hasAttributes();
    }

    /**
     * Returns this key without annotation attributes, i.e. with only the
     * annotation type.
     *
     * @since 3.0
     */
    public Key<T> withoutAttributes() {
        return new Key<T>(typeLiteral, annotationStrategy.withoutAttributes());
    }

    interface AnnotationStrategy {
        Annotation getAnnotation();

        Class<? extends Annotation> getAnnotationType();

        boolean hasAttributes();

        AnnotationStrategy withoutAttributes();
    }

    /**
     * Gets the strategy for an annotation.
     */
    static AnnotationStrategy strategyFor(Annotation annotation) {
        checkNotNull(annotation, "annotation");
        Class<? extends Annotation> annotationType = annotation.annotationType();
        ensureRetainedAtRuntime(annotationType);
        ensureIsBindingAnnotation(annotationType);

        if (Annotations.isMarker(annotationType)) {
            return new AnnotationTypeStrategy(annotationType, annotation);
        }

        return new AnnotationInstanceStrategy(Annotations.canonicalizeIfNamed(annotation));
    }

    /**
     * Gets the strategy for an annotation type.
     */
    static AnnotationStrategy strategyFor(Class<? extends Annotation> annotationType) {
        annotationType = Annotations.canonicalizeIfNamed(annotationType);
        if (isAllDefaultMethods(annotationType)) {
            return strategyFor(generateAnnotation(annotationType));
        }

        checkNotNull(annotationType, "annotation type");
        ensureRetainedAtRuntime(annotationType);
        ensureIsBindingAnnotation(annotationType);
        return new AnnotationTypeStrategy(annotationType, null);

    }

    private static void ensureRetainedAtRuntime(Class<? extends Annotation> annotationType) {
        checkArgument(Annotations.isRetainedAtRuntime(annotationType), "%s is not retained at runtime. Please annotate it with @Retention(RUNTIME).", annotationType.getName());
    }

    private static void ensureIsBindingAnnotation(Class<? extends Annotation> annotationType) {
        checkArgument(Annotations.isBindingAnnotation(annotationType), "%s is not a binding annotation. Please annotate it with @BindingAnnotation.", annotationType.getName());
    }

    static enum NullAnnotationStrategy implements AnnotationStrategy {
        INSTANCE;

        public boolean hasAttributes() {
            return false;
        }

        public AnnotationStrategy withoutAttributes() {
            throw new UnsupportedOperationException("Key already has no attributes.");
        }

        public Annotation getAnnotation() {
            return null;
        }

        public Class<? extends Annotation> getAnnotationType() {
            return null;
        }

        @Override
        public String toString() {
            return "[none]";
        }
    }

    // this class not test-covered
    static class AnnotationInstanceStrategy implements AnnotationStrategy {

        final Annotation annotation;

        AnnotationInstanceStrategy(Annotation annotation) {
            this.annotation = checkNotNull(annotation, "annotation");
        }

        public boolean hasAttributes() {
            return true;
        }

        public AnnotationStrategy withoutAttributes() {
            return new AnnotationTypeStrategy(getAnnotationType(), annotation);
        }

        public Annotation getAnnotation() {
            return annotation;
        }

        public Class<? extends Annotation> getAnnotationType() {
            return annotation.annotationType();
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof AnnotationInstanceStrategy)) {
                return false;
            }

            AnnotationInstanceStrategy other = (AnnotationInstanceStrategy) o;
            return annotation.equals(other.annotation);
        }

        @Override
        public int hashCode() {
            return annotation.hashCode();
        }

        @Override
        public String toString() {
            return annotation.toString();
        }
    }

    static class AnnotationTypeStrategy implements AnnotationStrategy {

        final Class<? extends Annotation> annotationType;

        // Keep the instance around if we have it so the client can request it.
        final Annotation annotation;

        AnnotationTypeStrategy(Class<? extends Annotation> annotationType, Annotation annotation) {
            this.annotationType = checkNotNull(annotationType, "annotation type");
            this.annotation = annotation;
        }

        public boolean hasAttributes() {
            return false;
        }

        public AnnotationStrategy withoutAttributes() {
            throw new UnsupportedOperationException("Key already has no attributes.");
        }

        public Annotation getAnnotation() {
            return annotation;
        }

        public Class<? extends Annotation> getAnnotationType() {
            return annotationType;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof AnnotationTypeStrategy)) {
                return false;
            }

            AnnotationTypeStrategy other = (AnnotationTypeStrategy) o;
            return annotationType.equals(other.annotationType);
        }

        @Override
        public int hashCode() {
            return annotationType.hashCode();
        }

        @Override
        public String toString() {
            return "@" + annotationType.getName();
        }
    }
}