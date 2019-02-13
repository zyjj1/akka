/*
 * Copyright (C) 2018-2019 Lightbend Inc. <https://www.lightbend.com>
 */

package akka.util;

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.lang.invoke.MethodHandles;

final class PlainUnsafeUsage {
  private volatile Object _refDoNotCallMeDirectly;

  @SuppressWarnings("unused")
  private final static long refOffset;

  static {
    try {
      refOffset = Unsafe.instance.objectFieldOffset(PlainUnsafeUsage.class.getDeclaredField("_refDoNotCallMeDirectly"));
    } catch(Throwable t) {
      throw new ExceptionInInitializerError(t);
    }
  }

  public final boolean cas(Object compare, Object swap) { return Unsafe.instance.compareAndSwapObject(this, refOffset, compare, swap); }
  public final void set(Object set) { Unsafe.instance.putObjectVolatile(this, refOffset, set); }
  public final void lazySet(Object set) { Unsafe.instance.putOrderedObject(this, refOffset, set); }
  public final Object get() { return Unsafe.instance.getObjectVolatile(this, refOffset); }
}

final class PlainVarHandleUsage {
  private volatile Object _refDoNotCallMeDirectly;

  private final static java.lang.invoke.VarHandle ref;

  static {
    try {
      ref = MethodHandles.lookup().findVarHandle(PlainVarHandleUsage.class, "_refDoNotCallMeDirectly", Object.class);
    } catch(Throwable t) {
      throw new ExceptionInInitializerError(t);
    }
  }

  public final boolean cas(Object compare, Object swap) { return ref.compareAndSet(this, compare, swap); }
  public final void set(Object set) { ref.setVolatile(this, set); }
  public final void lazySet(Object set) { ref.setRelease(this, set); }
  public final Object get() { return ref.getVolatile(this); } 
}

final class PlainAtomicUpdaterUsage {
  private volatile Object _refDoNotCallMeDirectly;

  private static final AtomicReferenceFieldUpdater<PlainAtomicUpdaterUsage, Object> ref =
     AtomicReferenceFieldUpdater.newUpdater(PlainAtomicUpdaterUsage.class, Object.class, "_refDoNotCallMeDirectly");

  public final boolean cas(Object compare, Object swap) { return ref.compareAndSet(this, compare, swap); }
  public final void set(Object set) { ref.set(this, set); }
  public final void lazySet(Object set) { ref.lazySet(this, set); }
  public final Object get() { return ref.get(this); } 
}

final class PlainAtomicReferenceUsage {
  private final AtomicReference<Object> ref = new AtomicReference<Object>(null);

  public final boolean cas(Object compare, Object swap) { return ref.compareAndSet(compare, swap); }
  public final void set(Object set) { ref.set(set); }
  public final void lazySet(Object set) { ref.lazySet(set); }
  public final Object get() { return ref.get(); }  
}

final class akkaAtomicReferenceUnsafeUsage {
  private volatile Object _refDoNotCallMeDirectly;
  private final static UnsafeAtomicAccessLookup lookup = new UnsafeAtomicAccessLookup();
  private final static Atomic.Reference<akkaAtomicReferenceUnsafeUsage, Object> ref =
    lookup.<akkaAtomicReferenceUnsafeUsage, Object>newReference(MethodHandles.lookup(), akkaAtomicReferenceUnsafeUsage.class, "_refDoNotCallMeDirectly", Object.class);

  public final boolean cas(Object compare, Object swap) { return ref.cas(this, compare, swap); }
  public final void set(Object set) { ref.set(this, set); }
  public final void lazySet(Object set) { ref.setOrdered(this, set); }
  public final Object get() { return ref.<akkaAtomicReferenceUnsafeUsage, Object>get(this); } 
}

final class akkaAtomicReferenceVarHandleUsage {
  private volatile Object _refDoNotCallMeDirectly;
  private final static DirectVarHandleAccessLookup lookup = new DirectVarHandleAccessLookup();
  private final static Atomic.Reference<akkaAtomicReferenceVarHandleUsage, Object> ref = 
    lookup.<akkaAtomicReferenceVarHandleUsage, Object>newReference(MethodHandles.lookup(), akkaAtomicReferenceVarHandleUsage.class, "_refDoNotCallMeDirectly", Object.class);

  public final boolean cas(Object compare, Object swap) { return ref.cas(this, compare, swap); }
  public final void set(Object set) { ref.set(this, set); }
  public final void lazySet(Object set) { ref.setOrdered(this, set); }
  public final Object get() { return ref.get(this); } 
}