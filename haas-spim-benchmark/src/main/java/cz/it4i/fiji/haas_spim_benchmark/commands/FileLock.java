package cz.it4i.fiji.haas_spim_benchmark.commands;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileLock implements Closeable{
	
	public static final Logger log = LoggerFactory.getLogger(cz.it4i.fiji.haas_spim_benchmark.commands.FileLock.class);
	
	private FileChannel fileChannel;
	
	private java.nio.channels.FileLock lock;

	private Path filePath;


	public FileLock(Path file) throws FileNotFoundException {
		this.filePath = file;
        
	}
	
	
	public boolean tryLock() throws IOException {
		this.fileChannel = FileChannel.open(filePath, StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE); 
		
	    try {
            this.lock = fileChannel.tryLock();
        } catch (OverlappingFileLockException e) {
        	this.fileChannel.close();
            return false;
        }
	    return true;
	    
	}


	@Override
	public void close() {
		if(lock != null) {
			try {
				lock.release();
			} catch (IOException e) {
				log.error(e.getMessage(), e);
			}
		}
		
		try {
			fileChannel.close();
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
		
		try {
			Files.delete(filePath);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
	}
}
