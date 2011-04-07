package com.magenta.guice.events;

import gnu.trove.THashMap;
import org.apache.bcel.Constants;
import org.apache.bcel.Repository;
import org.apache.bcel.generic.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import java.util.Map;

import static java.lang.reflect.Modifier.isPublic;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 22.06.2009
 * Time: 12:33:50
 */
public class ClassgenHandlerInvocator<T, L> extends ReflectionHandlerInvocator<T, L> {

    private static final Logger log = LoggerFactory.getLogger(ClassgenHandlerInvocator.class);

    private static final Map<Method, Invocator> INVOCATORS_MAP = new THashMap();

    private final Invocator<T, L> invocator;

    public interface Invocator<T, L> {
        Object invoke(L listener, T o);
    }

    public ClassgenHandlerInvocator(Method method) {
        super(method);

        if (!isPublic(method.getDeclaringClass().getModifiers()) || !isPublic(method.getModifiers())) {
            invocator = null;
        } else {
            invocator = getOrCreateInvocator(method, paramType);
        }
    }

    private static synchronized <T, L> Invocator<T, L> getOrCreateInvocator(Method method, Class paramType) {
        //noinspection unchecked
        Invocator<T, L> inv = INVOCATORS_MAP.get(method);
        if (inv == null) {
            try {
                long start = System.currentTimeMillis();
                Class<?> cls = method.getDeclaringClass();

                String mn = Repository.lookupClass(cls).getMethod(method).getSignature();

                mn = mn.replace('(', '$').replace(')', '$').replace(';', '$').replace('/', '$');

                ConstantPoolGen cpg = new ConstantPoolGen();
                final String invocatorClassName = cls.getName() + "$" + method.getName() + mn + "$Invocator";

                final String superClassName = Object.class.getName();
                final Type objectType = Type.getType(Object.class);

                ClassGen invocatorClass = new ClassGen(invocatorClassName, superClassName, "<generated>", Constants.ACC_PUBLIC | Constants.ACC_SUPER, new String[]{Invocator.class.getName()}, cpg);

                InstructionFactory factory = new InstructionFactory(invocatorClass);

                InstructionList methodCode = new InstructionList();

                methodCode.append(InstructionFactory.createLoad(objectType, 1));
                methodCode.append(factory.createCast(objectType, Type.getType(cls)));
                Type[] params;
                if (paramType != null) {
                    methodCode.append(InstructionFactory.createLoad(objectType, 2));
                    if (paramType != Object.class) {
                        methodCode.append(factory.createCast(objectType, Type.getType(paramType)));
                    }
                    params = new Type[]{Type.getType(paramType)};
                } else {
                    params = new Type[]{};
                }
                methodCode.append(factory.createInvoke(cls.getName(), method.getName(), Type.getType(method.getReturnType()), params, cls.isInterface() ? Constants.INVOKEINTERFACE : Constants.INVOKEVIRTUAL));
                if (method.getReturnType() == Void.TYPE) {
                    methodCode.append(InstructionFactory.createNull(Type.OBJECT));
                }
                methodCode.append(InstructionFactory.createReturn(Type.OBJECT));

                MethodGen m = new MethodGen(Constants.ACC_PUBLIC, Type.OBJECT, new Type[]{Type.OBJECT, Type.OBJECT}, null, "invoke", invocatorClassName, methodCode, cpg);
                m.setMaxStack(2);

                invocatorClass.addMethod(m.getMethod());

                invocatorClass.addEmptyConstructor(Constants.ACC_PUBLIC);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                invocatorClass.getJavaClass().dump(baos);

                final byte[] classData = baos.toByteArray();

                /*
                try {
                    // dump class files
                    FileOutputStream out = new FileOutputStream(new File("C:\\Proxy\\" + invocatorClassName + ".class"));
                    out.write(classData);
                    out.close();
                } catch (Exception e) {
                    // ignore
                }
                */


                ClassLoader cl = new ClassLoader(cls.getClassLoader()) {
                    @Override
                    public Class findClass(String name) throws ClassNotFoundException {
                        if (!name.equals(invocatorClassName)) {
                            throw new ClassNotFoundException(name);
                        }
                        return defineClass(name, classData, 0, classData.length);
                    }
                };

                //noinspection unchecked
                final Class<Invocator<T, L>> newClass = (Class<Invocator<T, L>>) cl.loadClass(invocatorClassName);

                assert newClass.getPackage().equals(cls.getPackage());

                long end = System.currentTimeMillis();

                log.debug("Generated invocator for " + method + " in " + (end - start) + " ms");

                inv = newClass.newInstance();
                INVOCATORS_MAP.put(method, inv);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return inv;
    }

    @Override
    public Object invoke(L instance, T message) {
        if (invocator == null) {
            return super.invoke(instance, message);
        } else {
            return invocator.invoke(instance, message);
        }
    }
}
