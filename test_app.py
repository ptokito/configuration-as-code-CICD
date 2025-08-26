"""
Simple test file for Configuration as Code Demo
This demonstrates automated testing in the CI/CD pipeline
"""

import pytest
import json
import sys
import os

# Add current directory to path
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

# Import the Flask app
from app import app

@pytest.fixture
def client():
    """Create a test client for the Flask app"""
    app.config['TESTING'] = True
    with app.test_client() as client:
        yield client

def test_hello_world_route(client):
    """Test that the main route returns 200"""
    response = client.get('/')
    assert response.status_code == 200
    print("✅ Main route test passed")

def test_health_check(client):
    """Test the health check endpoint"""
    response = client.get('/health')
    assert response.status_code == 200

    # Parse JSON response
    data = json.loads(response.data)
    assert data['status'] == 'healthy'
    assert data['service'] == 'Configuration-as-Code-Demo'
    print("✅ Health check test passed")

def test_api_info(client):
    """Test the API info endpoint"""
    response = client.get('/api/info')
    assert response.status_code == 200

    # Parse JSON response
    data = json.loads(response.data)

    # Check required fields exist
    assert 'message' in data
    assert 'features' in data
    assert 'pipeline' in data

    # Verify pipeline configuration
    assert data['pipeline']['source'] == 'GitHub'
    assert 'GitHub Actions' in data['pipeline']['ci_cd']
    assert data['pipeline']['deployment'] == 'Render.com via Webhook'

    print("✅ API info test passed")

def test_configuration_as_code_benefits(client):
    """Test that the API lists Configuration as Code benefits"""
    response = client.get('/api/info')
    data = json.loads(response.data)

    # Check that key features are listed
    expected_features = [
        'Version Control Integration',
        'Automated Testing',
        'Security Scanning',
        'Multi-environment Deployment',
        'Infrastructure as Code'
    ]

    for feature in expected_features:
        assert feature in data['features'], f"Missing feature: {feature}"

    print("✅ Configuration as Code benefits test passed")

def test_deployment_info(client):
    """Test the deployment info endpoint"""
    response = client.get('/api/deployment')
    assert response.status_code == 200

    data = json.loads(response.data)
    assert 'deployment_method' in data
    assert 'GitHub Actions' in data['deployment_method']
    assert 'trigger_chain' in data
    assert len(data['trigger_chain']) == 5

    print("✅ Deployment info test passed")

# Allow running tests directly
if __name__ == "__main__":
    print("Running tests for Configuration as Code Demo...")
    pytest.main([__file__, '-v', '--tb=short'])