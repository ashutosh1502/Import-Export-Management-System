package com.project.utils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Popup;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

public class AutoCompleteUtils {
    private static final ObservableList<String> productSuggestions = FXCollections.observableArrayList();
    private static final ArrayList<String> suggestedProductId = new ArrayList<>();
    private static final ArrayList<Double> suggestedProductPrice = new ArrayList<>();
    private static final ObservableList<String> STATES = FXCollections.observableArrayList(
            "Andhra Pradesh","Arunachal Pradesh","Assam","Bihar","Chhattisgarh","Goa","Gujarat",
            "Haryana","Himachal Pradesh","Jharkhand","Karnataka","Kerala","Madhya Pradesh","Maharashtra",
            "Manipur","Meghalaya","Mizoram","Nagaland","Odisha","Punjab","Rajasthan","Sikkim","Tamil Nadu",
            "Telangana","Tripura","Uttar Pradesh","Uttarakhand","West Bengal"
    );
    private static final ListView<String> suggestionList = new ListView<>();
    private static final ListView<String> matchedStates = new ListView<>();

    public static void setAutoCompleteProductName(Connection conn, TextField productName, TextField productId, TextField productPrice) {
        Popup popup = new Popup();
        popup.setAutoHide(true);
        popup.getContent().add(suggestionList);

        productName.textProperty().addListener((obs, oldText, newtext) -> {
            if(newtext.isEmpty()){
                popup.hide();
                return;
            }
            ObservableList<String> filtered = fetchMatchingProducts(conn, newtext, suggestionList);
            if(filtered.isEmpty()){
                popup.hide();
                return;
            }
            suggestionList.setItems(filtered);
            Bounds bounds = productName.localToScreen(productName.getBoundsInLocal());
            popup.show(productName,bounds.getMinX(),bounds.getMaxY());
        });

        // Handle item selection from the ListView
        suggestionList.setOnMouseClicked(event -> {
            String selectedProduct = suggestionList.getSelectionModel().getSelectedItem();
            int index = suggestionList.getSelectionModel().getSelectedIndex();
            String selectedProductId = suggestedProductId.get(index);
            double selectedProductPrice = suggestedProductPrice.get(index);
            if (selectedProduct != null) {
                productName.clear();
                productName.setText(selectedProduct);
                productId.clear();
                productId.setText(selectedProductId);
                productPrice.clear();
                productPrice.setText(Double.toString(selectedProductPrice));
                suggestionList.setVisible(false);
            }
        });
        suggestionList.addEventHandler(KeyEvent.KEY_PRESSED,event ->{
            if (event.getCode() == KeyCode.ENTER) {
                String selectedProduct = suggestionList.getSelectionModel().getSelectedItem();
                String selectedProductId = suggestedProductId.get(suggestionList.getSelectionModel().getSelectedIndex());
                double selectedProductPrice = suggestedProductPrice.get(suggestionList.getSelectionModel().getSelectedIndex());
                if (selectedProduct != null) {
                    productName.clear();
                    productName.setText(selectedProduct);
                    productId.clear();
                    productId.setText(selectedProductId);
                    productPrice.clear();
                    productPrice.setText(Double.toString(selectedProductPrice));
                    suggestionList.setVisible(false);
                }
            }
        });
    }

    private static ObservableList<String> fetchMatchingProducts(Connection conn, String input, ListView<String> suggestionList) {
        productSuggestions.clear();
        suggestedProductId.clear();
        String query = "SELECT product_name,product_id,price FROM products WHERE LOWER(product_name) LIKE LOWER(?)";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, input + "%");
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                productSuggestions.add(rs.getString("product_name"));
                suggestedProductId.add(rs.getString("product_id"));
                suggestedProductPrice.add(rs.getDouble("price"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return productSuggestions;
    }

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

        matchedStates.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if(event.getCode() == KeyCode.ENTER){
                String selected = matchedStates.getSelectionModel().getSelectedItem();
                if (selected!=null){
                    textField.setText(selected);
                    popup.hide();
                }
            }
        });
    }
}
