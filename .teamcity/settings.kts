import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.script

version = "2019.2"

project {
    buildType(Build)
}

object Build : BuildType({
    name = "Configuration as Code Demo"

    steps {
        script {
            scriptContent = """
                echo "============================================================"
                echo "🎯 CONFIGURATION AS CODE IS WORKING!"
                echo "============================================================"
                echo "This message proves that TeamCity is reading settings.kts"
                echo "Build Number: %build.number%"
                echo "============================================================"
            """.trimIndent()
        }
    }
})