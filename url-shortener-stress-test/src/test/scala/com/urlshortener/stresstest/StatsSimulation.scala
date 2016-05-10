package com.urlshortener.stresstest

import java.util.UUID.randomUUID

import com.urlshortener.stresstest.configuration.ProdConfig
import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration._

class StatsSimulation extends Simulation with ProdConfig {

  val scn =
    scenario("Stats scenario")
      .repeat(1) {
        exec(_.set("user_id", randomUUID().toString))
        .exec(
          http("User creation")
          .post("/user/")
          .body(StringBody("""{ "id": "${user_id}" }""")).asJSON
          .check(status.is(201))
        )
        .repeat(3) {
          exec(_.set("url", randomUUID().toString))
          .exec(
            http("Url creation")
              .post("""/users/${user_id}/urls""")
              .body(StringBody("""{ "url": "${url}" }""")).asJSON
              .check(status.is(201), jsonPath("$.id").saveAs("url_id"))
          )
          .repeat(3) {
            exec(
              http("Url retrieval (Add hits)")
                .get("""/url/${url_id}""")
                .check(status.is(301))
            )
          }
          .repeat(3) {
            exec(
              http("Url stats retrieval")
                .get("""/stats/${url_id}""")
                .check(status.is(200))
            )
          }
        }
        .repeat(3) {
          exec(
            http("Global stats retrieval")
              .get("""/stats""")
              .check(status.is(200))
          )
        }
        .repeat(3) {
          exec(
            http("User stats retrieval")
              .get("""/user/${user_id}/stats""")
              .check(status.is(200))
          )
        }
    }

  setUp(scn.inject(
    constantUsersPerSec(100) during(30 seconds)
  ).protocols(httpConf))

}
