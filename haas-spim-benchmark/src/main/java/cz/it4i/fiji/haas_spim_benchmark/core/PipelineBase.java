package cz.it4i.fiji.haas_spim_benchmark.core;

import java.util.Collection;

abstract public class PipelineBase<T extends PipelineBase<?,?>,S> {
	private Collection<T> successors;
	private S id;
	
	
	
	public PipelineBase( S id) {
		super();
		this.id = id;
	}

	
	public Collection<T> getSuccessors() {
		if(successors == null) {
			successors = fillSuccesors();
		}
		return successors;
	}
	
	public S getId() {
		return id;
	}

	abstract protected Collection<T> fillSuccesors(); 
}
