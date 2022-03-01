package net.neoremind.dynamicproxy.impl;

import java.lang.reflect.Method;
import java.util.Map;

import com.google.common.collect.Maps;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import net.neoremind.dynamicproxy.Interceptor;
import net.neoremind.dynamicproxy.MethodSignature;
import net.neoremind.dynamicproxy.ObjectInvoker;
import net.neoremind.dynamicproxy.ObjectProvider;
import net.neoremind.dynamicproxy.exception.ProxyCreatorException;
import net.neoremind.dynamicproxy.template.ClassCache;
import net.neoremind.dynamicproxy.template.GeneratorTemplate;
import net.neoremind.dynamicproxy.template.SubclassCreatorTemplate;
import net.neoremind.dynamicproxy.util.JavassistUtil;
import net.neoremind.dynamicproxy.util.ProxyUtil;

/**
 * JavassistCreator
 *
 * @author zhangxu
 */
public class JavassistCreator extends SubclassCreatorTemplate {

    private static final String GET_METHOD_NAME = "_javassistGetMethod";

    private static final ClassCache DELEGATING_PROXY_CACHE = new ClassCache(new DelegatingProxyClassGenerator());
    private static final ClassCache INTERCEPTOR_PROXY_CACHE = new ClassCache(new InterceptorProxyClassGenerator());
    private static final ClassCache INVOKER_PROXY_CACHE = new ClassCache(new InvokerProxyClassGenerator());

    private static final Map<String, Method> methodCache = Maps.newHashMap();

    /**
     * 对该方法进行性能优化，即使使用了Javassist，但是利用了反射，性能仍然很差，改为缓存Method对象。
     *
     * @param proxyClass 代理的CtClass
     *
     * @throws CannotCompileException
     */
    private static void addGetMethodMethod(CtClass proxyClass) throws CannotCompileException {
        CtMethod method =
                new CtMethod(JavassistUtil.resolve(Method.class), GET_METHOD_NAME, JavassistUtil.resolve(new Class[] {
                        String.class, String.class, Class[].class}), proxyClass);
        //                String body =
        //                        "try { return Class.forName($1).getMethod($2, $3); } catch( Exception e ) "
        //                                + "{ throw new RuntimeException(\"Unable to look up method.\", e); }";
        String body =
                "try {\n"
                        + "return net.neoremind.dynamicproxy.impl.JavassistCreator.getMethodCache($1,$2,$3);\n"
                        + "} catch (Exception e) {\n"
                        + "    throw new RuntimeException(\"Unable to look up method.\", e);\n"
                        + "}";
        method.setBody(body);
        proxyClass.addMethod(method);
    }

    @Override
    public <T> T createDelegatorProxy(ClassLoader classLoader, ObjectProvider<?> targetProvider,
                                      Class<?>... proxyClasses) {
        try {
            @SuppressWarnings("unchecked")
            Class<? extends T> clazz =
                    (Class<? extends T>) DELEGATING_PROXY_CACHE.getProxyClass(classLoader, proxyClasses);

            return clazz.getConstructor(ObjectProvider.class).newInstance(targetProvider);
        } catch (Exception e) {
            throw new ProxyCreatorException("Unable to instantiate proxy from generated proxy class.", e);
        }
    }

    @Override
    public <T> T createInterceptorProxy(ClassLoader classLoader, Object target, Interceptor interceptor,
                                        Class<?>... proxyClasses) {
        try {
            @SuppressWarnings("unchecked")
            Class<? extends T> clazz =
                    (Class<? extends T>) INTERCEPTOR_PROXY_CACHE.getProxyClass(classLoader, proxyClasses);

            return clazz.getConstructor(Object.class, Interceptor.class).newInstance(target, interceptor);
        } catch (Exception e) {
            throw new ProxyCreatorException("Unable to instantiate proxy class instance.", e);
        }
    }

