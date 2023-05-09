package imagerequests

import getDoublePropertyOr
import getLongPropertyOr
import getStringPropertyOr
import io.gatling.javaapi.core.*
import io.gatling.javaapi.core.CoreDsl.*
import io.gatling.javaapi.http.HttpDsl.*
import java.time.Duration

class ImageRequestsSimulation : Simulation() {

    private val usersPerSecond = getDoublePropertyOr("usersPerSecond", 10.0)
    private val duration = getLongPropertyOr("duration", 1L)
    private val testsFile = getStringPropertyOr("testsFile", "requests.csv")
    private val hostUrl = getStringPropertyOr("hostUrl", "http://0.0.0.0:8080")
    private val usersMode = UsersMode.valueOf(getStringPropertyOr("injection", UsersMode.RAMP.name))
    private val startRate = getDoublePropertyOr("startRate", 1.0)
    private val endRate = getDoublePropertyOr("endRate", 2.0)
    private val feeder = csv(testsFile).circular()

    private val imageRequests =
        scenario(getScenario(usersMode))
            .feed(feeder)
            .exec(
                http("Image requested")
                    .get("#{Path}")
            )

    private val httpProtocol =
        http.baseUrl(hostUrl)

    init {
        when (usersMode) {
            UsersMode.CONSTANT -> setUp(
                imageRequests.injectOpen(constantUsersPerSec(usersPerSecond).during(Duration.ofMinutes(duration)))
                    .protocols(httpProtocol)
            )

            UsersMode.RAMP -> {
                setUp(
                    imageRequests.injectOpen(
                        rampUsersPerSec(startRate).to(endRate).during(Duration.ofMinutes(duration))
                    )
                        .protocols(httpProtocol)
                )
            }

            UsersMode.STRESS -> setUp(
                imageRequests.injectOpen(stressPeakUsers(usersPerSecond.toInt()).during(Duration.ofMinutes(duration)))
                    .protocols(httpProtocol)
            )
        }

    }

    private fun getScenario(usersMode: UsersMode): String {
        return when (usersMode) {
            UsersMode.CONSTANT -> "Image requests Constant $usersPerSecond Users Per Second over $duration minutes"
            UsersMode.RAMP -> "Image requests Ramp from $startRate Users Per Second to $endRate Users Per Second over $duration minutes"
            UsersMode.STRESS -> "Image requests Stress Peak $usersPerSecond Users Per Second over $duration minutes"
        }
    }
}

enum class UsersMode {
    CONSTANT, RAMP, STRESS
}
