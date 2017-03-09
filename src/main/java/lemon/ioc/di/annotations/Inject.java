package lemon.ioc.di.annotations;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotates members of your implementation class (constructors, methods
 * and fields) into which the {@link Injector} should inject values.
 * The Injector fulfills injection requests for:
 *
 * <ul>
 * <li>Every instance it constructs. The class being constructed must have
 * exactly one of its constructors marked with {@code @Inject} or must have a
 * constructor taking no parameters. The Injector then proceeds to perform
 * field and method injections.
 * 
 * <li>Pre-constructed instances passed to {@link Injector#injectMembers},
 * {@link com.google.inject.binder.LinkedBindingBuilder#toInstance(Object)} and
 * {@link com.google.inject.binder.LinkedBindingBuilder#toProvider(javax.inject.Provider)}.
 * In this case all constructors are, of course, ignored.
 *
 * <li>Static fields and methods of classes which any {@link Module} has
 * specifically requested static injection for, using
 * {@link Binder#requestStaticInjection}.
 * </ul>
 *
 * In all cases, a member can be injected regardless of its Java access
 * specifier (private, default, protected, public).
 *
 * @author crazybob@google.com (Bob Lee)
 */
@Target({ METHOD, CONSTRUCTOR, FIELD })
@Retention(RUNTIME)
@Documented
public @interface Inject {

    /**
     * If true, and the appropriate binding is not found,
     * the Injector will skip injection of this method or field rather than
     * produce an error. When applied to a field, any default value already
     * assigned to the field will remain (guice will not actively null out the
     * field). When applied to a method, the method will only be invoked if
     * bindings for <i>all</i> parameters are found. When applied to a
     * constructor, an error will result upon Injector creation.
     */
    boolean optional() default false;
}