    @Override
    public <T> T createInvokerProxy(ClassLoader classLoader, ObjectInvoker invoker, Class<?>... proxyClasses) {
        try {
            @SuppressWarnings("unchecked")
            Class<? extends T> clazz =
                    (Class<? extends T>) INVOKER_PROXY_CACHE.getProxyClass(classLoader, proxyClasses);

            return clazz.getConstructor(ObjectInvoker.class).newInstance(invoker);
        } catch (Exception e) {
            throw new ProxyCreatorException("Unable to instantiate proxy from generated proxy class.", e);
        }
    }

    private static class DelegatingProxyClassGenerator extends GeneratorTemplate {
        @Override
        public Class<?> generateProxyClass(ClassLoader classLoader, Class<?>... proxyClasses) {
            try {
                CtClass proxyClass = JavassistUtil.createClass(getSuperclass(proxyClasses));
                JavassistUtil.addField(ObjectProvider.class, "provider", proxyClass);
                CtConstructor proxyConstructor =
                        new CtConstructor(JavassistUtil.resolve(new Class[] {ObjectProvider.class}), proxyClass);
                proxyConstructor.setBody("{ this.provider = $1; }");
                proxyClass.addConstructor(proxyConstructor);
                JavassistUtil.addInterfaces(proxyClass, toInterfaces(proxyClasses));
                addHashCodeMethod(proxyClass);
                addEqualsMethod(proxyClass);
                Method[] methods = getImplementationMethods(proxyClasses);
                for (int i = 0; i < methods.length; ++i) {
                    if (!ProxyUtil.isEqualsMethod(methods[i]) && !ProxyUtil.isHashCode(methods[i])) {
                        Method method = methods[i];
                        CtMethod ctMethod =
                                new CtMethod(JavassistUtil.resolve(method.getReturnType()), method.getName(),
                                        JavassistUtil.resolve(method.getParameterTypes()), proxyClass);
                        String body =
                                "{ return ( $r ) ( ( " + method.getDeclaringClass().getName()
                                        + " )provider.getObject() )." + method.getName() + "($$); }";
                        ctMethod.setBody(body);
                        proxyClass.addMethod(ctMethod);
                    }
                }

                return proxyClass.toClass(classLoader, null);
            } catch (CannotCompileException e) {
                throw new ProxyCreatorException("Could not compile class.", e);
            }
        }
    }

    private static class InterceptorProxyClassGenerator extends GeneratorTemplate {
        @Override
        public Class<?> generateProxyClass(ClassLoader classLoader, Class<?>... proxyClasses) {
            try {
                CtClass proxyClass = JavassistUtil.createClass(getSuperclass(proxyClasses));
                Method[] methods = getImplementationMethods(proxyClasses);
                JavassistUtil.addInterfaces(proxyClass, toInterfaces(proxyClasses));
                JavassistUtil.addField(Object.class, "target", proxyClass);
                JavassistUtil.addField(Interceptor.class, "interceptor", proxyClass);
                addGetMethodMethod(proxyClass);
                addHashCodeMethod(proxyClass);
                addEqualsMethod(proxyClass);
                CtConstructor proxyConstructor =
                        new CtConstructor(JavassistUtil.resolve(new Class[] {Object.class, Interceptor.class}),
                                proxyClass);
                proxyConstructor.setBody("{\n\tthis.target = $1;\n\tthis.interceptor = $2; }");
                proxyClass.addConstructor(proxyConstructor);
                for (int i = 0; i < methods.length; ++i) {
                    if (!ProxyUtil.isEqualsMethod(methods[i]) && !ProxyUtil.isHashCode(methods[i])) {
                        CtMethod method =
                                new CtMethod(JavassistUtil.resolve(methods[i].getReturnType()), methods[i].getName(),
                                        JavassistUtil.resolve(methods[i].getParameterTypes()), proxyClass);
                        Class<?> invocationClass =
                                JavassistInvocation.getMethodInvocationClass(classLoader, methods[i]);

                        String body =
                                "{\n\t return ( $r ) interceptor.intercept( new " + invocationClass.getName()
                                        + "( this, target, " + GET_METHOD_NAME + "(\""
                                        + methods[i].getDeclaringClass().getName() + "\", \"" + methods[i].getName()
                                        + "\", $sig), $args ) );\n }";
                        method.setBody(body);
                        proxyClass.addMethod(method);
                    }

                }

                return proxyClass.toClass(classLoader, null);
            } catch (CannotCompileException e) {
                throw new ProxyCreatorException("Could not compile class.", e);
            }
        }

    }

