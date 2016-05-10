package com.urlshortener.stresstest.configuration
import io.gatling.http.Predef._

trait ProdConfig {
  val httpConf = http.baseURL("http://localhost:8080").disableFollowRedirect
}
