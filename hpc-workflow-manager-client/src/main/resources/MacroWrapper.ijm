// Start of parallel functions code section:
// This code is automatically appended to the user script file, 
// Do NOT remove this section of code!

function parInit() {
	ret = call("cz.it4i.fiji.parallel_macro.ParallelMacro.initialise");
}

function parFinalize() {
	ret = call("cz.it4i.fiji.parallel_macro.ParallelMacro.finalise");
}

function parGetRank() {
	rank = call("cz.it4i.fiji.parallel_macro.ParallelMacro.getRank");
	return rank;
}

function parGetSize() {
	size = call("cz.it4i.fiji.parallel_macro.ParallelMacro.getSize");
	return size;
}

function parBarrier() {
	ret = call("cz.it4i.fiji.parallel_macro.ParallelMacro.barrier");
}

function parReportProgress(task, progress) {
	ret = call("cz.it4i.fiji.parallel_macro.ParallelMacro.reportProgress", task, progress);
}

function parReportText(text) {
	ret = call("cz.it4i.fiji.parallel_macro.ParallelMacro.reportText",text);
}

function parAddTask(description){
	id = call("cz.it4i.fiji.parallel_macro.ParallelMacro.addTask", description);
	return id;
}

function parReportTasks(){
	ret = call("cz.it4i.fiji.parallel_macro.ParallelMacro.reportTasks");
}

function parScatterEqually(sendBuffer, totalSendBufferLength, root) {
	sendString = convertArrayToCommaSeparatedString(sendBuffer);
	receivedString = call("cz.it4i.fiji.parallel_macro.ParallelMacro.scatterEqually", sendString, totalSendBufferLength, root);
	receivedBuffer = convertCommaSeparatedStringToArray(receivedString);
	return receivedBuffer;
}

function parScatter(sendBuffer, sendCount, receiveCount, root) {
	sendString = convertArrayToCommaSeparatedString(sendBuffer);
	receivedString = call("cz.it4i.fiji.parallel_macro.ParallelMacro.scatter", sendString, sendCount, receiveCount, root);
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
	receivedString = call("cz.it4i.fiji.parallel_macro.ParallelMacro.gatherEqually", sendString, totalReceiveBufferLength, receiver);
	receivedBuffer = convertCommaSeparatedStringToArray(receivedString);
	return receivedBuffer;
}

function parSelectProgressLogger(type){
	ret = call("cz.it4i.fiji.parallel_macro.ParallelMacro.selectProgressLogger", type);
}

function convert1DArrayToImage(Array1D, imageWidth, imageHeight, imageName){
	newImage(imageName, "24-bit", imageWidth, imageHeight,1);
	image1=getImageID();
	for(i=0; i<imageHeight; i++){
		for(j=0; j<imageWidth; j++){
			setPixel(j,i,Array1D[(i*imageWidth)+j]);
		}
	}
	return image1;
}

function convertImageTo1DArray(anImage){
	getDimensions(width, height, channels, slices, frames);
	myArray = newArray(width*height);
	for (i = 0; i < height; i++) {
		for (j = 0; j < width; j++) {
			myArray[i*width+j] = getPixel(j, i);
		}		
	}
	
	return myArray;
}
function convertArrayToCommaSeparatedString(array){
	String.resetBuffer();
	length = lengthOf(array);
	for(i = 0; i < length; i++){
		String.append(""+array[i]);
		if(i != length - 1){
			String.append(", ");
		}
	}
	str = String.buffer;
	String.resetBuffer();
	return str;
}

function convertCommaSeparatedStringToArray(string) {
	array = split(string, ",");
	return array;
}

// End of parallel functions section, 
// bellow this point is the user's code:
