package org.summerclouds.common.rest.operation;

import java.util.Map;

import org.summerclouds.common.core.form.DefRoot;
import org.summerclouds.common.core.node.IReadProperties;
import org.summerclouds.common.core.operation.OperationDescription;

public class ServiceOperationDescription {

	private String path;
	private String version;
	private IReadProperties labels;
	private Map<String, Object> form;

	public ServiceOperationDescription(OperationDescription desc) {
		path = desc.getPath();
		version = desc.getVersionString();
		labels = desc.getLabels();
		DefRoot f = desc.getForm();
		if (f != null)
			form = f.toMap();
	}

	public String getPath() {
		return path;
	}

	public String getVersion() {
		return version;
	}

	public IReadProperties getLabels() {
		return labels;
	}

	public Map<String, Object> getForm() {
		return form;
	}

	public ServiceOperationDescription setPath(String path) {
		this.path = path;
		return this;
	}

}
