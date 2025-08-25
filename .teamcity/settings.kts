/*
 * CONFIGURATION AS CODE DEMO
 * Last updated: [Current time] - Live demo from IntelliJ
 */

import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildSteps.script

version = "2023.11"

project {
    buildType {
        name = "Configuration as Code Pipeline"

        steps {
            script {
                scriptContent = """
                    echo "This entire pipeline is defined in settings.kts"
                    echo "Pushed from IntelliJ at: $(date)"
                """.trimIndent()
            }
        }
    }
}