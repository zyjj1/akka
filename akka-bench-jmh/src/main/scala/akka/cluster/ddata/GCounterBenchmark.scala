/*
 * Copyright (C) 2015-2020 Lightbend Inc. <https://www.lightbend.com>
 */

package akka.cluster.ddata

import java.util.concurrent.TimeUnit

import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Level
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.Warmup
import org.openjdk.jmh.annotations.{ Scope => JmhScope }

import akka.actor.Address
import akka.cluster.UniqueAddress

@State(JmhScope.Benchmark)
@BenchmarkMode(Array(Mode.Throughput))
@Warmup(iterations = 4)
@Measurement(iterations = 5)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
class GCounterBenchmark {

  val nodeA = UniqueAddress(Address("akka", "Sys", "aaaa", 2552), 1L)
  val nodeB = UniqueAddress(nodeA.address.copy(host = Some("bbbb")), 2L)
  val nodeC = UniqueAddress(nodeA.address.copy(host = Some("cccc")), 3L)
  val nodeD = UniqueAddress(nodeA.address.copy(host = Some("dddd")), 4L)
  val nodeE = UniqueAddress(nodeA.address.copy(host = Some("eeee")), 5L)
  val nodes = Vector(nodeA, nodeB, nodeC, nodeD, nodeE)
  val nodesIndex = Iterator.from(0)
  def nextNode(): UniqueAddress = nodes(nodesIndex.next() % nodes.size)

  var counter1: GCounter = _
  var addFromSameNode: GCounter = _
  var addFromOtherNode: GCounter = _
  var complex1: GCounter = _
  var complex2: GCounter = _

  @Setup(Level.Trial)
  def setup(): Unit = {
    counter1 = GCounter().increment(nodeA).increment(nodeB).increment(nodeC)
    addFromSameNode = counter1.increment(nodeA).merge(counter1)
    addFromOtherNode = counter1.increment(nodeB).merge(counter1)
    complex1 = counter1.increment(nodeD).merge(counter1)
    complex2 = GCounter().increment(nodeE).merge(counter1)
  }

  @Benchmark
  def mergeAddFromSameNode: GCounter = {
    // this is the scenario when updating and then merging with local value
    // set2 produced by modify function
    val counter2 = counter1.increment(nodeA).increment(nodeA)
    // replicator merges with local value
    counter1.merge(counter2)
  }

  @Benchmark
  def mergeAddFromOtherNode: GCounter = counter1.merge(addFromOtherNode)

  @Benchmark
  def mergeAddFromBothNodes: GCounter = addFromSameNode.merge(addFromOtherNode)

  @Benchmark
  def mergeComplex: GCounter = complex1.merge(complex2)

}
