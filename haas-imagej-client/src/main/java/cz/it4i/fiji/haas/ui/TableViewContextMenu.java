package cz.it4i.fiji.haas.ui;

import java.util.Collection;
import java.util.LinkedList;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableView;
import javafx.scene.input.ContextMenuEvent;

public class TableViewContextMenu<T> {
	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(cz.it4i.fiji.haas.ui.TableViewContextMenu.class);
	private Collection<P_MenuItem> items = new LinkedList<TableViewContextMenu<T>.P_MenuItem>();
	private ContextMenu cm;
	private TableView<T> tableView;

	public TableViewContextMenu(TableView<T> tableView) {
		this.cm = new ContextMenu();
		this.tableView = tableView;
		tableView.setContextMenu(cm);
		tableView.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
			@Override
			public void handle(ContextMenuEvent event) {
				T selected = getSelectedItem();
				
				for (P_MenuItem item : items) {
					item.updateEnable(selected);
				}
			}
		});
	}

	public void addItem(String text, Consumer<T> eventHandler, Predicate<T> enableHandler) {
		items.add(new P_MenuItem(text, eventHandler, enableHandler));
	}

	private T getSelectedItem() {
		T result = null;
		if (tableView.getSelectionModel().getSelectedCells().size() >= 0) {
			result = tableView.getSelectionModel().getSelectedItem();
		}
		return result;
	}

	private class P_MenuItem {

		private MenuItem item;
		private Predicate<T> enableHandler;

		public P_MenuItem(String text, Consumer<T> eventHandler, Predicate<T> enableHandler) {
			this.enableHandler = enableHandler;
			item = new MenuItem(text);
			item.setOnAction(e -> eventHandler.accept(getSelectedItem()));
			cm.getItems().add(item);
		}

		public void updateEnable(T selected) {
			item.setDisable(!enableHandler.test(selected));
		}

	}

}
