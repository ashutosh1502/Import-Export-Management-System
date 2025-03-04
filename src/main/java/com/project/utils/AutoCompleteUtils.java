package com.project.utils;

import javafx.collections.ObservableList;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;

public class AutoCompleteUtils {
    public static ContextMenu createContextMenu(ObservableList<String> suggestions, TextField textField, java.util.function.Consumer<String> onSelect) {
        ContextMenu contextMenu = new ContextMenu();

        for (String suggestion : suggestions) {
            MenuItem item = new MenuItem(suggestion);
            item.setOnAction(e -> onSelect.accept(suggestion));
            contextMenu.getItems().add(item);
        }

        return contextMenu;
    }
}

