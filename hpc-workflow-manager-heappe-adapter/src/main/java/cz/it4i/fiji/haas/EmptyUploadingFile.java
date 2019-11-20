/*******************************************************************************
 * IT4Innovations - National Supercomputing Center
 * Copyright (c) 2017 - 2019 All Right Reserved, https://www.it4i.cz
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE', which is part of this project.
 ******************************************************************************/
package cz.it4i.fiji.haas;

import java.io.IOException;
import java.io.InputStream;

import cz.it4i.fiji.hpc_client.UploadingFile;


public class EmptyUploadingFile implements UploadingFile {

	private static final InputStream EMPTY_STREAM = new InputStream() {

		@Override
		public int read() throws IOException {
			return -1;
		}
	};

	private final String name;

	public EmptyUploadingFile(String name) {
		this.name = name;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return EMPTY_STREAM;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public long getLength() throws IOException {
		return 0;
	}

	@Override
	public long getLastTime() {
		return 0;
	}

}
