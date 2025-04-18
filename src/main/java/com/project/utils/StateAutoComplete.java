package com.project.utils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Popup;

import java.util.ArrayList;

public class StateAutoComplete {
    private static final ObservableList<String> STATES = FXCollections.observableArrayList(
            "Andhra Pradesh","Arunachal Pradesh","Assam","Bihar","Chhattisgarh","Goa","Gujarat",
            "Haryana","Himachal Pradesh","Jharkhand","Karnataka","Kerala","Madhya Pradesh","Maharashtra",
            "Manipur","Meghalaya","Mizoram","Nagaland","Odisha","Punjab","Rajasthan","Sikkim","Tamil Nadu",
            "Telangana","Tripura","Uttar Pradesh","Uttarakhand","West Bengal"
    );

    private static final ListView<String> matchedStates = new ListView<>();

    public static void setAutoCompleteStates(TextField textField){
        Popup popup = new Popup();
        popup.setAutoHide(true);
        popup.getContent().add(matchedStates);

        textField.textProperty().addListener((obs, oldText, newText) ->{
            if(newText.isEmpty()){
                popup.hide();
                return;
            }

            ObservableList<String> filtered = STATES.filtered(state ->
                state.toLowerCase().startsWith(newText.toLowerCase())
            );

            if(filtered.isEmpty()){
                popup.hide();
                return;
            }

            matchedStates.setItems(filtered);

            Bounds bounds = textField.localToScreen(textField.getBoundsInLocal());
            popup.show(textField,bounds.getMinX(),bounds.getMaxY());
        });

        matchedStates.setOnMouseClicked(e -> {
            String selected = matchedStates.getSelectionModel().getSelectedItem();
            if (selected!=null){
                textField.setText(selected);
                popup.hide();
            }
        });
    }
}
