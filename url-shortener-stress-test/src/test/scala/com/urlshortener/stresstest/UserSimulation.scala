package com.urlshortener.stresstest

import java.util.UUID.randomUUID

import com.urlshortener.stresstest.configuration.ProdConfig
import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration._

class UserSimulation  extends Simulation with ProdConfig {

  val scn =
    scenario("User scenarios")
      .exec(_.set("user_id", randomUUID().toString))
      .repeat(1) {
        exec(
          http("User creation")
            .post("/user/")
            .body(StringBody("""{ "id": "${user_id}" }""")).asJSON
            .check(status.is(201))
        )
        .exec(
            http("User deletion")
              .delete("""/user/${user_id}""")
              .check(status.is(204))
        )
    }

  setUp(scn.inject(
    constantUsersPerSec(100) during(30 seconds)
  ).protocols(httpConf))

}
