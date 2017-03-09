package lemon.ioc.dinjection.binder;
//
//package lemon.ioc.di.binder;
//
//import lemon.ioc.di.TypeLiteral;
//
///**
// * Listens for Guice to encounter injectable types. If a given type has its constructor injected in
// * one situation but only its methods and fields injected in another, Guice will notify this
// * listener once.
// *
// * <p>Useful for extra type checking, {@linkplain TypeEncounter#register(InjectionListener)
// * registering injection listeners}, and {@linkplain TypeEncounter#bindInterceptor(
// * com.google.inject.matcher.Matcher, org.aopalliance.intercept.MethodInterceptor[])
// * binding method interceptors}.
// * 
// * @since 2.0
// */
//public interface TypeListener {
//
//    /**
//     * Invoked when Guice encounters a new type eligible for constructor or members injection.
//     * Called during injector creation (or afterwords if Guice encounters a type at run time and
//     * creates a JIT binding).
//     *
//     * @param type encountered by Guice
//     * @param encounter context of this encounter, enables reporting errors, registering injection
//     *     listeners and binding method interceptors for {@code type}.
//     *
//     * @param <I> the injectable type
//     */
//    <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter);
//
//}