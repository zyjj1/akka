/*
 * Copyright (C) 2009-2019 Lightbend Inc. <https://www.lightbend.com>
 */

package akka.util

import java.lang.invoke.{ MethodHandle, MethodHandles }
import java.lang.invoke.MethodType.methodType
import java.lang.reflect.Field

final class UnsafeAtomicAccessLookup() extends Atomic.Lookup {
  import UnsafeStatics._
  override final def onSpinWait(): Unit = (onSpinWaitHandle.invokeExact(): Unit)

  override def newReference[O, T](lookup: MethodHandles.Lookup, ownerType: Class[O], fieldName: String, fieldType: Class[T]): Atomic.Reference[O, T] = {
    val field = lookup.lookupClass.getDeclaredField(fieldName)
    require(field.getType == fieldType)
    require(ownerType == lookup.lookupClass)
    val fieldOffset = (getFieldOffsetHandle.invokeExact(field): Long)
    new Atomic.Reference[O, T] {
      private[this] final val offset = fieldOffset
      override final def cas(owner: O, compare: T, swap: T): Boolean = (casObjectHandle.invokeExact(owner, offset, compare, swap): Boolean)
      override final def set(owner: O, value: T): Unit = (setObjectVolatileHandle.invokeExact(owner, offset, value): Unit)
      override final def setOrdered(owner: O, value: T): Unit = (lazySetObjectHandle.invokeExact(owner, offset, value): Unit)
      override final def get(owner: O): T = (getObjectVolatileHandle.invokeExact(owner, offset): T)
    }
  }
}

final class VarHandleAccessLookup() extends Atomic.Lookup {
  import VarHandleStatics._
  override final def onSpinWait(): Unit = (onSpinWaitHandle.invokeExact(): Unit)

  override def newReference[O, T](lookup: MethodHandles.Lookup, ownerType: Class[O], fieldName: String, fieldType: Class[T]): Atomic.Reference[O, T] = {
    val vh = findVarHandle.invoke(lookup, ownerType, fieldName, fieldType)
    val casO = (createAccessorMethodHandle.invoke(lookupAccessModeHandle.invoke("compareAndSet"), methodType(java.lang.Boolean.TYPE, ownerType, fieldType, fieldType)): MethodHandle)
    val setOV = (createAccessorMethodHandle.invoke(lookupAccessModeHandle.invoke("setVolatile"), methodType(java.lang.Void.TYPE, ownerType, fieldType)): MethodHandle)
    val lazySetO = (createAccessorMethodHandle.invoke(lookupAccessModeHandle.invoke("setRelease"), methodType(java.lang.Void.TYPE, ownerType, fieldType)): MethodHandle)
    val getOV = (createAccessorMethodHandle.invoke(lookupAccessModeHandle.invoke("getVolatile"), methodType(fieldType, ownerType)): MethodHandle)

    new Atomic.Reference[O, T] {
      private[this] val h = vh
      override final def cas(owner: O, compare: T, swap: T): Boolean = (casO.invokeExact(h, owner, compare, swap): Boolean)
      override final def set(owner: O, value: T): Unit = (setOV.invokeExact(h, owner, value): Unit)
      override final def setOrdered(owner: O, value: T): Unit = (lazySetO.invokeExact(h, owner, value): Unit)
      override final def get(owner: O): T = (getOV.invokeExact(h, owner): T)
    }
  }
}
