package com.urlshortener.stresstest

import java.util.UUID.randomUUID

import com.urlshortener.stresstest.configuration.ProdConfig
import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration._

class UrlSimulation extends Simulation with ProdConfig {

  val scn =
    scenario("Stats scenario")
      .exec(_.set("user_id", randomUUID().toString))
      .exec(_.set("url", randomUUID().toString))
      .repeat(1) {
        exec(
          http("User creation")
            .post("/user/")
            .body(StringBody("""{ "id": "${user_id}" }""")).asJSON
            .check(status.is(201))
        )
        .exec(
            http("Url creation")
              .post("""/users/${user_id}/urls""")
              .body(StringBody("""{ "url": "${url}" }""")).asJSON
              .check(status.is(201), jsonPath("$.id").saveAs("url_response"))
        )
          .exec(
            http("Url deletion")
              .delete("""/url/${url_response}""")
              .check(status.is(204))
          )
    }

  setUp(scn.inject(
      constantUsersPerSec(100) during(30 seconds)
  ).protocols(httpConf))

}
