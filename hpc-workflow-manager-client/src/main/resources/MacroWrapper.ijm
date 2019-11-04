// Start of parallel functions code section:
// This code is automatically appended to the user script file, 
// Do NOT remove this section of code!

function parInit() {
	ret = call("cz.it4i.fiji.ij1_mpi_wrapper.MPIWrapper.initialise");
	parReportProgress(0, 0);
}

function parFinalize() {
	parReportProgress(0, 100);
	ret = call("cz.it4i.fiji.ij1_mpi_wrapper.MPIWrapper.finalise");
}

function parGetRank() {
	rank = call("cz.it4i.fiji.ij1_mpi_wrapper.MPIWrapper.getRank");
	return rank;
}

function parGetSize() {
	size = call("cz.it4i.fiji.ij1_mpi_wrapper.MPIWrapper.getSize");
	return size;
}

function parBarrier() {
	ret = call("cz.it4i.fiji.ij1_mpi_wrapper.MPIWrapper.barrier");
}

function parReportProgress(task, progress) {
	ret = call("cz.it4i.fiji.ij1_mpi_wrapper.MPIWrapper.reportProgress", task, progress);
}

function parReportText(text) {
	ret = call("cz.it4i.fiji.ij1_mpi_wrapper.MPIWrapper.reportText",text);
}

function parAddTask(description){
	id = call("cz.it4i.fiji.ij1_mpi_wrapper.MPIWrapper.addTask", description);
	return id;
}

function parReportTasks(){
	ret = call("cz.it4i.fiji.ij1_mpi_wrapper.MPIWrapper.reportTasks");
}

function parScatterEqually(sendBuffer, totalSendBufferLength, root) {
	sendString = convertArrayToCommaSeparatedString(sendBuffer);
	receivedString = call("cz.it4i.fiji.ij1_mpi_wrapper.MPIWrapper.scatterEqually", sendString, totalSendBufferLength, root);
	receivedBuffer = convertCommaSeparatedStringToArray(receivedString);
	return receivedBuffer;
}

function parScatter(sendBuffer, sendCount, receiveCount, root) {
	sendString = convertArrayToCommaSeparatedString(sendBuffer);
	receivedString = call("cz.it4i.fiji.ij1_mpi_wrapper.MPIWrapper.scatter", sendString, sendCount, receiveCount, root);
	receivedBuffer = convertCommaSeparatedStringToArray(receivedString);
	return receivedBuffer;
}

function parGather(sendBuffer, sendCount, receiveCount, receiver){
	sendString = convertArrayToCommaSeparatedString(sendBuffer);
	receivedString = call("cz.it4i.fiji.parallel_macro.ParallelMacro.gather", sendString, sendCount, receiveCount, root);
	receivedBuffer = convertCommaSeparatedStringToArray(receivedString);
	return receivedBuffer;
}

function parGatherEqually(sendBuffer, totalReceiveBufferLength, receiver){
	sendString = convertArrayToCommaSeparatedString(sendBuffer);
	receivedString = call("cz.it4i.fiji.parallel_macro.ParallelMacro.gatherEqually", sendString, totalSendBufferLength, root);
	receivedBuffer = convertCommaSeparatedStringToArray(receivedString);
	return receivedBuffer;
}

function convertArrayToCommaSeparatedString(array){
	length = lengthOf(array);
	string = "";
	for(i = 0; i < length; i++){
		string += ""+array[i];
		if(i != length - 1){
			string += ", ";
		}
	}
	return string;
}

function convertCommaSeparatedStringToArray(string) {
	array = split(string, ",");
	return array;
}

// End of parallel functions section, 
// bellow this point is the user's code:
