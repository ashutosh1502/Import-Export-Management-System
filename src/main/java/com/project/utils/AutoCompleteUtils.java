package com.project.utils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

public class AutoCompleteUtils {
    private final ObservableList<String> productSuggestions = FXCollections.observableArrayList();
    private final ArrayList<String> suggestedProductId = new ArrayList<>();
    private final ArrayList<Double> suggestedProductPrice = new ArrayList<>();

    public void setupAutoCompleteProductName(Connection conn, TextField txtProductName,TextField txtProductId,TextField txtProductPrice, ListView<String> suggestionList) {
        suggestionList.setVisible(false);
        suggestionList.setMaxHeight(100); // Set max height for dropdown

        txtProductName.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            String input = txtProductName.getText().trim();
//            System.out.println("Input: "+input);
            if (!input.isEmpty()) {
                fetchMatchingProducts(conn, input,suggestionList);
                suggestionList.setVisible(!productSuggestions.isEmpty());
            } else {
                suggestionList.setVisible(false);
            }
        });

        // Handle item selection from the ListView
        suggestionList.setOnMouseClicked(event -> {
            String selectedProduct = suggestionList.getSelectionModel().getSelectedItem();
            String selectedProductId = suggestedProductId.get(suggestionList.getSelectionModel().getSelectedIndex());
            double selectedProductPrice = suggestedProductPrice.get(suggestionList.getSelectionModel().getSelectedIndex());
            if (selectedProduct != null) {
                txtProductName.clear();
                txtProductName.setText(selectedProduct);
                txtProductId.clear();
                txtProductId.setText(selectedProductId);
                txtProductPrice.clear();
                txtProductPrice.setText(Double.toString(selectedProductPrice));
                suggestionList.setVisible(false);
            }
        });
        suggestionList.addEventHandler(KeyEvent.KEY_PRESSED,event ->{
            if (event.getCode() == KeyCode.ENTER) {
                String selectedProduct = suggestionList.getSelectionModel().getSelectedItem();
                String selectedProductId = suggestedProductId.get(suggestionList.getSelectionModel().getSelectedIndex());
                double selectedProductPrice = suggestedProductPrice.get(suggestionList.getSelectionModel().getSelectedIndex());
                if (selectedProduct != null) {
                    txtProductName.clear();
                    txtProductName.setText(selectedProduct);
                    txtProductId.clear();
                    txtProductId.setText(selectedProductId);
                    txtProductPrice.clear();
                    txtProductPrice.setText(Double.toString(selectedProductPrice));
                    suggestionList.setVisible(false);
                }
            }
        });
    }

    private void fetchMatchingProducts(Connection conn, String input, ListView<String> suggestionList) {
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

            suggestionList.setItems(productSuggestions);
//            for(String i : suggestionList.getItems())
//                System.out.println("SL: "+i);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
