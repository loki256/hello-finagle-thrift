package zdavep
package app

import com.twitter.finagle.builder.ClientBuilder
import com.twitter.finagle.thrift.ThriftClientFramedCodec
import hello._
import org.apache.thrift.protocol.TBinaryProtocol

/**
 * Client app - uses ZooKeeper to find and call the hello service.
 */
object ClientApp extends App {

  val helloService = ClientBuilder().codec(ThriftClientFramedCodec())
    .dest("zk!localhost:2181!/helloService/api/v1").hostConnectionLimit(2).build()
  val helloClient = new HelloService$FinagleClient(helloService, new TBinaryProtocol.Factory())
  val name = if (args.length > 0) args(0) else "from Scala"
  val callServices = helloClient.ping() flatMap { _ =>
    helloClient.sayHello(HelloMsg(name))
  }
  callServices onSuccess { result =>
    println(s"Client received response message: ${result.name}")
  } onFailure { ex =>
    ex.printStackTrace()
  } ensure {
    helloService.close()
  }

} // ClientApp
