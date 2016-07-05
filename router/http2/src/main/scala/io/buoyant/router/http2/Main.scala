package io.buoyant.router.http2

import com.twitter.finagle.{Service, Stack}
import com.twitter.finagle.buoyant.http2._
import com.twitter.finagle.transport.Transport
import com.twitter.server.TwitterServer
import com.twitter.util.{Await, Future}
import io.netty.handler.codec.http2.Http2StreamFrame
import java.net.InetSocketAddress
import java.util.concurrent.ConcurrentHashMap

object Main extends TwitterServer {

  private type Http2Transport = Transport[Http2StreamFrame, Http2StreamFrame]

  private val service = Service.mk[Request, Response] { req =>
    log.info(s"Main: service $req")
    Future.value(Response(ResponseHeaders(200)))
  }

  def main(): Unit = {
    val addr = new InetSocketAddress("127.1", 4142)

    val listener = Http2Listener.mk(Stack.Params.empty)

    log.info(s"Main: listening on $addr")
    val server = listener.listen(addr) { transport: Http2Transport =>
      log.info(s"Main: dispatch $transport")
      val _ = ServerDispatcher.dispatch(transport, service)
    }
    closeOnExit(server)
    Await.result(server)
  }

}
