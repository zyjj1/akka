/*
 * Copyright (C) 2009-2018 Lightbend Inc. <https://www.lightbend.com>
 */

package akka.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.invoke.MethodHandles;

import static java.lang.invoke.MethodType.methodType;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class Atomic {
   public final static Lookup lookup;

   static {
     try {
       final RuntimeException initializationException = new RuntimeException();
       Lookup l = null;

       if (l == null)
         try { l = new UnsafeAtomicAccessLookup(); } catch(Throwable t) { initializationException.addSuppressed(t); }

       if (l == null)
         try { l = new VarHandleAccessLookup(); } catch(Throwable t) { initializationException.addSuppressed(t); }

       if(l == null) throw initializationException;
       else lookup = l;
     } catch (Throwable t) {
        throw new ExceptionInInitializerError(t);
     }
   }

   public final static void onSpinWait() {
     lookup.onSpinWait();
   }

   public static abstract class Lookup {
     abstract void onSpinWait();
     public abstract <O, T> Reference<O, T> newReference(MethodHandles.Lookup lookup, Class<O> ownerType, String fieldName, Class<T> fieldType);
   }

   public static abstract class Reference<O, T> {
     public abstract boolean cas(O owner, T compare, T swap);
     public abstract void set(O owner, T value);
     public abstract void setOrdered(O owner, T value);
     public abstract T get(O owner);
   }

   public static abstract class Int {
     public abstract boolean cas(Object owner, int compare, int swap);
     public abstract void set(Object owner, int value);
     public abstract void setOrdered(Object owner, int value);
     public abstract int get(Object owner);
   }

   public static abstract class Long {
     public abstract boolean cas(Object owner, long compare, long swap);
     public abstract void set(Object owner, long value);
     public abstract void setOrdered(Object owner, long value);
     public abstract long get(Object owner);
   }

   public static abstract class Boolean {
     public abstract boolean cas(Object owner, boolean compare, boolean swap);
     public abstract void set(Object owner, boolean value);
     public abstract void setOrdered(Object owner, boolean value);
     public abstract boolean get(Object owner);
   }

   public final static <O, T> Reference<O, T> Reference(MethodHandles.Lookup mhLookup, Class<O> ownerType, String fieldName, Class<T> fieldType) {
     return lookup.<O, T>newReference(mhLookup, ownerType, fieldName, fieldType);
   }
}

final class UnsafeStatics {
  final static MethodHandle getObjectVolatileHandle;
  final static MethodHandle getFieldOffsetHandle;
  final static MethodHandle lazySetObjectHandle;
  final static MethodHandle casObjectHandle;
  final static MethodHandle setObjectVolatileHandle;
  final static MethodHandle onSpinWaitHandle;

  private final static void onSpinWait() {}

  static {
      try {
        final MethodHandles.Lookup bootstrapLookup = MethodHandles.lookup();
        final Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
        Object unsafe = null;

        for (Field f : unsafeClass.getDeclaredFields()) {
          if (f.getType() == unsafeClass) {
            f.setAccessible(true);
            unsafe = f.get(null);
            break;
          }
        }

        if (unsafe == null)
          throw new IllegalStateException("Can't find instance of sun.misc.Unsafe");
        else {
          getObjectVolatileHandle = bootstrapLookup.findVirtual(unsafeClass, "getObjectVolatile", methodType(Object.class, Object.class, Long.TYPE)).bindTo(unsafe);
          getFieldOffsetHandle    = bootstrapLookup.findVirtual(unsafeClass, "objectFieldOffset", methodType(Long.TYPE, Field.class)).bindTo(unsafe);
          lazySetObjectHandle     = bootstrapLookup.findVirtual(unsafeClass, "putOrderedObject", methodType(Void.TYPE, Object.class, Long.TYPE, Object.class)).bindTo(unsafe);
          casObjectHandle         = bootstrapLookup.findVirtual(unsafeClass, "compareAndSwapObject", methodType(Boolean.TYPE, Object.class, Long.TYPE, Object.class, Object.class)).bindTo(unsafe);
          setObjectVolatileHandle = bootstrapLookup.findVirtual(unsafeClass, "putObjectVolatile", methodType(Void.TYPE, Object.class, Long.TYPE, Object.class)).bindTo(unsafe);

          MethodHandle onSpin;
          try {
            onSpin = bootstrapLookup.findStatic(Thread.class, "onSpinWait", methodType(Void.TYPE));
          } catch(NoSuchMethodException ignore) {
            onSpin = bootstrapLookup.findStatic(UnsafeStatics.class, "onSpinWait", methodType(Void.TYPE));
          }
          onSpinWaitHandle = onSpin;
        }
      } catch(Throwable t) {
        throw new ExceptionInInitializerError(t);
      }
    }
}

