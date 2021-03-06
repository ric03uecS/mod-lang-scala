/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.vertx.scala

import org.vertx.java.core.{ Vertx => JVertx }
import org.vertx.java.core.{ VertxFactory => JVertxFactory }
import org.vertx.scala.core.datagram.InternetProtocolFamily._
import org.vertx.scala.core.eventbus.EventBus
import org.vertx.scala.core.http.{ HttpClient, HttpServer }
import org.vertx.scala.core.net.{ NetServer, NetClient }
import org.vertx.scala.core.sockjs.SockJSServer
import org.vertx.scala.core.file.FileSystem
import org.vertx.scala.core.FunctionConverters._
import org.vertx.scala.core.shareddata.SharedData
import org.vertx.java.core.Context
import java.net.InetSocketAddress
import org.vertx.scala.core.dns.DnsClient
import org.vertx.scala.core.datagram.{DatagramSocket, InternetProtocolFamily}

package object core {

  type AsyncResult[T] = org.vertx.java.core.AsyncResult[T]
  type Handler[T] = org.vertx.java.core.Handler[T]
  type MultiMap = org.vertx.java.core.MultiMap

  def newVertx() = new Vertx(JVertxFactory.newVertx())

  def newVertx(port: Int, hostname: String) = new Vertx(JVertxFactory.newVertx(port, hostname))

  def newVertx(hostname: String) = new Vertx(JVertxFactory.newVertx(hostname))

  implicit class Vertx(val internal: JVertx) {

    /**
     * Create a TCP/SSL server.
     */
    def createNetServer(): NetServer = NetServer(internal.createNetServer())

    /**
     * Create a TCP/SSL client.
     */
    def createNetClient(): NetClient = NetClient(internal.createNetClient())

    /**
     * Create an HTTP/HTTPS server.
     */
    def createHttpServer(): HttpServer = HttpServer(internal.createHttpServer())

    /**
     * Create a HTTP/HTTPS client.
     */
    def createHttpClient(): HttpClient = HttpClient(internal.createHttpClient())

    /**
     * Create a new [[org.vertx.scala.core.datagram.DatagramSocket]].
     *
     * @param family  optional user provided [[org.vertx.scala.core.datagram.InternetProtocolFamily]]
     *                to use for multicast. If [[scala.None]], it's up to the
     *                operation system to detect it's default.
     *@return socket the created [[org.vertx.scala.core.datagram.DatagramSocket]].
     */
    def createDatagramSocket(family: Option[InternetProtocolFamily] = None): DatagramSocket =
      DatagramSocket(internal.createDatagramSocket(toJavaIpFamily(family).getOrElse(null)))

    /**
     * Create a SockJS server that wraps an HTTP server.
     */
    def createSockJSServer(httpServer: HttpServer): SockJSServer = SockJSServer(internal.createSockJSServer(httpServer.toJava))

    /**
     * The File system object.
     */
    val fileSystem: FileSystem = FileSystem(internal.fileSystem())

    /**
     * The event bus.
     */
    val eventBus: EventBus = EventBus(internal.eventBus())

    /**
     * Return the {@link DnsClient}
     */
    def createDnsClient(dnsServers: InetSocketAddress*): DnsClient = DnsClient(internal.createDnsClient(dnsServers: _*))

    /**
     * The shared data object.
     */
    val sharedData: SharedData = SharedData(internal.sharedData())

    /**
     * Set a one-shot timer to fire after {@code delay} milliseconds, at which point {@code handler} will be called with
     * the id of the timer.
     * @return The unique ID of the timer.
     */
    def setTimer(delay: Long, handler: Long => Unit): Long = {
      internal.setTimer(delay, fnToHandler(handler.compose {
        l: java.lang.Long => Long.box(l)
      }))
    }

    /**
     * Set a periodic timer to fire every {@code delay} milliseconds, at which point {@code handler} will be called with
     * the id of the timer.
     * @return the unique ID of the timer.
     */
    def setPeriodic(delay: Long, handler: Long => Unit): Long = {
      internal.setPeriodic(delay, fnToHandler(handler.compose {
        l: java.lang.Long => Long.box(l)
      }))
    }

    /**
     * Cancel the timer with the specified {@code id}. Returns {@code} true if the timer was successfully cancelled, or
     * {@code false} if the timer does not exist.
     */
    def cancelTimer(id: Long): Boolean = internal.cancelTimer(id)

    /**
     * @return The current context.
     */
    def currentContext(): Context = internal.currentContext()

    /**
     * Put the handler on the event queue for the current loop (or worker context) so it will be run asynchronously ASAP after this event has
     * been processed.
     */
    def runOnContext(action: => Unit): Unit = internal.runOnContext(lazyToVoidHandler(action))

    /**
     * Is the current thread an event loop thread?
     * @return true if current thread is an event loop thread.
     */
    def isEventLoop(): Boolean = internal.isEventLoop()

    /**
     * Is the current thread an worker thread?
     * @return true if current thread is an worker thread.
     */
    def isWorker(): Boolean = internal.isWorker()

    /**
     * Stop the eventbus and any resource managed by the eventbus.
     */
    def stop(): Unit = internal.stop()

  }

}
