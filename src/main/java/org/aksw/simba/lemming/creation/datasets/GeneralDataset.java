package org.aksw.simba.lemming.creation.datasets;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("custom")
@Scope(value = "prototype")
public class GeneralDataset extends AbstractDatasetManager {

	public GeneralDataset(String folderPath) {
		super("Custom", folderPath);
	}
	
	

}