    private static void addEqualsMethod(CtClass proxyClass) throws CannotCompileException {
        CtMethod equalsMethod =
                new CtMethod(JavassistUtil.resolve(Boolean.TYPE), "equals",
                        JavassistUtil.resolve(new Class[] {Object.class}), proxyClass);
        String body = "{\n\treturn this == $1;\n}";
        equalsMethod.setBody(body);
        proxyClass.addMethod(equalsMethod);
    }

    private static void addHashCodeMethod(CtClass proxyClass) throws CannotCompileException {
        CtMethod hashCodeMethod =
                new CtMethod(JavassistUtil.resolve(Integer.TYPE), "hashCode", new CtClass[0], proxyClass);
        hashCodeMethod.setBody("{\n\treturn System.identityHashCode(this);\n}");
        proxyClass.addMethod(hashCodeMethod);
    }

    private static class InvokerProxyClassGenerator extends GeneratorTemplate {
        @Override
        public Class<?> generateProxyClass(ClassLoader classLoader, Class<?>... proxyClasses) {
            try {
                CtClass proxyClass = JavassistUtil.createClass(getSuperclass(proxyClasses));
                Method[] methods = getImplementationMethods(proxyClasses);
                JavassistUtil.addInterfaces(proxyClass, toInterfaces(proxyClasses));
                JavassistUtil.addField(ObjectInvoker.class, "invoker", proxyClass);
                CtConstructor proxyConstructor =
                        new CtConstructor(JavassistUtil.resolve(new Class[] {ObjectInvoker.class}), proxyClass);
                proxyConstructor.setBody("{\n\tthis.invoker = $1; }");
                proxyClass.addConstructor(proxyConstructor);
                addGetMethodMethod(proxyClass);
                addHashCodeMethod(proxyClass);
                addEqualsMethod(proxyClass);
                for (int i = 0; i < methods.length; ++i) {
                    if (!ProxyUtil.isEqualsMethod(methods[i]) && !ProxyUtil.isHashCode(methods[i])) {
                        CtMethod method =
                                new CtMethod(JavassistUtil.resolve(methods[i].getReturnType()), methods[i].getName(),
                                        JavassistUtil.resolve(methods[i].getParameterTypes()), proxyClass);
                        String body =
                                "{\n\t return ( $r ) invoker.invoke( this, " + GET_METHOD_NAME + "(\""
                                        + methods[i].getDeclaringClass().getName() + "\", \"" + methods[i].getName()
                                        + "\", $sig), $args );\n }";
                        method.setBody(body);
                        proxyClass.addMethod(method);
                    }
                }

                return proxyClass.toClass(classLoader, null);
            } catch (CannotCompileException e) {
                throw new ProxyCreatorException("Could not compile class.", e);
            }
        }
    }

    // FIXME
    public static synchronized Method getMethodCache(final String className, final String methodName,
                                                     final Class<?>[] parameterTypes)
            throws ClassNotFoundException, NoSuchMethodException {
        String methodSignature = MethodSignature.getName(className, methodName, parameterTypes);
        Method method = methodCache.get(methodSignature);
        if (method == null) {
            method = Class.forName(className).getMethod(methodName, parameterTypes);
            methodCache.put(methodSignature, method);
            return method;
        }
        return method;
    }
}
