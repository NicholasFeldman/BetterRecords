pipeline {
    agent any

    environment {
        CI = 'true'

        DISCORD_TOKEN = credentials('WEBHOOK_TOKEN')
    }

    stages {
        stage('Setup') {
            steps {
                sh './gradlew clean setupCiWorkspace'
            }
        }

        stage('Test') {
            steps {
                sh './gradlew test'
            }
        }

        stage('Build') {
            steps {
                sh './gradlew build'
            }
        }

        stage('Deploy') {
            steps {
                archiveArtifacts 'build/libs/*.jar'
            }
        }
    }

    post {
        success {
            script {
                if (env.GIT_BRANCH == 'dev') {
                    discordSend webhookURL: "https://discordapp.com/api/webhooks/456986000447766534/${DISCORD_TOKEN}",
                                title: 'A New Development Build is Available!',
                                link: env.BUILD_URL,
                                description: "${env.BUILD_TAG}-${env.GIT_COMMIT}\n\nPlease report any issues to https://github.com/NicholasFeldman/BetterRecords/issues",
                                footer: 'Thank you for testing!'
                } else if (env.GIT_BRANCH.startsWith('release')) {
                    discordSend webhookURL: "https://discordapp.com/api/webhooks/456986000447766534/${DISCORD_TOKEN}",
                                title: 'A New Release is Being Prepared!',
                                link: env.BUILD_URL,
                                description: "${env.BUILD_TAG}-${env.GIT_COMMIT}\n\nPlease report any issues to https://github.com/NicholasFeldman/BetterRecords/issues",
                                footer: 'Thank you for testing!',
                                successful: true
                } else if (env.CHANGE_ID) {
                    discordSend webhookURL: "https://discordapp.com/api/webhooks/456986000447766534/${DISCORD_TOKEN}",
                                title: 'A New Feature is available for Testing!',
                                link: env.BUILD_URL,
                                description: "${env.BUILD_TAG}-${env.GIT_COMMIT}\n\n[${CHANGE_TITLE}](${CHANGE_URL})\n\nPlease report any issues to https://github.com/NicholasFeldman/BetterRecords/issues",
                                footer: 'Thank you for testing!',
                                successful: true
                }
            }
        }
    }
}
