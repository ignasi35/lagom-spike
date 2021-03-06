package com.knoldus.twitterconsumer.impl

import com.knoldus.twitterconsumer.api.TwitterProducerSubscriberService
import com.knoldus.twitterproducer.api.TwitterProducerService
import com.knoldus.twitterproducer.api.models.Tweet
import com.lightbend.lagom.scaladsl.persistence.ReadSide
import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.{ProducerStub, ProducerStubFactory, ServiceTest}
import org.scalatest.{AsyncWordSpec, Matchers}

/**
  * Created by harmeet on 23/2/17.
  */
class TwitterProducerSubscriberSpec extends AsyncWordSpec with Matchers {

  var producerStub: ProducerStub[Tweet] = _

  "Twitter kafka consumer " should {
    "consume tweets from topic" in ServiceTest.withServer(ServiceTest.defaultSetup.withCassandra(true)) { ctx =>
      new TwitterConsumerComponents(ctx) with LocalServiceLocator {

        val stubFactory = new ProducerStubFactory(actorSystem, materializer)
        producerStub = stubFactory.producer[Tweet](TwitterProducerService.TOPIC_NAME)
        override lazy val twitterService = new TwitterServiceStub(producerStub)
      }
    } { server =>

      val client = server.serviceClient.implement[TwitterProducerSubscriberService]

      val tweet = Tweet(833556819314409473l, 1487570407000l, 206645598, "javinpaul", "12 Advanced Java Programming " +
        "Books for Experienced Programmer", 7880)

      producerStub.send(tweet)

      client.latestTweet.invoke().map { latestTweet =>
        latestTweet should ===(tweet)
      }
    }
  }
}
