package cz.it4i.fiji.haas.ui;

import java.util.Collection;
import java.util.LinkedList;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.event.EventHandler;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.ContextMenuEvent;

public class TableViewContextMenu<T> {

	public final static Logger log = LoggerFactory.getLogger(cz.it4i.fiji.haas.ui.TableViewContextMenu.class);

	private final Collection<P_Updatable<T>> items = new LinkedList<>();

	private final TableView<T> tableView;

	private int columnIndex = -1;

	public TableViewContextMenu(TableView<T> tableView) {
		this.tableView = tableView;
	}

	public void addItem(String text, Consumer<T> eventHandler, Predicate<T> enableHandler) {
		items.add(new P_MenuItem(text, eventHandler, enableHandler));
	}

	public void addItem(String text, BiConsumer<T, Integer> eventHandler, BiPredicate<T, Integer> enableHandler) {
		items.add(new P_MenuItemWithColumnIndex(text, eventHandler, enableHandler));
	}

	public void addItem(String text, Consumer<T> eventHandlerOn, Consumer<T> eventHandlerOff,
			Predicate<T> enableHandler, Function<T, Boolean> property) {
		items.add(new P_CheckMenuItem(text, eventHandlerOff, eventHandlerOn, enableHandler, property));
	}

	private T getRequestedItem() {
		return tableView.getFocusModel().getFocusedItem();
	}

	private int getRequestedColumn() {
		return columnIndex;
	}

	private ContextMenu getOrCreateContextMenu() {
		ContextMenu cm = tableView.getContextMenu();
		if (cm == null) {
			cm = new ContextMenu();
			tableView.setContextMenu(cm);
			tableView.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
				@Override
				public void handle(ContextMenuEvent event) {
					T requestedItem = getRequestedItem();
					updateColumnIndex(event.getSceneX());
					int columnIndex = getRequestedColumn();
					items.forEach(item -> item.updateEnable(requestedItem, columnIndex));
				}

				private void updateColumnIndex(double sceneX) {
					double last = 0;
					columnIndex = tableView.getColumns().size();
					int index = 0;
					for (TableColumn<?, ?> column : tableView.getColumns()) {
						last += column.getWidth();
						if (last > sceneX) {
							columnIndex = index;
							break;
						}
						index++;
					}
				}
			});

		}
		return cm;
	}

	private interface P_Updatable<T> {
		public void updateEnable(T selected, int columnIndex);
	}

	private class P_UpdatableImpl<I extends MenuItem> implements P_Updatable<T> {

		private final I item;
		private final Predicate<T> enableHandler;

		public P_UpdatableImpl(I item, Predicate<T> enableHandler) {
			this.item = item;
			this.enableHandler = enableHandler;
			getOrCreateContextMenu().getItems().add(getItem());
		}

		@Override
		public void updateEnable(T selected, int columnIndex) {
			item.setDisable(!enableHandler.test(selected));
		}

		protected I getItem() {
			return item;
		}

	}

	private class P_MenuItem extends P_UpdatableImpl<MenuItem> {
		public P_MenuItem(String text, Consumer<T> eventHandler, Predicate<T> enableHandler) {
			super(new MenuItem(text), enableHandler);
			getItem().setOnAction(e -> eventHandler.accept(getRequestedItem()));
		}

	}

	private class P_MenuItemWithColumnIndex implements P_Updatable<T> {

		private final MenuItem item;

		private final BiPredicate<T, Integer> enableHandler;

		public P_MenuItemWithColumnIndex(String text, BiConsumer<T, Integer> eventHandler,
				BiPredicate<T, Integer> enableHandler) {
			this.enableHandler = enableHandler;
			item = new MenuItem(text);
			item.setOnAction(e -> eventHandler.accept(getRequestedItem(), getRequestedColumn()));
			getOrCreateContextMenu().getItems().add(item);
		}

		@Override
		public void updateEnable(T selected, int column) {
			item.setDisable(!enableHandler.test(selected, column));
		}

	}

	private class P_CheckMenuItem extends P_UpdatableImpl<CheckMenuItem> {

		private final Function<T, Boolean> property;

		public P_CheckMenuItem(String text, Consumer<T> eventHandlerOff, Consumer<T> eventHandlerOn,
				Predicate<T> enableHandler, Function<T, Boolean> property) {
			super(new CheckMenuItem(text), enableHandler);
			this.property = property;
			getItem().setOnAction(e -> {
				boolean selected = getItem().isSelected();
				if (selected) {
					eventHandlerOn.accept(getRequestedItem());
				} else {
					eventHandlerOff.accept(getRequestedItem());
				}
			});
		}

		@Override
		public void updateEnable(T selected, int columnIndex) {
			super.updateEnable(selected, columnIndex);
			getItem().setSelected(property.apply(selected));
		}

	}

}
