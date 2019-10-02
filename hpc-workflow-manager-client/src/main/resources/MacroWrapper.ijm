function parInit() {
	ret = call("MPIWrapper.initialise");
	parReportProgress(0, 0);
}

function parFinalize() {
	parReportProgress(0, 100);
	ret = call("MPIWrapper.finalise");
}

function parGetRank() {
	rank = call("MPIWrapper.getRank");
	return rank;
}

function parGetSize() {
	size = call("MPIWrapper.getSize");
	return size;
}

function parBarrier() {
	ret = call("MPIWrapper.barrier");
}

function parReportProgress(task, progress) {
	ret = call("MPIWrapper.reportProgress", task, progress);
}

function parReportText(text) {
	ret = call("MPIWrapper.reportText",text);
}

function addTask(description){
	ret = call("MPIWrapper.addTask", description);
}

function reportTasks(){
	ret = call("MPIWrapper.reportTasks");
}

function parScatterEqually(sendBuffer, root) {
	receivedBuffer = call("MPIWrapper.scatterEqually", sendBuffer, root);
	return receivedBuffer;
}

function parScatter(sendBuffer, sendCount, receiveCount, root) {
	receivedBuffer = call("MPIWrapper.scatter", sendBuffer, sendCount, receiveCount, root);
	return receivedBuffer;
}