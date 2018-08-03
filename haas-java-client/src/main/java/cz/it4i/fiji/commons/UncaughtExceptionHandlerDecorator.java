
package cz.it4i.fiji.commons;

import java.io.Closeable;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ThreadFactory;
import java.util.function.BiPredicate;

import org.slf4j.Logger;

abstract public class UncaughtExceptionHandlerDecorator implements
	UncaughtExceptionHandler, Closeable
{

	private final UncaughtExceptionHandler previousHandler;

	private UncaughtExceptionHandler decoratedHandler;

	private final Collection<BiPredicate<Thread, Throwable>> handlers =
		new LinkedList<>();

	private boolean closed;

	public static UncaughtExceptionHandlerDecorator setDefaultHandler() {
		return setDefaultHandler(null);
	}

	public static UncaughtExceptionHandlerDecorator setDefaultHandler(
		final Logger logger)
	{
		final UncaughtExceptionHandlerDecorator result =
			new UncaughtExceptionHandlerDecorator(Thread
				.getDefaultUncaughtExceptionHandler(), logger)
			{

				@Override
				protected void setPreviousHandler(
					final UncaughtExceptionHandler handler)
			{
					Thread.setDefaultUncaughtExceptionHandler(handler);
				}

				@Override
				public void activate() {
					Thread.setDefaultUncaughtExceptionHandler(this);
				}
			};

		return result;
	}

	public static UncaughtExceptionHandlerDecorator setHandler(
		final Thread thread, final Logger logger)
	{
		final UncaughtExceptionHandlerDecorator result =
			new UncaughtExceptionHandlerDecorator(thread
				.getUncaughtExceptionHandler(), logger)
			{

				@Override
				protected void setPreviousHandler(
					final UncaughtExceptionHandler handler)
			{
					thread.setUncaughtExceptionHandler(handler);
				}

				@Override
				public void activate() {
					thread.setUncaughtExceptionHandler(this);

				}
			};
		thread.setUncaughtExceptionHandler(result);
		return result;
	}

	@SafeVarargs
	public static ThreadFactory createThreadFactory(
		final BiPredicate<Thread, Throwable>... handlers)
	{
		final ThreadFactory result = new ThreadFactory() {

			@Override
			public Thread newThread(final Runnable r) {
				final Thread t = new Thread(r);
				final UncaughtExceptionHandlerDecorator uehd = setHandler(t, null);
				for (final BiPredicate<Thread, Throwable> handler : handlers) {
					uehd.registerHandler(handler);
				}
				uehd.activate();
				return t;
			}
		};
		return result;
	}

	private UncaughtExceptionHandlerDecorator(
		final UncaughtExceptionHandler previousHandler, final Logger logger)
	{
		this.previousHandler = previousHandler;
		if (previousHandler != null) {
			this.decoratedHandler = previousHandler;
		}
		else {
			this.decoratedHandler = new UncaughtExceptionHandler() {

				@Override
				public void uncaughtException(final Thread t, final Throwable e) {
					if (logger != null) {
						logger.error(e.getMessage(), e);
					}
					else {
						e.printStackTrace(System.err);
					}
				}
			};
		}

	}

	public UncaughtExceptionHandlerDecorator registerHandler(
		final BiPredicate<Thread, Throwable> handler)
	{
		handlers.add(handler);
		return this;
	}

	@Override
	public void uncaughtException(final Thread t, final Throwable e) {
		for (final BiPredicate<Thread, Throwable> handler : handlers) {
			if (handler.test(t, e)) {
				return;
			}
		}
		decoratedHandler.uncaughtException(t, e);
	}

	@Override
	synchronized public void close() {
		if (!closed) {
			if (previousHandler != null) {
				setPreviousHandler(previousHandler);
			}
			closed = true;
		}
	}

	abstract public void activate();

	abstract protected void setPreviousHandler(UncaughtExceptionHandler handler);

}
