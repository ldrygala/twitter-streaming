import com.typesafe.config.{Config, ConfigFactory}
import org.apache.spark.SparkConf
import org.apache.spark.streaming.twitter.TwitterUtils
import org.apache.spark.streaming.{Minutes, Seconds, StreamingContext}
import twitter4j.auth.AccessToken
import twitter4j.{Twitter, TwitterFactory}

/**
 * Created by ldrygala on 2015-06-15.
 */
object TwitterStreamingApp extends App {
  val driverPort = 7777
  val driverHost = "localhost"

  val conf = new SparkConf(false)
    .setMaster("local[*]")
    .setAppName("Twitter Streaming App")
    .set("spark.logConf", "false")
    .set("spark.driver.port", s"$driverPort")
    .set("spark.driver.host", s"$driverHost")
    .set("spark.akka.logLifecycleEvents", "false")

  val streamingContext: StreamingContext = new StreamingContext(conf, Seconds(5))

  val configFactory: Config = ConfigFactory.load

  val twitter: Twitter = new TwitterFactory().getInstance()
  twitter.setOAuthConsumer(configFactory.getString("consumerKey"), configFactory.getString("consumerSecret"))
  twitter.setOAuthAccessToken(new AccessToken(configFactory.getString("accessToken"), configFactory.getString("accessTokenSecret")))

  val twitterStream = TwitterUtils.createStream(streamingContext, Option(twitter.getAuthorization))

  val hashTags = twitterStream.flatMap(status => status.getText.split(" ")).filter(_.startsWith("#"))
  val topCounts60 = hashTags.map((_, 1)).reduceByKeyAndWindow(_ + _, Minutes(1)).map {
    case (tag, count) => (count, tag)
  }.transform(_.sortByKey(false))

  topCounts60.foreachRDD { rdd =>
    rdd.take(10).foreach(println)
  }
  streamingContext.start()
  streamingContext.awaitTermination()
}
