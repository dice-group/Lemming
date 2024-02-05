package org.aksw.simba.lemming.configuration;

import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component("inputValidator")
@PropertySource(value = "classpath:application.properties")
public class Validator {
	
	/** Supported datasets and respective folder path */
	@Value("#{PropertySplitter.toSet('${datasets.allowed}')}")
	private Set<String> allowedDatasets;
	
	public Set<String> getAllowedDatasets() {
        return allowedDatasets;
    }
	
	public void isDatasetAllowed(String dataset) {
		if (!allowedDatasets.contains(dataset)) {
			throw new IllegalArgumentException(String.format("Unknown dataset: %s", dataset));
		}		
	}

}
