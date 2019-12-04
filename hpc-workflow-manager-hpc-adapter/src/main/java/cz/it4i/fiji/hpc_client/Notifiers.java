/*******************************************************************************
 * IT4Innovations - National Supercomputing Center
 * Copyright (c) 2017 - 2019 All Right Reserved, https://www.it4i.cz
 *
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE', which is part of this project.
 ******************************************************************************/
package cz.it4i.fiji.hpc_client;

import cz.it4i.fiji.scpclient.TransferFileProgress;

public final class Notifiers {

	private Notifiers() {}


	private final static TransferFileProgress EMPTY_TRANSFER_FILE_PROGRESS =
		bytesTransfered -> {};
	private final static ProgressNotifier EMPTY_PROGRESS_NOTIFIER =
		new ProgressNotifier()
		{

		@Override
		public void setTitle(final String title) {
			// NOP
		}

		@Override
		public void setItemCount(final int count, final int total) {
			// NOP
		}

		@Override
		public void setCount(final int count, final int total) {
			// NOP
		}

		@Override
		public void itemDone(final Object item) {
			// NOP
		}

		@Override
		public void done() {
			// NOP
		}

		@Override
		public void addItem(final Object item) {
			// NOP
		}
	};

	public static TransferFileProgress emptyTransferFileProgress() {
		return EMPTY_TRANSFER_FILE_PROGRESS;
	}

	public static ProgressNotifier emptyProgressNotifier() {
		return EMPTY_PROGRESS_NOTIFIER;
	}
}
