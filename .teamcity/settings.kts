import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildSteps.script
import jetbrains.buildServer.configs.kotlin.buildSteps.python
import jetbrains.buildServer.configs.kotlin.triggers.vcs
import jetbrains.buildServer.configs.kotlin.vcs.GitVcsRoot

version = "2023.11"

project {
    description = "Configuration as Code demonstration with TeamCity Kotlin DSL"

    vcsRoot(GitHubRepo)
    buildType(CICDPipeline)
}

object GitHubRepo : GitVcsRoot({
    name = "GitHub Repository"
    url = "https://github.com/ptokito/configuration-as-code-CICD"
    branch = "refs/heads/main"
    authMethod = anonymous()
})

object CICDPipeline : BuildType({
    name = "Configuration as Code Pipeline"
    description = "Everything defined in Kotlin DSL - no UI configuration needed"

    vcs {
        root(GitHubRepo)
    }

    steps {
        script {
            name = "1. Verify Config Source"
            scriptContent = """
                echo "================================================"
                echo "🎯 CONFIGURATION AS CODE - TEAMCITY KOTLIN DSL"
                echo "================================================"
                echo "This pipeline is defined in .teamcity/settings.kts"
                echo "Pushed from IntelliJ at: ${'$'}(date)"
                echo "No TeamCity UI configuration needed!"
                echo "================================================"
            """.trimIndent()
        }

        python {
            name = "2. Setup Python Environment"
            command = script {
                content = """
                    import sys
                    print(f"Python {sys.version}")
                    print("Environment ready for testing")
                """.trimIndent()
            }
        }

        script {
            name = "3. Install Dependencies"
            scriptContent = """
                echo "Installing Python dependencies..."
                pip install -r requirements.txt
            """.trimIndent()
        }

        script {
            name = "4. Run Tests"
            scriptContent = """
                echo "Running tests..."
                python -m pytest test_app.py -v
            """.trimIndent()
        }

        script {
            name = "5. Build Application"
            scriptContent = """
                echo "Building application..."
                python -c "import app; print('✅ App module loaded successfully')"
            """.trimIndent()
        }

        script {
            name = "6. Deploy to Render"
            scriptContent = """
                echo "================================================"
                echo "🚀 DEPLOYING TO RENDER.COM"
                echo "================================================"
                curl -X POST https://api.render.com/deploy/srv-d2ltti7diees73cbo5h0?key=K7rSQMB44Ps
                echo "✅ Deployment triggered via TeamCity!"
                echo "================================================"
            """.trimIndent()
        }

        // DEMO: Uncomment during live presentation
        /*
        script {
            name = "7. Security Scan - ADDED LIVE!"
            scriptContent = """
                echo "================================================"
                echo "⚡ THIS STEP WAS ADDED BY EDITING settings.kts"
                echo "No clicking through TeamCity UI!"
                echo "Just uncommented in Kotlin DSL and pushed!"
                echo "Timestamp: ${'$'}(date)"
                echo "================================================"
            """.trimIndent()
        }
        */
    }

    triggers {
        vcs {
            branchFilter = "+:main"
            enableQueueOptimization = true
        }
    }

    params {
        param("env.DEPLOY_ENV", "production")
        param("env.PYTHON_VERSION", "3.9")
    }

    requirements {
        contains("teamcity.agent.os.name", "Linux")
    }

    failureConditions {
        executionTimeoutMin = 10
        testFailure = true
    }
})

// DEMO: Show how easy to create multiple environments
/*
object StagingPipeline : BuildType({
    name = "Staging Environment Pipeline"
    description = "Created by copying Kotlin DSL - 30 seconds vs 30 minutes of clicking!"

    vcs {
        root(GitHubRepo)
    }

    steps {
        script {
            name = "Deploy to Staging"
            scriptContent = """
                echo "This staging pipeline was created by copying Kotlin code"
                echo "Time to create: 30 seconds vs 30 minutes in UI"
            """.trimIndent()
        }
    }
})
*/
/* gooooo */