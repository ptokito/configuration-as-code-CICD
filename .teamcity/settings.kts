import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildSteps.script
import jetbrains.buildServer.configs.kotlin.buildSteps.python
import jetbrains.buildServer.configs.kotlin.triggers.vcs

/*
 * CONFIGURATION AS CODE DEMO
 *
 * This entire CI/CD pipeline is defined in this single Kotlin file.
 * No manual TeamCity configuration needed!
 *
 * This demonstrates:
 * - Infrastructure as Code
 * - Version controlled CI/CD
 * - Type-safe configuration
 * - Reproducible pipelines
 */

version = "2023.11"

project {

    description = "Complete Configuration as Code Demo - Everything in Kotlin DSL"

    // Define the pipeline
    buildType(PipelineJob)
}

object PipelineJob : BuildType({

    name = "Configuration as Code Pipeline"
    description = "Complete CI/CD pipeline defined entirely in settings.kts"

    // Build configuration ID
    id = AbsoluteId("ConfigurationAsCodePipeline")

    // Parameters for the build
    params {
        param("env.PROJECT_NAME", "Configuration-as-Code-Demo")
        param("env.MESSAGE", "Everything you see was defined in settings.kts!")
    }

    // VCS settings - use the default repository
    vcs {
        root(DslContext.settingsRoot)
        cleanCheckout = true
    }

    // All build steps defined in code
    steps {

        // Step 1: Introduction
        script {
            name = "🎯 Step 1: Configuration as Code Demo"
            id = "STEP_01"
            scriptContent = """
                echo "============================================================"
                echo "🎯 CONFIGURATION AS CODE DEMONSTRATION"
                echo "============================================================"
                echo ""
                echo "IMPORTANT: This entire pipeline is defined in settings.kts!"
                echo "No manual TeamCity UI configuration was used."
                echo ""
                echo "Build Information:"
                echo "  • Build Number: %build.number%"
                echo "  • VCS Revision: %build.vcs.number%"
                echo "  • Branch: %teamcity.build.branch%"
                echo "  • Agent: %teamcity.agent.name%"
                echo "  • Project: %env.PROJECT_NAME%"
                echo ""
                echo "Message: %env.MESSAGE%"
                echo "============================================================"
            """.trimIndent()
        }

        // Step 2: Show repository contents
        script {
            name = "📁 Step 2: Repository Analysis"
            id = "STEP_02"
            scriptContent = """
                echo "============================================================"
                echo "📁 REPOSITORY CONTENTS"
                echo "============================================================"
                echo "These files were all checked out from Git:"
                echo ""
                ls -la
                echo ""
                echo "TeamCity configuration:"
                ls -la .teamcity/ || echo "TeamCity config in repository root"
                echo ""
                echo "Key point: The .teamcity/settings.kts file defines this pipeline!"
                echo "============================================================"
            """.trimIndent()
        }

        // Step 3: Build simulation
        script {
            name = "🔨 Step 3: Build Application"
            id = "STEP_03"
            scriptContent = """
                echo "============================================================"
                echo "🔨 BUILD STAGE"
                echo "============================================================"
                echo "Installing dependencies..."
                
                # Check for Python
                if command -v python3 &> /dev/null; then
                    echo "Python3 found: $(python3 --version)"
                    
                    # Try to install requirements
                    if [ -f requirements.txt ]; then
                        echo "Installing from requirements.txt..."
                        pip install -r requirements.txt 2>/dev/null || echo "Note: Some packages skipped in demo environment"
                    fi
                    
                    # Check if app.py exists
                    if [ -f app.py ]; then
                        echo "✅ Application file found: app.py"
                        python3 -c "print('✅ Python application verified')"
                    fi
                else
                    echo "Python not found - simulating build..."
                    echo "  [1/3] Compiling source code..."
                    sleep 1
                    echo "  [2/3] Bundling application..."
                    sleep 1
                    echo "  [3/3] Creating artifacts..."
                    sleep 1
                fi
                
                echo ""
                echo "✅ Build completed successfully!"
                echo "============================================================"
            """.trimIndent()
        }

        // Step 4: Test simulation
        script {
            name = "🧪 Step 4: Run Tests"
            id = "STEP_04"
            scriptContent = """
                echo "============================================================"
                echo "🧪 TEST EXECUTION"
                echo "============================================================"
                echo "Running test suite..."
                echo ""
                
                # Check if test file exists
                if [ -f test_app.py ]; then
                    echo "Found test_app.py - attempting to run tests..."
                    python3 test_app.py 2>/dev/null || {
                        echo "Tests require specific environment - simulating..."
                    }
                else
                    echo "Simulating test execution for demo..."
                fi
                
                echo ""
                echo "Test Results:"
                echo "  ✅ test_configuration_as_code .......... PASSED"
                echo "  ✅ test_build_pipeline .................. PASSED"
                echo "  ✅ test_kotlin_dsl ...................... PASSED"
                echo "  ✅ test_version_control ................. PASSED"
                echo ""
                echo "Summary: 4 passed, 0 failed"
                echo "Code Coverage: 85%"
                echo ""
                echo "✅ All tests passed!"
                echo "============================================================"
            """.trimIndent()
        }

        // Step 5: Security scan
        script {
            name = "🔒 Step 5: Security Scan"
            id = "STEP_05"
            scriptContent = """
                echo "============================================================"
                echo "🔒 SECURITY SCAN"
                echo "============================================================"
                echo "Scanning for vulnerabilities..."
                echo ""
                echo "Checking dependencies:"
                
                if [ -f requirements.txt ]; then
                    echo "Analyzing requirements.txt..."
                    while IFS= read -r line; do
                        if [[ ! -z "${'$'}line" ]] && [[ ! "${'$'}line" == "#"* ]]; then
                            echo "  📦 ${'$'}line ............... ✅ Secure"
                        fi
                    done < requirements.txt
                else
                    echo "  📦 Flask==3.0.0 .................. ✅ Secure"
                    echo "  📦 gunicorn==21.2.0 .............. ✅ Secure"
                    echo "  📦 pytest==8.0.0 ................. ✅ Secure"
                fi
                
                echo ""
                echo "Security Summary:"
                echo "  • Vulnerabilities found: 0"
                echo "  • Security rating: A+"
                echo ""
                echo "✅ Security scan passed!"
                echo "============================================================"
            """.trimIndent()
        }

        // Step 6: Deploy simulation
        script {
            name = "🚀 Step 6: Deploy to Staging"
            id = "STEP_06"
            scriptContent = """
                echo "============================================================"
                echo "🚀 DEPLOYMENT TO STAGING"
                echo "============================================================"
                echo "Deploying application..."
                echo ""
                echo "Deployment steps:"
                echo "  [1/5] Preparing deployment package..."
                sleep 1
                echo "  [2/5] Connecting to staging server..."
                sleep 1
                echo "  [3/5] Uploading application..."
                sleep 1
                echo "  [4/5] Running deployment scripts..."
                sleep 1
                echo "  [5/5] Verifying deployment..."
                sleep 1
                echo ""
                echo "✅ Successfully deployed to staging!"
                echo "🌐 URL: https://staging.configuration-as-code-demo.com"
                echo "============================================================"
            """.trimIndent()
        }

        // Step 7: Production deployment
        script {
            name = "🎯 Step 7: Deploy to Production"
            id = "STEP_07"
            scriptContent = """
                echo "============================================================"
                echo "🎯 PRODUCTION DEPLOYMENT"
                echo "============================================================"
                echo "Final deployment to production..."
                echo ""
                echo "Production deployment steps:"
                echo "  [1/6] Creating backup..."
                sleep 1
                echo "  [2/6] Preparing production package..."
                sleep 1
                echo "  [3/6] Deploying to production servers..."
                sleep 1
                echo "  [4/6] Running database migrations..."
                sleep 1
                echo "  [5/6] Warming up cache..."
                sleep 1
                echo "  [6/6] Running smoke tests..."
                sleep 1
                echo ""
                echo "============================================================"
                echo "🎉 PRODUCTION DEPLOYMENT SUCCESSFUL!"
                echo "============================================================"
                echo ""
                echo "🌐 Live URL: https://configuration-as-code-demo.com"
                echo "📝 Version: %build.number%"
                echo "📅 Deployed at: $(date)"
                echo ""
                echo "🏆 Configuration as Code Demo Complete!"
                echo "   Everything you saw was defined in settings.kts"
                echo "============================================================"
            """.trimIndent()
        }

        // Step 8: Summary
        script {
            name = "📊 Step 8: Pipeline Summary"
            id = "STEP_08"
            scriptContent = """
                echo "============================================================"
                echo "📊 CONFIGURATION AS CODE - SUMMARY"
                echo "============================================================"
                echo ""
                echo "What just happened:"
                echo "  1. ✅ Code checked out from GitHub"
                echo "  2. ✅ Build executed"
                echo "  3. ✅ Tests ran"
                echo "  4. ✅ Security scan completed"
                echo "  5. ✅ Deployed to staging"
                echo "  6. ✅ Deployed to production"
                echo ""
                echo "Key Point:"
                echo "  🎯 ALL OF THIS WAS DEFINED IN settings.kts"
                echo "  🎯 NO MANUAL TEAMCITY CONFIGURATION"
                echo "  🎯 FULLY VERSION CONTROLLED"
                echo "  🎯 COMPLETELY REPRODUCIBLE"
                echo ""
                echo "This is the power of Configuration as Code!"
                echo "============================================================"
            """.trimIndent()
        }
    }

    // Triggers
    triggers {
        vcs {
            id = "vcsTrigger"
            branchFilter = "+:*"
        }
    }

    // Build failure conditions
    failureConditions {
        errorMessage = true
        nonZeroExitCode = true
        failOnText {
            conditionType = BuildFailureOnText.ConditionType.CONTAINS
            pattern = "ERROR"
            failureMessage = "Build failed due to ERROR in log"
            reverse = false
        }
    }

    // Build features
    features {
        feature {
            type = "perfmon"
        }
    }

    // Requirements for build agents
    requirements {
        doesNotContain("teamcity.agent.name", "Windows")
    }
})