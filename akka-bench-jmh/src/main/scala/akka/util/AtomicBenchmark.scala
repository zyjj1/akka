/*
 * Copyright (C) 2014-2018 Lightbend Inc. <https://www.lightbend.com>
 */

package akka.util

import org.openjdk.jmh.annotations._
import org.openjdk.jmh.infra.Blackhole
import java.util.concurrent.TimeUnit

@State(Scope.Benchmark)
@BenchmarkMode(Array(Mode.Throughput))
@Fork(value = 1, jvmArgsAppend = Array("-Xmx512M", "-Xms512M", "-ea", "-server", "-XX:+UseCompressedOops", "-XX:+AlwaysPreTouch", "-XX:+UseCondCardMark"))
@Threads(1)
@Warmup(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS, batchSize = 1)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS, batchSize = 1)
class AtomicBenchmark {

  private[this] val setObject = new Object() {}

  private[this] var plainUnsafeReference: PlainUnsafeUsage = null
  private[this] var plainAtomicReference: PlainAtomicReferenceUsage = null
  private[this] var akkaAtomicReferenceUnsafe: akkaAtomicReferenceUnsafeUsage = null
  private[this] var akkaAtomicReferenceVarHandle: akkaAtomicReferenceVarHandleUsage = null
  private[this] var plainVarHandleReference: PlainVarHandleUsage = null
  private[this] var plainAtomicUpdaterReference: PlainAtomicUpdaterUsage = null

  @Setup(Level.Trial)
  def setup(): Unit = {
    plainUnsafeReference = new PlainUnsafeUsage()
    plainAtomicReference = new PlainAtomicReferenceUsage()
    akkaAtomicReferenceUnsafe = new akkaAtomicReferenceUnsafeUsage()
    akkaAtomicReferenceVarHandle = new akkaAtomicReferenceVarHandleUsage()
    plainVarHandleReference = new PlainVarHandleUsage()
    plainAtomicUpdaterReference = new PlainAtomicUpdaterUsage()
  }

  @TearDown(Level.Trial)
  def shutdown(): Unit = {
    plainUnsafeReference = null
    plainAtomicReference = null
    akkaAtomicReferenceUnsafe = null
    akkaAtomicReferenceVarHandle = null
    plainVarHandleReference = null
    plainAtomicUpdaterReference = null
  }

  @Benchmark
  @OperationsPerInvocation(1)
  def set_plainUnsafe(): Unit = { plainUnsafeReference.set(setObject) }

  @Benchmark
  @OperationsPerInvocation(1)
  def set_plainVarHandle(): Unit = { plainVarHandleReference.set(setObject) }

  @Benchmark
  @OperationsPerInvocation(1)
  def set_plainAtomicUpdater(): Unit = { plainAtomicUpdaterReference.set(setObject) }

  @Benchmark
  @OperationsPerInvocation(1)
  def set_plainAtomicReference(): Unit = { plainAtomicReference.set(setObject) }

  @Benchmark
  @OperationsPerInvocation(1)
  def set_akkaAtomicReferenceUnsafe(): Unit = { akkaAtomicReferenceUnsafe.set(setObject) }

  @Benchmark
  @OperationsPerInvocation(1)
  def set_akkaAtomicReferenceVarHandle(): Unit = { akkaAtomicReferenceVarHandle.set(setObject) }

  @Benchmark
  @OperationsPerInvocation(1)
  def get_plainUnsafe(blackhole: Blackhole): Unit = { blackhole.consume(plainUnsafeReference.get()) }

  @Benchmark
  @OperationsPerInvocation(1)
  def get_plainVarHandle(blackhole: Blackhole): Unit = { blackhole.consume(plainVarHandleReference.get()) }

  @Benchmark
  @OperationsPerInvocation(1)
  def get_plainAtomicUpdater(blackhole: Blackhole): Unit = { blackhole.consume(plainAtomicUpdaterReference.get()) }

  @Benchmark
  @OperationsPerInvocation(1)
  def get_plainAtomicReference(blackhole: Blackhole): Unit = { blackhole.consume(plainAtomicReference.get()) }

  @Benchmark
  @OperationsPerInvocation(1)
  def get_akkaAtomicReferenceUnsafe(blackhole: Blackhole): Unit = { blackhole.consume(akkaAtomicReferenceUnsafe.get()) }

  @Benchmark
  @OperationsPerInvocation(1)
  def get_akkaAtomicReferenceVarHandle(blackhole: Blackhole): Unit = { blackhole.consume(akkaAtomicReferenceVarHandle.get()) }

