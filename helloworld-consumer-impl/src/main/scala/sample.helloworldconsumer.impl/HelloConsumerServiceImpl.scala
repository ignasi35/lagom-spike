package sample.helloworldconsumer.impl

import akka.stream.scaladsl.Flow
import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import org.slf4j.{Logger, LoggerFactory}
import sample.helloworld.api.HelloService
import sample.helloworld.api.model.GreetingMessage
import sample.helloworldconsumer.api.HelloConsumerService
import sample.helloworldconsumer.impl.repositories.MessageRepository


class HelloConsumerServiceImpl (registery: PersistentEntityRegistry ,helloService: HelloService ,msgRepository:MessageRepository) extends HelloConsumerService {

  val HUNDRED = 100
  val logger: Logger = LoggerFactory.getLogger(this.getClass())

  helloService.greetingsTopic
    .subscribe
    .atLeastOnce(
      Flow[GreetingMessage].map{ msg =>
        putGreetingMessage(msg)
        Done
      }
    )

  var lastObservedMessage: String = _
  private def putGreetingMessage(greetingMessage: GreetingMessage) = {
    logger.info(s"obersrve new message ${greetingMessage.message}")
    entityRef(greetingMessage.message.toString).ask(SaveNewMessage(greetingMessage.message))
    lastObservedMessage = greetingMessage.message
  }

  override def findTopHundredWordCounts(): ServiceCall[NotUsed, Map[String, Int]] = ServiceCall {
        //fetch top 100 message and perform word count
    req => msgRepository.fetchAndCountWordsFromMessages(HUNDRED)
  }

  override  def foo():ServiceCall[NotUsed, String] = ServiceCall{
    req => scala.concurrent.Future.successful(lastObservedMessage)
  }

  private def entityRef(id: String) = registery.refFor[MessageEntity](id)
}
