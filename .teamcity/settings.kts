/*
 * CONFIGURATION AS CODE DEMO
 * Last updated: [Current time] - Live demo from IntelliJ
 *
 * This file IS our TeamCity configuration
 * Changes here = Changes in TeamCity (no UI needed!)
 */
import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildSteps.python
import jetbrains.buildServer.configs.kotlin.buildSteps.script
import jetbrains.buildServer.configs.kotlin.triggers.vcs
import jetbrains.buildServer.configs.kotlin.vcs.GitVcsRoot
import jetbrains.buildServer.configs.kotlin.buildFeatures.perfmon

version = "2023.11"

project {

    // VERSION CONTROL SETTINGS - Also as code!
    vcsRoot(GitVcsRoot {
        name = "Project Repository"
        url = "https://github.com/yourusername/your-repo.git"
        branch = "refs/heads/main"
        authMethod = anonymous()  // or password/token auth
    })

    // BUILD CONFIGURATION
    buildType {
        id("ConfigAsCodeDemo")
        name = "Configuration as Code Pipeline"
        description = "Everything you see here was defined in code, not UI"

        // VCS Settings
        vcs {
            root(GitVcsRoot)
        }

        // BUILD STEPS - The heart of your demo
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
            python {
                name = "3. Run Tests"
                command = script {
                    content = """
                        import test_app
                        print("Running tests defined in config-as-code...")
                        # pytest would run here
                    """.trimIndent()
                }
            }

            // Step 4: Build application
            script {
                name = "4. Build Application"
                scriptContent = """
                    echo "Building application..."
                    python app.py
                """.trimIndent()
            }

            // Step 5: Deploy to Render (if tests pass)
            script {
                name = "5. Deploy to Render.com"
                scriptContent = """
                    echo "Deploying to Render.com..."
                    # Your Render webhook or deploy command here
                    curl -X POST https://api.render.com/deploy/your-service-id
                """.trimIndent()
            }

//             DEMO MOMENT: Uncomment during live demo to show instant changes
//             script {
//                name = "6. NEW STEP - Added Live!"
//                 scriptContent = """
//                     echo "================================================"
//                     echo "THIS STEP WAS ADDED BY EDITING settings.kts"
//                     echo "No UI clicking needed!"
//                     echo "Timestamp: $(date)"
//                     echo "================================================"
//                 """.trimIndent()
//             }
        }

        // TRIGGERS - Also configuration as code!
        triggers {
            vcs {
                branchFilter = "+:main"
                enableQueueOptimization = true
            }
        }

        // PARAMETERS - Environment variables as code
        params {
            param("env.DEPLOY_ENV", "production")
            param("env.PYTHON_VERSION", "3.9")
            password("env.RENDER_API_KEY", "credentialsJSON:render-key")
        }

        // BUILD FEATURES
        features {
            feature {
                type = "perfmon"  // Performance monitoring
            }
        }

        // FAILURE CONDITIONS - Quality gates as code
        failureConditions {
            executionTimeoutMin = 10
            testFailure = true
            errorMessage = true
        }

        // REQUIREMENTS - Agent requirements as code
        requirements {
            contains("teamcity.agent.os.name", "Linux")
        }
    }

    // DEMO: Second build configuration (uncomment to show multiple pipelines)
    /*
    buildType {
        id("StagingPipeline")
        name = "Staging Environment Pipeline"
        description = "Created by copying and modifying code - 30 seconds vs 30 minutes!"

        steps {
            script {
                name = "Deploy to Staging"
                scriptContent = """
                    echo "This entire staging pipeline was created by copying code"
                    echo "Time to create: 30 seconds (vs 30 minutes of UI clicking)"
                """.trimIndent()
            }
        }
    }
    */
}