  @Benchmark
  @OperationsPerInvocation(1)
  def lazyset_plainUnsafe(): Unit = { plainUnsafeReference.lazySet(setObject) }

  @Benchmark
  @OperationsPerInvocation(1)
  def lazyset_plainVarHandle(): Unit = { plainVarHandleReference.lazySet(setObject) }

  @Benchmark
  @OperationsPerInvocation(1)
  def lazyset_plainAtomicUpdater(): Unit = { plainAtomicUpdaterReference.lazySet(setObject) }

  @Benchmark
  @OperationsPerInvocation(1)
  def lazyset_plainAtomicReference(): Unit = { plainAtomicReference.lazySet(setObject) }

  @Benchmark
  @OperationsPerInvocation(1)
  def lazyset_akkaAtomicReferenceUnsafe(): Unit = { akkaAtomicReferenceUnsafe.lazySet(setObject) }

  @Benchmark
  @OperationsPerInvocation(1)
  def lazyset_akkaAtomicReferenceVarHandle(): Unit = { akkaAtomicReferenceVarHandle.lazySet(setObject) }

  @Benchmark
  @OperationsPerInvocation(1)
  def cas_plainUnsafe(blackhole: Blackhole): Unit = { blackhole.consume(plainUnsafeReference.cas(setObject, setObject)) }

  @Benchmark
  @OperationsPerInvocation(1)
  def cas_plainVarHandle(blackhole: Blackhole): Unit = { blackhole.consume(plainVarHandleReference.cas(setObject, setObject)) }

  @Benchmark
  @OperationsPerInvocation(1)
  def cas_plainAtomicUpdater(blackhole: Blackhole): Unit = { blackhole.consume(plainAtomicUpdaterReference.cas(setObject, setObject)) }

  @Benchmark
  @OperationsPerInvocation(1)
  def cas_plainAtomicReference(blackhole: Blackhole): Unit = { blackhole.consume(plainAtomicReference.cas(setObject, setObject)) }

  @Benchmark
  @OperationsPerInvocation(1)
  def cas_akkaAtomicReferenceUnsafe(blackhole: Blackhole): Unit = { blackhole.consume(akkaAtomicReferenceUnsafe.cas(setObject, setObject)) }

  @Benchmark
  @OperationsPerInvocation(1)
  def cas_akkaAtomicReferenceVarHandle(blackhole: Blackhole): Unit = { blackhole.consume(akkaAtomicReferenceVarHandle.cas(setObject, setObject)) }

  @Benchmark
  @OperationsPerInvocation(1)
  def mixed_plainUnsafe(): Unit = {
    val o = plainUnsafeReference.get()
    if (plainUnsafeReference.cas(o, setObject)) ()
    else plainUnsafeReference.set(setObject)
  }

  @Benchmark
  @OperationsPerInvocation(1)
  def mixed_plainVarHandle(): Unit = {
    val o = plainVarHandleReference.get()
    if (plainVarHandleReference.cas(o, setObject)) ()
    else plainVarHandleReference.set(setObject)
  }

  @Benchmark
  @OperationsPerInvocation(1)
  def mixed_plainAtomicUpdater(): Unit = {
    val o = plainAtomicUpdaterReference.get()
    if (plainAtomicUpdaterReference.cas(o, setObject)) ()
    else plainAtomicUpdaterReference.set(setObject)
  }

  @Benchmark
  @OperationsPerInvocation(1)
  def mixed_plainAtomicReference(): Unit = {
    val o = plainAtomicReference.get()
    if (plainAtomicReference.cas(o, setObject)) ()
    else plainAtomicReference.set(setObject)
  }

  @Benchmark
  @OperationsPerInvocation(1)
  def mixed_akkaAtomicReferenceUnsafe(): Unit = {
    val o = akkaAtomicReferenceUnsafe.get()
    if (akkaAtomicReferenceUnsafe.cas(o, setObject)) ()
    else akkaAtomicReferenceUnsafe.set(setObject)
  }

  @Benchmark
  @OperationsPerInvocation(1)
  def mixed_akkaAtomicReferenceVarHandle(): Unit = {
    val o = akkaAtomicReferenceVarHandle.get()
    if (akkaAtomicReferenceVarHandle.cas(o, setObject)) ()
    else akkaAtomicReferenceVarHandle.set(setObject)
  }
}
