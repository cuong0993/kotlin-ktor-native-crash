Repository contains simple Ktor server (Kotlin Native) source code together with Gatling tests that are containeraized via Docker compose and deployed with Jenkins. 
Environment created that way allows to reproduce the issue: https://youtrack.jetbrains.com/issue/KTOR-5822/free-invalid-pointer-crash-during-a-high-load

podman build -f Dockerfile.sample .
podman run --memory=1G -p 8080:8080 <image-ID>
cd service-performance-tests
./gradlew gatlingRun --simulation=imagerequests.ImageRequestsSimulation -Dduration=5 -Dinjection=RAMP -DstartRate=15 -DendRate=90