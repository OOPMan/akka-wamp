package akka.wamp.client

import akka.wamp.messages.Invocation
import akka.wamp.serialization.Payload
import org.scalamock.scalatest.MockFactory

import scala.concurrent.duration._
import scala.concurrent._

class CallerSpec extends ClientFixtureSpec with MockFactory {


  "A caller" should "fail call procedure when session closed" in { f =>
    f.withSession { session =>
      whenReady(session.close()) { _ =>
        val result = session.call("myapp.procedure")
        whenReady(result.failed) { ex =>
          ex mustBe a[SessionException]
          ex.getMessage mustBe "session closed"
        }
      }
    }
  }


  it should "succeed call procedure and expect result" in { f =>
    f.withSession { session1 =>
      val handler = stubFunction[Invocation, Future[Payload]]
      val payload = Future.successful(Payload(List("paolo", 40, true)))
      handler.when(*).returns(payload)
      val registration = session1.register("myapp.procedure")(handler)
      whenReady(registration) { registration =>
        registration.registered.requestId mustBe 1
        registration.registered.registrationId mustBe 1

        // TODO can a caller invoke itself procedures?

        // the caller shall be another connection actor,
        // otherwise the router wouldn't invoke the procedure
        f.withConnection { conn2 =>
          whenReady(conn2.openSession()) { session2 =>
            val result = session2.call("myapp.procedure")
            whenReady(result) { _ =>
              awaitAssert(handler.verify(*).once().returning(payload), 5 seconds)
            }
          }
        }
      }
    }
  }
  
}
