pipeline {
    agent {
        docker { image 'maven:3.6.3-openjdk-11' }
    }
    tools {
        maven 'maven-3-6-3'
    }
    environment {
        IMAGE_VERSION = "dev"
        DOCKER_IMAGE_NAME = "xcal.main-service"
        //DOCKER_IMAGE_NAME_REMOTE = "hub.xcalibyte.co/xcalscan/xcal.main-service"
        DOCKER_IMAGE_NAME_REMOTE  = "xcal.web-api-service-main" 
    }
    stages {
        stage('pull') {
            steps {
                echo '========= pull =========='
                git credentialsId: 'xxx', branch: 'dev', url: 'https://github.com/xcalcc/web-api-service.git'
            }
        }
        stage('build') {
            steps {
                echo '======= build ======='
                lock('web_api_service_Build_Lock') {
                    sh 'mvn clean package docker:build '
                }
            }
        }
        stage('JaCoCo') {
            steps {
                echo '======= Generate JaCoCo Report ======='
                jacoco(
                        changeBuildStatus: true,
                        execPattern: 'target/*.exec',
                        classPattern: 'target/classes',
                        sourcePattern: 'src/main/java',
                        exclusionPattern: '**/repository/*.class,**/entity/*_.class,**/WebAPIServiceMainApplication.class',
                        minimumBranchCoverage: '70',
                        maximumBranchCoverage: '80',
                        minimumClassCoverage: '70',
                        maximumClassCoverage: '80',
                        minimumComplexityCoverage: '65',
                        maximumComplexityCoverage: '80',
                        minimumInstructionCoverage: '80',
                        maximumInstructionCoverage: '85',
                        minimumLineCoverage: '80',
                        maximumLineCoverage: '85',
                        minimumMethodCoverage: '80',
                        maximumMethodCoverage: '85'
                )
            }
        }
        stage('deploy') {
            steps {
                echo '======= deploy ======='
                lock('web_api_service_Build_Lock') {
                    script {
                        tagName = getTagName()
                        containerID = getContainerID()
                        echo "${containerID}"
                        oldImageID = getImagesID()
                        echo "${oldImageID}"
                        newImageID = getNewImagesID()
                        echo "${newImageID}"
                        withCredentials([usernamePassword(credentialsId: 'xxx', passwordVariable: 'password', usernameVariable: 'user')]) {
                            sh """
                      echo $tagName
                      set +e
                      docker tag $DOCKER_IMAGE_NAME_REMOTE:$tagName $DOCKER_IMAGE_NAME:$IMAGE_VERSION
                      docker rmi $DOCKER_IMAGE_NAME_REMOTE:$tagName
                      docker stop $containerID
                      docker rm $containerID
                      if [ $newImageID != $oldImageID ]; then
                        docker rmi $oldImageID
                      fi       
                      set -e
                      """
                        }
                        // sshPublisher(
                        //         continueOnError: false, failOnError: true,
                        //         publishers: [
                        //                 sshPublisherDesc(
                        //                         configName: "aliyun_jenkins_host",
                        //                         verbose: true,
                        //                         transfers: [
                        //                                 sshTransfer(
                        //                                         //execCommand: "cd /home/xc5/dev_deploy/web-api-service && docker-compose down && docker-compose up -d "
                        //                                 )
                        //                         ])
                        //         ])
                    }
                }
            }
        }
    }
}

// def getLatestImage() { // return like:  xcal.web-api-service-main:20190730-142013.9ee1222
//     return sh(returnStdout: true,
//             script: "docker images | grep $DOCKER_IMAGE_NAME_REMOTE | awk '{print \$1 \":\"  \$2}' | sed 2,\$\$d").trim()
// }

def getTagName() {
    return sh(returnStdout: true, script: "ls -l target/docker/xcal.web-api-service-main/ | awk '{print  \$9}'").trim()
}

def getContainerID() {
    return sh(returnStdout: true, script: "docker ps | grep $DOCKER_IMAGE_NAME:$IMAGE_VERSION | awk '{print  \$1}'").trim()
}

def getImagesID() {
    return sh(returnStdout: true, script: "docker images | grep ^$DOCKER_IMAGE_NAME | grep $IMAGE_VERSION | awk '{print  \$3}'").trim()
}

def getNewImagesID() {
    return sh(returnStdout: true, script: "docker images | grep ^$DOCKER_IMAGE_NAME_REMOTE | awk '{print  \$3}'").trim()
}