final class DirectVarHandleAccessLookup extends Atomic.Lookup {
   @Override void onSpinWait() { Thread.onSpinWait(); }
   @Override public <O, T> Atomic.Reference<O, T> newReference(MethodHandles.Lookup lookup, Class<O> ownerType, String fieldName, Class<T> fieldType) {
     try {
         final java.lang.invoke.VarHandle vh = lookup.findVarHandle(ownerType, fieldName, fieldType);
         return new Atomic.Reference<O, T>() {
           @Override public final boolean cas(O owner, T compare, T swap) { return vh.compareAndSet(owner, compare, swap); }
           @Override public final void set(O owner, T value) { vh.setVolatile(owner, value); }
           @Override public final void setOrdered(O owner, T value) { vh.setRelease(owner, value); }
           @Override public final T get(O owner) { return (T)vh.get(owner); }
         };
     } catch(Throwable t) {
        throw new RuntimeException(t);
     }
   }
}

final class VarHandleStatics {
  final static MethodHandle onSpinWaitHandle;
  final static MethodHandle findVarHandle;
  final static MethodHandle createAccessorMethodHandle;
  final static MethodHandle lookupAccessModeHandle;
  final static Class<?> varHandleClass;

  private final static void onSpinWait() {}

  static {
    try {
      final MethodHandles.Lookup bootstrapLookup = MethodHandles.lookup();

      //These classes are available on JDK8+ so we can refer to them statically.
      final Class<?> mtClass   = MethodType.class;
      final Class<?> mhsClass  = MethodHandles.class;
      final Class<?> mhsLClass = MethodHandles.Lookup.class;

      //These classes are only available on Java9+ so let's only refer to them dynamically.
      final Class<?> vhClass   = Class.forName("java.lang.invoke.VarHandle");
      final Class<?> vhAMClass = Class.forName("java.lang.invoke.VarHandle$AccessMode");

      findVarHandle = bootstrapLookup.findVirtual(mhsLClass, "findVarHandle", methodType(vhClass, Class.class, String.class, Class.class));

      // For some reason I can't use bootstrapLookup.findStatic to access these methods, so using reflect+unreflect instead.
      lookupAccessModeHandle = bootstrapLookup.unreflect(vhAMClass.getMethod("valueFromMethodName", String.class));
      createAccessorMethodHandle = bootstrapLookup.unreflect(mhsClass.getMethod("varHandleExactInvoker", vhAMClass, mtClass));

      varHandleClass = vhClass;

      MethodHandle onSpin;  

      try {
        onSpin = bootstrapLookup.findStatic(Thread.class, "onSpinWait", methodType(Void.TYPE));
      } catch(NoSuchMethodException ignore) {
        onSpin = bootstrapLookup.findStatic(UnsafeStatics.class, "onSpinWait", methodType(Void.TYPE));
      }
      onSpinWaitHandle = onSpin;
    } catch(Throwable t) {
      throw new ExceptionInInitializerError(t);
    }
  }
}