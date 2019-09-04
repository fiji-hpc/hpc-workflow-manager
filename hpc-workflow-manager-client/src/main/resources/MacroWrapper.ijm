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

function parGetMySubList(list) {
	rank = parGetRank();
	size = parGetSize();
	lastRank = round(size)-1;
	if (rank == lastRank) 
		subList = Array.slice(list, (rank) * round(list.length / size) , list.length);
	else
        subList = Array.slice(list, (rank) * round(list.length / size) , (rank + 1) * round(list.length / size));
	return subList;
}

function countFiles(path) {
  list = getFileList(path); 
  for (i=0; i<list.length; i++) {
	  if (endsWith(list[i], "/"))
		  countFiles(""+path+list[i]);
	  else
		  count++;
  }
}

function processFiles(dir) {
  list = getFileList(dir);
  subList = parGetMySubList(list);

  for (i=0; i<subList.length; i++) {
	  if (endsWith(subList[i], "/"))
		  processFiles(""+dir+subList[i]);
	  else {
		 //showProgress(n++, count);
		 path = dir+subList[i];
		 processFile(path);
	  }
  }
}

function processFile(path) {
   if (endsWith(path, ".tif")) {
	   print("#" + rank + " node processing " + path);
	   processed++;
	   //open(path);
	   //run("Subtract Background...", "rolling=50 white");
	   //save(path);
	   //close();
  }
}