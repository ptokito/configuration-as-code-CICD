import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildFeatures.perfmon
import jetbrains.buildServer.configs.kotlin.buildFeatures.notifications
import jetbrains.buildServer.configs.kotlin.buildSteps.python
import jetbrains.buildServer.configs.kotlin.buildSteps.script
import jetbrains.buildServer.configs.kotlin.triggers.vcs
import jetbrains.buildServer.configs.kotlin.vcs.GitVcsRoot

/*
TeamCity Configuration as Code Demo
Author: Tim Okito
Description: CI/CD pipeline for Hello World Python application
Last Updated: Live demo update - showing real-time pipeline changes!

Benefits demonstrated:
- Version controlled configuration
- Code review process for CI/CD changes
- Reusable templates
- Environment-specific configurations
- Real-time updates without UI access
- Developer-friendly IDE integration
*/

version = "2023.11"

project {
    description = "Hello World Python Application - Configuration as Code Demo by Tim Okito"

    // VCS Root Configuration
    vcsRoot(GitVcsRootConfig)

    // Build Configurations
    buildType(BuildAndTest)
    buildType(Deploy)
    buildType(LiveDemoShowcase)  // NEW: Added during live demo!

    // Build Chain - Sequential pipeline
    sequential {
        buildType(BuildAndTest)
        buildType(Deploy)
        parallel {
            buildType(LiveDemoShowcase)
        }
    }

    // Global Parameters - can be overridden per build
    params {
        param("env.PYTHON_VERSION", "3.9")
        param("env.PROJECT_NAME", "hello-world-cac-demo")
        param("env.AUTHOR", "Tim Okito")
        param("env.DEMO_TIMESTAMP", "${Date()}")  // NEW: Dynamic timestamp
        param("env.DEMO_MODE", "LIVE")  // NEW: Demo indicator
    }

    // Templates for reusability
    template(PythonBuildTemplate)

    // NEW: Build Features added during demo
    features {
        feature {
            type = "project-graphs"
            param("series", "build-duration")
            param("format", "duration")
        }
    }
}

// VCS Root Configuration - Now pointing to GitHub!
object GitVcsRootConfig : GitVcsRoot({
    name = "Hello World Repository - GitHub"
    url = "https://github.com/ptokito/cac-demo.git"  // UPDATED: Your actual repo!
    branch = "refs/heads/main"
    branchSpec = """
        +:refs/heads/*
        +:refs/pull/*/merge
    """.trimIndent()
    authMethod = password {  // For demo purposes, using HTTPS
        userName = "ptokito"
        password = "credentialsJSON:github-token"
    }
})

// Reusable Build Template
object PythonBuildTemplate : Template({
    name = "Python Build Template"

    artifactRules = """
        *.py => python-app.zip
        requirements.txt => python-app.zip
        test-reports/** => test-reports.zip
        demo-output.txt => demo-artifacts.zip
    """.trimIndent()

    params {
        param("python.test.coverage.threshold", "80")
        param("demo.live.update", "true")  // NEW: Demo parameter
    }

    vcs {
        root(GitVcsRootConfig)
        cleanCheckout = true
    }

    steps {
        script {
            name = "🔧 Setup Python Environment"
            scriptContent = """
                #!/bin/bash
                echo "╔════════════════════════════════════════════╗"
                echo "║   Configuration as Code Demo - Tim Okito   ║"
                echo "╚════════════════════════════════════════════╝"
                echo ""
                echo "🔧 Setting up Python environment..."
                python3 -m venv venv
                source venv/bin/activate
                pip install --upgrade pip
                pip install -r requirements.txt
                echo "✅ Environment ready!"
                echo "📍 All configuration defined in IntelliJ - no UI needed!"
            """.trimIndent()
        }
    }
})

// Build and Test Configuration - Enhanced for demo
object BuildAndTest : BuildType({
    templates(PythonBuildTemplate)
    name = "🏗️ Build and Test"
    description = "Build application and run comprehensive tests"

    params {
        param("env.BUILD_CONFIGURATION", "test")
        param("env.SHOW_DEMO_BANNER", "true")  // NEW
    }

    steps {
        // NEW: Demo banner step
        script {
            name = "🎯 Demo Banner"
            scriptContent = """
                #!/bin/bash
                echo "┌─────────────────────────────────────────────────────┐"
                echo "│  LIVE DEMO: Configuration as Code with TeamCity     │"
                echo "│  Developer: Tim Okito                               │"
                echo "│  IDE: IntelliJ IDEA - Single Source of Truth       │"
                echo "│  Status: No UI needed, everything in code!         │"
                echo "└─────────────────────────────────────────────────────┘"
            """.trimIndent()
        }

        python {
            name = "🔍 Code Quality Check"
            command = module {
                module = "flake8"
                arguments = ". --count --select=E9,F63,F7,F82 --show-source --statistics"
            }
        }

        python {
            name = "🧪 Run Unit Tests"
            command = module {
                module = "pytest"
                arguments = "-v --cov=. --cov-report=xml --cov-report=html --junitxml=test-reports/junit.xml"
            }
        }

        python {
            name = "🚀 Run Application Test Mode"
            command = file {
                filename = "hello_world.py"
                arguments = "--test"
            }
        }

        script {
            name = "📊 Generate Build Info & Demo Metrics"
            scriptContent = """
                #!/bin/bash
                echo "📊 Build Information:"
                echo "  Build Number: %build.number%"
                echo "  VCS Revision: %build.vcs.number%"
                echo "  Agent: %teamcity.agent.name%"
                echo "  Timestamp: %system.build.start.date%"
                echo ""
                echo "💡 DEMO INSIGHT: This entire pipeline was defined in IntelliJ!"
                echo "   No TeamCity UI access required!"
                echo ""
                
                # Set environment variables for the app
                export BUILD_NUMBER=%build.number%
                export GIT_COMMIT=%build.vcs.number%
                export TEAMCITY_VERSION=%teamcity.version%
                
                # Run the application
                python3 hello_world.py
                
                # NEW: Create demo artifact
                echo "Pipeline configured by Tim Okito at %system.build.start.date%" > demo-output.txt
                echo "All changes tracked in Git - no UI modifications!" >> demo-output.txt
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
            // NEW: Trigger on any change to .teamcity folder
            triggerRules = """
                +:.teamcity/**
                +:*.py
            """.trimIndent()
        }
    }

    features {
        perfmon {
        }

        // NEW: Build status notification
        notifications {
            notifierSettings = slackNotifier {
                connection = "PROJECT_EXT_Slack"
                sendTo = "#ci-cd-demo"
                messageFormat = verboseMessageFormat {
                    addBranch = true
                    addStatusText = true
                    maximumNumberOfChanges = 10
                }
            }
            buildStarted = true
            buildFinishedSuccessfully = true
            buildFailed = true
        }

        feature {
            type = "xml-report-plugin"
            param("xmlReportParsing.reportType", "junit")
            param("xmlReportParsing.reportDirs", "test-reports/junit.xml")
        }

        // NEW: Build failure conditions
        feature {
            type = "BuildFailureOnMetric"
            param("anchorBuild", "lastSuccessful")
            param("metricKey", "buildDuration")
            param("metricThreshold", "300")
            param("metricType", "buildDurationSecs")
            param("moreOrLess", "more")
            param("stopBuildOnFailure", "false")
        }
    }

    requirements {
        contains("teamcity.agent.os.name", "Linux")
        exists("python3")
    }
})

// Deploy Configuration - Enhanced with live demo features
object Deploy : BuildType({
    name = "🚀 Deploy to Environment"
    description = "Deploy application to target environment with approval gates"

    params {
        select("env.DEPLOY_TARGET", "staging", display = ParameterDisplay.PROMPT,
            options = listOf("development", "staging", "production"))
        param("env.DEPLOY_APPROVAL", "false")
        param("env.NOTIFY_SLACK", "true")  // NEW
        param("env.DEMO_DEPLOYMENT", "true")  // NEW
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
            name = "🔍 Pre-deployment Checks"
            scriptContent = """
                #!/bin/bash
                echo "╔══════════════════════════════════════════════════════╗"
                echo "║         DEPLOYMENT via Configuration as Code         ║"
                echo "║              Configured in IntelliJ IDE              ║"
                echo "╚══════════════════════════════════════════════════════╝"
                echo ""
                echo "🔍 Running pre-deployment checks..."
                echo "  Target Environment: %env.DEPLOY_TARGET%"
                echo "  Build Number: %dep.${BuildAndTest.id}.build.number%"
                echo "  Configured by: Tim Okito"
                echo "  Configuration Source: .teamcity/settings.kts"
                
                if [ "%env.DEPLOY_TARGET%" == "production" ]; then
                    if [ "%env.DEPLOY_APPROVAL%" != "true" ]; then
                        echo "❌ Production deployment requires approval!"
                        echo "   This check was defined in CODE, not UI!"
                        exit 1
                    fi
                fi
                
                echo "✅ Pre-deployment checks passed!"
            """.trimIndent()
        }

        script {
            name = "🚀 Deploy Application"
            scriptContent = """
                #!/bin/bash
                echo "🚀 Deploying to %env.DEPLOY_TARGET%..."
                echo "   Deployment pipeline defined entirely in IntelliJ!"
                
                # Extract artifacts
                unzip -o python-app.zip
                
                # Simulate deployment based on environment
                case "%env.DEPLOY_TARGET%" in
                    development)
                        echo "📦 Deploying to development server..."
                        echo "   Config: development.yaml (managed in code)"
                        ;;
                    staging)
                        echo "📦 Deploying to staging server..."
                        echo "   Config: staging.yaml (managed in code)"
                        ;;
                    production)
                        echo "📦 Deploying to production server..."
                        echo "   Config: production.yaml (managed in code)"
                        echo "   🔒 Production safeguards active (defined in code)"
                        ;;
                esac
                
                echo "✅ Deployment completed successfully!"
                echo ""
                echo "📝 Deployment tracked in Git history - full audit trail!"
                
                # Run smoke test
                python3 hello_world.py
            """.trimIndent()
        }

        // NEW: Post-deployment notification step
        script {
            name = "📧 Post-deployment Actions"
            scriptContent = """
                #!/bin/bash
                echo "═══════════════════════════════════════════════════════"
                echo "📧 Sending deployment notification..."
                echo "Deployment Summary:"
                echo "  Application: %env.PROJECT_NAME%"
                echo "  Environment: %env.DEPLOY_TARGET%"
                echo "  Version: %dep.${BuildAndTest.id}.build.number%"
                echo "  Deployed by: %system.teamcity.auth.userId%"
                echo "  Timestamp: %system.build.start.date%"
                echo "  Pipeline Source: GitHub - ptokito/cac-demo"
                echo ""
                echo "🎯 Key Achievement:"
                echo "  This entire deployment pipeline was configured"
                echo "  without ever opening the TeamCity UI!"
                echo "  Everything was done in IntelliJ with full IDE support!"
                echo "═══════════════════════════════════════════════════════"
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

// NEW: Live Demo Showcase Build - Added during presentation!
object LiveDemoShowcase : BuildType({
    name = "✨ Live Demo Showcase"
    description = "Build configuration added LIVE during demo - no UI needed!"

    params {
        param("demo.message", "This build was added during the live demo!")
        param("demo.author", "Tim Okito")
        param("demo.tool", "IntelliJ IDEA")
    }

    vcs {
        root(GitVcsRootConfig)
    }

    steps {
        script {
            name = "🎉 Demo Live Update"
            scriptContent = """
                #!/bin/bash
                echo "╔════════════════════════════════════════════════════════════╗"
                echo "║              🎉 LIVE DEMO UPDATE 🎉                      ║"
                echo "║                                                           ║"
                echo "║  This build configuration was added during the demo!      ║"
                echo "║  No TeamCity UI was touched - everything in IntelliJ!    ║"
                echo "║                                                           ║"
                echo "║  Developer: Tim Okito                                    ║"
                echo "║  Method: Configuration as Code                           ║"
                echo "║  Tool: IntelliJ IDEA with Kotlin DSL                    ║"
                echo "║                                                           ║"
                echo "║  Benefits demonstrated:                                  ║"
                echo "║  ✓ Real-time pipeline updates                           ║"
                echo "║  ✓ No context switching                                 ║"
                echo "║  ✓ Full IDE support (autocomplete, refactoring)        ║"
                echo "║  ✓ Version control for all changes                      ║"
                echo "║  ✓ Single source of truth                               ║"
                echo "╚════════════════════════════════════════════════════════════╝"
                
                echo ""
                echo "Running application to prove the pipeline works..."
                python3 hello_world.py
                
                echo ""
                echo "📌 This change is now in Git history - fully auditable!"
                echo "📌 Commit: %build.vcs.number%"
                echo "📌 Author: Tim Okito"
                echo "📌 Timestamp: %system.build.start.date%"
            """.trimIndent()
        }

        script {
            name = "💡 Configuration as Code Benefits"
            scriptContent = """
                #!/bin/bash
                echo ""
                echo "💡 What just happened?"
                echo "─────────────────────────────────────────"
                echo "1. Pipeline configuration was modified in IntelliJJ"
                echo "2. Changes were committed to Git"
                echo "3. TeamCity automatically detected and applied changes"
                echo "4. New build configuration is now live"
                echo "5. All without leaving the IDE!"
                echo ""
                echo "🚀 This is the power of Configuration as Code!"
            """.trimIndent()
        }
    }

    triggers {
        vcs {
            branchFilter = "+:main"
        }
    }

    features {
        perfmon {
        }
    }
})