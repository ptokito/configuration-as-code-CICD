import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildFeatures.perfmon
import jetbrains.buildServer.configs.kotlin.buildSteps.python
import jetbrains.buildServer.configs.kotlin.buildSteps.script
import jetbrains.buildServer.configs.kotlin.triggers.vcs
import jetbrains.buildServer.configs.kotlin.vcs.GitVcsRoot

/*
TeamCity Configuration as Code Demo
Author: Tim Okito
Description: CI/CD pipeline for Hello World Python application
Benefits demonstrated:
- Version controlled configuration
- Code review process for CI/CD changes
- Reusable templates
- Environment-specific configurations
*/

version = "2023.11"

project {
    description = "Hello World Python Application - Configuration as Code Demo"

    // VCS Root Configuration
    vcsRoot(GitVcsRootConfig)

    // Build Configurations
    buildType(BuildAndTest)
    buildType(Deploy)

    // Build Chain
    sequential {
        buildType(BuildAndTest)
        buildType(Deploy)
    }

    // Parameters that can be overridden
    params {
        param("env.PYTHON_VERSION", "3.9")
        param("env.PROJECT_NAME", "hello-world-cac-demo")
        param("env.AUTHOR", "Tim Okito")
        param("env.DEMO_PRESENTER", "Tim Okito - Live Demo")
    }

    // Templates for reusability
    template(PythonBuildTemplate)
}

// VCS Root Configuration
object GitVcsRootConfig : GitVcsRoot({
    name = "Hello World Repository"
    url = "https://github.com/your-org/hello-world-python.git"
    branch = "refs/heads/main"
    branchSpec = """
        +:refs/heads/*
        +:refs/pull/*/merge
    """.trimIndent()
    authMethod = uploadedKey {
        uploadedKey = "teamcity_ssh_key"
    }
})

// Reusable Build Template
object PythonBuildTemplate : Template({
    name = "Python Build Template"

    artifactRules = """
        *.py => python-app.zip
        requirements.txt => python-app.zip
        test-reports/** => test-reports.zip
    """.trimIndent()

    params {
        param("python.test.coverage.threshold", "80")
    }

    vcs {
        root(GitVcsRootConfig)
        cleanCheckout = true
    }

    steps {
        script {
            name = "Setup Python Environment"
            scriptContent = """
                #!/bin/bash
                echo "🔧 Setting up Python environment..."
                python3 -m venv venv
                source venv/bin/activate
                pip install --upgrade pip
                pip install -r requirements.txt
                echo "✅ Environment ready!"
            """.trimIndent()
        }
    }
})

// Build and Test Configuration
object BuildAndTest : BuildType({
    templates(PythonBuildTemplate)
    name = "Build and Test"
    description = "Build application and run tests"

    params {
        param("env.BUILD_CONFIGURATION", "test")
    }

    steps {
        python {
            name = "Code Quality Check"
            command = module {
                module = "flake8"
                arguments = ". --count --select=E9,F63,F7,F82 --show-source --statistics"
            }
        }

        python {
            name = "Run Unit Tests"
            command = module {
                module = "pytest"
                arguments = "-v --cov=. --cov-report=xml --cov-report=html --junitxml=test-reports/junit.xml"
            }
        }

        python {
            name = "Run Application Test Mode"
            command = file {
                filename = "hello_world.py"
                arguments = "--test"
            }
        }

        script {
            name = "Generate Build Info"
            scriptContent = """
                #!/bin/bash
                echo "📊 Build Information:"
                echo "  Build Number: %build.number%"
                echo "  VCS Revision: %build.vcs.number%"
                echo "  Agent: %teamcity.agent.name%"
                echo "  Timestamp: %system.build.start.date%"
                
                # Set environment variables for the app
                export BUILD_NUMBER=%build.number%
                export GIT_COMMIT=%build.vcs.number%
                export TEAMCITY_VERSION=%teamcity.version%
                
                # Run the application
                python3 hello_world.py
            """.trimIndent()
        }
    }

    triggers {
        vcs {
            branchFilter = """
                +:*
                -:release/*
            """.trimIndent()
            perCheckinTriggering = true
            groupCheckinsByCommitter = true
            enableQueueOptimization = true
        }
    }

    features {
        perfmon {
        }
        feature {
            type = "xml-report-plugin"
            param("xmlReportParsing.reportType", "junit")
            param("xmlReportParsing.reportDirs", "test-reports/junit.xml")
        }
    }

    requirements {
        contains("teamcity.agent.os.name", "Linux")
        exists("python3")
    }
})

// Deploy Configuration
object Deploy : BuildType({
    name = "Deploy to Environment"
    description = "Deploy application to target environment"

    params {
        select("env.DEPLOY_TARGET", "staging", display = ParameterDisplay.PROMPT,
            options = listOf("development", "staging", "production"))
        param("env.DEPLOY_APPROVAL", "false")
    }

    dependencies {
        snapshot(BuildAndTest) {
            onDependencyFailure = FailureAction.FAIL_TO_START
            onDependencyCancel = FailureAction.CANCEL
        }
        artifacts(BuildAndTest) {
            artifactRules = "python-app.zip => ."
        }
    }

    steps {
        script {
            name = "Pre-deployment Checks"
            scriptContent = """
                #!/bin/bash
                echo "🔍 Running pre-deployment checks..."
                echo "  Target Environment: %env.DEPLOY_TARGET%"
                echo "  Build Number: %dep.${BuildAndTest.id}.build.number%"
                
                if [ "%env.DEPLOY_TARGET%" == "production" ]; then
                    if [ "%env.DEPLOY_APPROVAL%" != "true" ]; then
                        echo "❌ Production deployment requires approval!"
                        exit 1
                    fi
                fi
                
                echo "✅ Pre-deployment checks passed!"
            """.trimIndent()
        }

        script {
            name = "Deploy Application"
            scriptContent = """
                #!/bin/bash
                echo "🚀 Deploying to %env.DEPLOY_TARGET%..."
                
                # Extract artifacts
                unzip -o python-app.zip
                
                # Simulate deployment based on environment
                case "%env.DEPLOY_TARGET%" in
                    development)
                        echo "📦 Deploying to development server..."
                        # rsync -avz *.py dev-server:/opt/app/
                        ;;
                    staging)
                        echo "📦 Deploying to staging server..."
                        # rsync -avz *.py staging-server:/opt/app/
                        ;;
                    production)
                        echo "📦 Deploying to production server..."
                        # rsync -avz *.py prod-server:/opt/app/
                        ;;
                esac
                
                echo "✅ Deployment completed successfully!"
                
                # Run smoke test
                python3 hello_world.py
            """.trimIndent()
        }

        script {
            name = "Post-deployment Notification"
            scriptContent = """
                #!/bin/bash
                echo "📧 Sending deployment notification..."
                echo "Deployment Details:"
                echo "  Application: %env.PROJECT_NAME%"
                echo "  Environment: %env.DEPLOY_TARGET%"
                echo "  Version: %dep.${BuildAndTest.id}.build.number%"
                echo "  Deployed by: %system.teamcity.auth.userId%"
                echo "  Timestamp: %system.build.start.date%"
            """.trimIndent()
        }
    }

    features {
        feature {
            type = "BUILD_APPROVAL"
            param("approvalRules", "user:lead-developer")
        }
    }
})