#!/usr/bin/env groovy

pipeline{
    parameters {
        string(name: 'NODES_AMOUNT', defaultValue: '1', description: 'Number of nodes, which will run in parallel load tests', trim: true)
        string(name: 'NODE_TYPE', defaultValue: 'slave_name', description: 'Type of node to run the test', trim: true)
        string(name: 'USERS_PER_SECOND', defaultValue: '10', description: 'Number of users per second', trim: true)
        string(name: 'DURATION', defaultValue: '5', description: 'Duration of the test in minutes', trim: true)
        string(name: 'SIMULATION_CLASS', defaultValue: 'imagerequests.ImageRequestsSimulation', description: 'Name of the simulation class', trim: true)
        choice(name: 'INJECTION', choices: ['RAMP', 'CONSTANT','STRESS'], description: 'Type of injection')
        string(name: 'RAMP_RATE_1', defaultValue: '15', trim: true)
        string(name: 'RAMP_RATE_2', defaultValue: '90', trim: true)
        string(name: 'HOST_URL', defaultValue: 'http://localhost:8080' , description: 'URL of the host', trim: true)
        choice(name: 'TEST_FILE', choices: [ 'requests.csv'], description: 'Name of the test file')
    }

    environment {
        DOCKER_LOG_FILE = "dockerCompose.log"
    }

    agent {
        label "${env.NODE_TYPE}"
    }

    stages{
        stage('Init') {
            steps {
                sh "rm -rf target/gatling/*"
            }
        }

        stage('Run Parallel') {
            steps {
                sh "./start.sh"
                runInParallel()
            }

            post {
                cleanup {
                    sh "./stop.sh"
                }
                always {
                    archiveArtifacts("dockerCompose.log")
                }
            }
        }


        stage('Gather Results') {
            environment {
                BUILD_REPORTS="service-performance-tests/build/reports/gatling"
                GATLING_RESULTS="gatling-charts/results"
            }

            steps {
                script {
                    for(n = 0; n < env.NODES_AMOUNT.toInteger(); n += 1) {
                        def node_no = n
                        unstash "logs-test${node_no}"
                    }
                }

                sh "./gatling_charts.sh"
            }

            always {
                archiveArtifacts("gatling-charts/results")
            }
        }
    }
}


def runLoadTest(n) {
    sh """
        echo "Starting Test"
        (
            cd service-performance-tests
            ./gradlew gatlingRun-$SIMULATION_CLASS \
            -Dgatling.charting.noReports=true \
            -DusersPerSecond=$USERS_PER_SECOND \
            -Dduration="$DURATION" \
            -DhostUrl="$HOST_URL" \
            -DtestsFile="$TEST_FILE" \
            -Dinjection="$INJECTION" \
            -DstartRate="$RAMP_RATE_1" \
            -DendRate="$RAMP_RATE_2"
        )
    """
    stash includes: 'service-performance-tests/build/reports/gatling/**/simulation.log', name: "logs-test${n}"
}

def runInParallel() {
    script {
        def barrier = [:]
        def branches = [:]
        def nodesAmount = env.NODES_AMOUNT.toInteger()

        for(n = 0; n < nodesAmount; n += 1) {
            def node_no = n
            branches["node-${node_no}"] = {
                //node("${env.NODE_TYPE}") {
                    stage("Node-${node_no}: Preparing") {
                        // checkout scm
                        // sh "./gradlew clean"

                        lock("barrier") {
                            barrier["await${node_no}"] = true
                            echo "Added new barrier result, current size is: ${barrier.size()}"
                        }
                    }

                    stage("Node-${node_no}: Waiting...") {
                        waitUntil(initialRecurrencePeriod: 3000) {
                            lock("barrier") {
                                echo "Waiting for all nodes to be ready..."
                                echo "Barrier size: ${barrier.size()}"
                                return barrier.size() == nodesAmount
                            }
                        }
                    }

                    stage("Node-${node_no}: Running Test") {
                        runLoadTest(node_no)
                    }
            //    }
            }
        }

        parallel branches
    }
}
