package org.aksw.simba.lemming.configuration;

import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.ParameterException;

@Component
@PropertySource(value = "classpath:application.properties")
public class Validator implements IParameterValidator {

	/** Supported datasets and respective folder path */
	@Value("#{PropertySplitter.toSet('${datasets.allowed}')}")
	private Set<String> allowedDatasets;
	
	private Environment environment;
	
	public Validator(Environment environment) {
		this.environment = environment;
	}
    
    
    @Override
    public void validate(String name, String value) throws ParameterException {
        System.out.println("Dataset path provided: " + value);
    }
    
    public String resolveDatasetPath(String datasetName, String providedPath) {
        if (providedPath != null && !providedPath.trim().isEmpty()) {
            return providedPath;
        }
        
        // Try to get from properties
        String propertyKey = "datasets." + datasetName + ".filepath";
        String resolvedPath = environment.getProperty(propertyKey);
        
        // throw if we can't find it
        if (resolvedPath == null) {
            throw new IllegalArgumentException("Dataset path not provided and not found in application.properties for dataset: " + datasetName);
        }
        
        return resolvedPath;
    }

	public Set<String> getAllowedDatasets() {
		return allowedDatasets;
	}

	public void isDatasetAllowed(String dataset) {
		if (!allowedDatasets.contains(dataset)) {
			throw new IllegalArgumentException(String.format("Unknown dataset: %s", dataset));
		}
	}

	public int validateThreads (int threads) {
		if(threads < 1) {
			return Runtime.getRuntime().availableProcessors();
		}
		return threads;
	}

}
