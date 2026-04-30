package com.shangmin.whisperrr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for the Whisperrr Direct Audio Transcription API.
 * 
 * <p>This is the entry point for the simplified Spring Boot application that provides
 * instant audio transcription services. The application serves as a lightweight proxy
 * between the React frontend and the Python transcription service, handling:</p>
 * 
 * <ul>
 *   <li>Audio file upload and validation</li>
 *   <li>Direct communication with Python FastAPI transcription service</li>
 *   <li>CORS configuration for frontend communication</li>
 *   <li>Comprehensive error handling and logging</li>
 *   <li>Instant transcription results without persistence</li>
 * </ul>
 * 
 * <h3>Simplified Architecture:</h3>
 * <p>The application follows a streamlined proxy pattern:</p>
 * <ul>
 *   <li><strong>Controller Layer:</strong> REST endpoints for client communication</li>
 *   <li><strong>Service Layer:</strong> Validation and Python service integration</li>
 *   <li><strong>DTO Layer:</strong> Data transfer objects for API communication</li>
 *   <li><strong>Configuration Layer:</strong> CORS and service integration settings</li>
 * </ul>
 * 
 * <h3>Key Features:</h3>
 * <ul>
 *   <li>RESTful API with instant transcription results</li>
 *   <li>No database - stateless operation</li>
 *   <li>File upload validation and processing</li>
 *   <li>Direct Python service integration</li>
 *   <li>Health monitoring and metrics</li>
 *   <li>CORS configuration for cross-origin requests</li>
 * </ul>
 * 
 * <h3>Stateless Operation:</h3>
 * <p>The application operates without any persistent storage:</p>
 * <ul>
 *   <li><strong>No Database:</strong> No setup or maintenance required</li>
 *   <li><strong>Direct Processing:</strong> Files processed immediately</li>
 *   <li><strong>Instant Results:</strong> No job queuing or polling</li>
 *   <li><strong>Simplified Deployment:</strong> Fewer moving parts</li>
 * </ul>
 * 
 * <h3>Configuration:</h3>
 * <p>The application is configured through application.properties and supports:</p>
 * <ul>
 *   <li>CORS configuration for frontend communication</li>
 *   <li>Python service integration settings</li>
 *   <li>File upload limits and validation rules</li>
 *   <li>Logging and monitoring configuration</li>
 * </ul>
 * 
 * @author shangmin
 * @version 2.0
 * @since 2024
 * 
 * @see com.shangmin.whisperrr.controller.AudioController
 * @see com.shangmin.whisperrr.service.AudioService
 * @see com.shangmin.whisperrr.config.CorsConfig
 */
@SpringBootApplication
public class WhisperrrApiApplication {

	/**
	 * Main method to start the simplified Spring Boot application.
	 * 
	 * <p>This method initializes the Spring application context, sets up all
	 * configured beans, starts the embedded Tomcat server, and begins listening
	 * for HTTP requests on the configured port (default: 7331).</p>
	 * 
	 * <p>The application will automatically:</p>
	 * <ul>
	 *   <li>Configure CORS for frontend communication</li>
	 *   <li>Set up REST endpoints for direct audio transcription</li>
	 *   <li>Initialize HTTP client for Python service communication</li>
	 *   <li>Enable health monitoring and metrics</li>
	 *   <li>Configure file upload handling</li>
	 * </ul>
	 * 
	 * <p><strong>No Database Required:</strong> This simplified version operates
	 * without any database setup, providing instant transcription results.</p>
	 * 
	 * @param args command line arguments passed to the application
	 *             Common arguments include:
	 *             --server.port=7331 (change server port)
	 *             --spring.profiles.active=dev (activate dev profile)
	 *             --logging.level.com.shangmin.whisperrr=DEBUG (enable debug logging)
	 *             --whisperrr.service.url=http://localhost:5001 (Python service URL)
	 */
	public static void main(String[] args) {
		SpringApplication.run(WhisperrrApiApplication.class, args);
	}

}
