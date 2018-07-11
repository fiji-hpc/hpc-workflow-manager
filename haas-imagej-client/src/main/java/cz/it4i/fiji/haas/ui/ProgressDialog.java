package cz.it4i.fiji.haas.ui;

import java.awt.Adjustable;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import net.imagej.ui.swing.updater.SwingTools;
import net.imagej.updater.util.Progress;
import net.imagej.updater.util.UpdateCanceledException;

/**
 * 
 * @author Johannes Schindelin
 */
public class ProgressDialog extends JDialog implements Progress {
	
	JProgressBar progress;
	JButton detailsToggle;
	int toggleHeight = -1;
	JScrollPane detailsScrollPane;
	Details details;
	Detail latestDetail;
	String title;
	boolean canceled;
	protected long latestUpdate, itemLatestUpdate;

	public ProgressDialog(final Window owner) {
		this(owner, null);
	}
	
	public ProgressDialog(Window owner, String title) {
		this(owner, title, null);
	}

	public ProgressDialog(final Window owner, final String title, Runnable cancelableAction) {
		super(owner, title);
		boolean canCancel = cancelableAction != null;
		final Container root = getContentPane();
		root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
		progress = new JProgressBar();
		progress.setStringPainted(true);
		progress.setMinimum(0);
		root.add(progress);

		final JPanel buttons = new JPanel();
		detailsToggle = new JButton("Show Details");
		detailsToggle.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent event) {
				toggleDetails();
			}
		});
		buttons.add(detailsToggle);
		final JButton cancel = new JButton("Cancel");
		cancel.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				canceled = true;
				ProgressDialog.this.dispose();
				if (cancelableAction != null) {
					cancelableAction.run();
				}
			}
		});
		if(canCancel) {
			buttons.add(cancel);
		}
		buttons.setMaximumSize(buttons.getMinimumSize());
		root.add(buttons);

		details = new Details();
		detailsScrollPane = new JScrollPane(details, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		detailsScrollPane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {

			@Override
			public void adjustmentValueChanged(AdjustmentEvent e) {
				final int value = e.getValue();
				final Adjustable adjustable = e.getAdjustable();
				final int maximum = adjustable.getMaximum();
				if (value != maximum)
					adjustable.setValue(maximum);
			}
		});
		detailsScrollPane.setVisible(false);
		root.add(detailsScrollPane);

		if (title != null)
			setTitle(title);
		pack();
		if (owner != null) {
			final Dimension o = owner.getSize();
			final Dimension size = getSize();
			if (size.width < o.width / 2) {
				size.width = o.width / 2;
				setSize(size);
			}
			setLocation(owner.getX() + (o.width - size.width) / 2, owner.getY() + (o.height - size.height) / 2);
		} else { 
			setLocationRelativeTo(owner);
		}

		final KeyAdapter keyAdapter = new KeyAdapter() {

			@Override
			public void keyReleased(final KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
					cancel();
			}
		};
		root.addKeyListener(keyAdapter);
		detailsToggle.addKeyListener(keyAdapter);
		cancel.addKeyListener(keyAdapter);

		if (title != null)
			setVisible(true);
	}

	public void cancel() {
		canceled = true;
	}

	protected void checkIfCanceled() {
		if (canceled)
			throw new UpdateCanceledException();
	}

	@Override
	public void setTitle(final String title) {
		this.title = title;
		setTitle();
		setVisible(true);
	}

	protected void setTitle() {
		checkIfCanceled();
		SwingTools.invokeOnEDT(new Runnable() {
			@Override
			public void run() {
				if (detailsScrollPane.isVisible() || latestDetail == null)
					progress.setString(title);
				else
					progress.setString(title + ": " + latestDetail.getString());
			}
		});
		repaint();
	}

	@Override
	public void setCount(final int count, final int total) {
		checkIfCanceled();
		if (updatesTooFast())
			return;
		SwingTools.invokeOnEDT(new Runnable() {
			@Override
			public void run() {
				progress.setMaximum(total);
				progress.setValue(count);
			}
		});
		repaint();
	}

	@Override
	public void addItem(final Object item) {
		checkIfCanceled();
		details.addDetail(item.toString());
		if (itemUpdatesTooFast() && !detailsScrollPane.isVisible())
			return;
		setTitle();
		validate();
		repaint();
	}

	@Override
	public void setItemCount(final int count, final int total) {
		checkIfCanceled();
		if (itemUpdatesTooFast())
			return;
		SwingTools.invokeOnEDT(new Runnable() {
			@Override
			public void run() {
				latestDetail.setMaximum(total);
				latestDetail.setValue(count);
				repaint();
			}
		});
	}

	@Override
	public void itemDone(final Object item) {
		checkIfCanceled();
		if (itemUpdatesTooFast() && !detailsScrollPane.isVisible())
			return;
		
		SwingTools.invokeOnEDT(new Runnable() {
			@Override
			public void run() {
				latestDetail.setValue(latestDetail.getMaximum());
			}
		});
	}

	@Override
	public void done() {
		if (latestDetail != null)
			latestDetail.setValue(latestDetail.getMaximum());
		SwingTools.invokeOnEDT(new Runnable() {
			@Override
			public void run() {
				progress.setValue(progress.getMaximum());
				dispose();
			}
		});
	}

	public void toggleDetails() {
		SwingTools.invokeOnEDT(new Runnable() {
			@Override
			public void run() {
				final boolean show = !detailsScrollPane.isVisible();
				detailsScrollPane.setVisible(show);
				detailsScrollPane.invalidate();
				detailsToggle.setText(show ? "Hide Details" : "Show Details");
				setTitle();

				final Dimension dimension = getSize();
				if (toggleHeight == -1)
					toggleHeight = dimension.height + 100;
				setSize(new Dimension(dimension.width, toggleHeight));
				toggleHeight = dimension.height;
			}
		});
	}

	private class Details extends JPanel {

		Details() {
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		}

		public void addDetail(final String panelTitle) {
			addDetail(new Detail(panelTitle));
		}

		public void addDetail(final Detail detail) {
			add(detail);
			latestDetail = detail;
		}
	}

	private class Detail extends JProgressBar {

		Detail(final String text) {
			setStringPainted(true);
			setString(text);
		}
	}

	protected boolean updatesTooFast() {
		if (System.currentTimeMillis() - latestUpdate < 50)
			return true;
		latestUpdate = System.currentTimeMillis();
		return false;
	}

	protected boolean itemUpdatesTooFast() {
		if (System.currentTimeMillis() - itemLatestUpdate < 50)
			return true;
		itemLatestUpdate = System.currentTimeMillis();
		return false;
	}
}
