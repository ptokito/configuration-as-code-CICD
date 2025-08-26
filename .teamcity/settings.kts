/*
 * CONFIGURATION AS CODE DEMO
 * Last updated: TeamCity Demo - Live from IntelliJ
 *
 * This file IS our TeamCity configuration
 * Changes here = Changes in TeamCity (no UI needed!)
 */
import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildSteps.script
import jetbrains.buildServer.configs.kotlin.triggers.vcs

version = "2023.11"

project {

    buildType {
        id("ConfigAsCodeDemo")
        name = "Configuration as Code Pipeline"
        description = "Everything you see here was defined in code, not UI"

        steps {
            // Step 1: Show config is from code
            script {
                name = "1. Verify Config Source"
                scriptContent = """
                    echo "================================================"
                    echo "This entire pipeline is defined in settings.kts"
                    echo "Pushed from IntelliJ at: $(date)"
                    echo "No TeamCity UI was harmed in making this pipeline!"
                    echo "================================================"
                """.trimIndent()
            }

            // Step 2: Install dependencies
            script {
                name = "2. Install Dependencies"
                scriptContent = """
                    echo "Installing Python dependencies..."
                    pip install -r requirements.txt
                """.trimIndent()
            }

            // Step 3: Run tests
            script {
                name = "3. Run Tests"
                scriptContent = """
                    echo "Running tests..."
                    python -m pytest test_app.py
                """.trimIndent()
            }

            // Step 4: Build application
            script {
                name = "4. Build Application"
                scriptContent = """
                    echo "Building application..."
                    python app.py
                """.trimIndent()
            }

            // Step 5: Deploy to Render
            script {
                name = "5. Deploy to Render.com"
                scriptContent = """
                    echo "================================================"
                    echo "DEPLOYING TO RENDER.COM VIA WEBHOOK"
                    echo "================================================"
                    curl -X POST https://api.render.com/deploy/srv-ctch8o8gph6c73aj1f90/key/4vJGwu7sqxI
                    echo "Deployment triggered via TeamCity pipeline!"
                    echo "Application will be available at: https://configuration-as-code-cicd.onrender.com"
                    echo "================================================"
                """.trimIndent()
            }

            // DEMO MOMENT: Uncomment during live demo to show instant changes
            // script {
            //     name = "6. Security Scan - Added Live!"
            //     scriptContent = """
            //         echo "================================================"
            //         echo "THIS STEP WAS ADDED BY EDITING settings.kts"
            //         echo "No UI clicking needed!"
            //         echo "Timestamp: $(date)"
            //         echo "================================================"
            //     """.trimIndent()
            // }
        }

        // Triggers - also configuration as code!
        triggers {
            vcs {
                branchFilter = "+:main"
            }
        }

        // Parameters - environment variables as code
        params {
            param("env.DEPLOY_ENV", "production")
            param("env.PYTHON_VERSION", "3.9")
        }
    }

    // DEMO: Second build configuration (uncomment to show multiple pipelines)
    // buildType {
    //     id("StagingPipeline")
    //     name = "Staging Environment Pipeline"
    //     description = "Created by copying and modifying code - 30 seconds vs 30 minutes!"
    //
    //     steps {
    //         script {
    //             name = "Deploy to Staging"
    //             scriptContent = """
    //                 echo "This entire staging pipeline was created by copying code"
    //                 echo "Time to create: 30 seconds (vs 30 minutes of UI clicking)"
    //             """.trimIndent()
    //         }
    //     }
    // }
}