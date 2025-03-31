package com.project.application;

import com.project.utils.AlertUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.*;
import java.util.Optional;

public class StockController {

    private TableView<Product> stocksTable;
    private static final String FETCH_PRODUCTS_QUERY ="SELECT * FROM PRODUCTS";
    private static final String INSERT_PRODUCT_QUERY = "INSERT INTO PRODUCTS (product_id, product_name, qty, price) VALUES (?, ?, ?, ?)";
    private static final String FETCH_PRODUCT_BY_ID_QUERY="SELECT * FROM PRODUCTS WHERE product_id= ?";
    private static final String UPDATE_PRODUCT_QUERY = "UPDATE PRODUCTS SET product_id= ?, product_name= ?, qty= ?, price= ? WHERE product_id= ?";
    private static final String DELETE_PRODUCT_QUERY="DELETE FROM PRODUCTS WHERE product_id= ?";
    private static Connection conn;

    public TableView<Product> initializeStocksTable(Connection connection){
        conn=connection;

        stocksTable = new TableView<>();
        initializeStocksTableColumns();
        stocksTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        loadStockData();

        return stocksTable;
    }

    private void initializeStocksTableColumns(){
        TableColumn<Product, String> colSrNo = new TableColumn<>("SrNo.");
        TableColumn<Product, String> colProductId = new TableColumn<>("Product ID");
        TableColumn<Product, String> colProductName = new TableColumn<>("Product Name");
        TableColumn<Product, Integer> colQty = new TableColumn<>("Available Qty");
        TableColumn<Product, Double> colPrice = new TableColumn<>("Price");

        colSrNo.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getSrNo()));
        colProductId.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getProductID()));
        colProductName.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getProductName()));
        colQty.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getQuantity()).asObject());
        colPrice.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getPrice()).asObject());

        stocksTable.getColumns().addAll(colSrNo, colProductId, colProductName, colQty, colPrice);

    }

    public void loadStockData(){
        ObservableList<Product> data= FXCollections.observableArrayList();
        int srNoCounter = 1;
        try{
            PreparedStatement fetchProductsStmt=conn.prepareStatement(FETCH_PRODUCTS_QUERY);
            ResultSet resultSet = fetchProductsStmt.executeQuery();
            while(resultSet.next()){
                String productID= resultSet.getString("product_id");
                String productName= resultSet.getString("product_name");
                int qty= resultSet.getInt("qty");
                double price= resultSet.getDouble("price");
                data.add(new Product(srNoCounter++,productID,productName,qty,price));
            }
        }catch (SQLException s){
            System.out.println("SQL Exception: "+s.getMessage());
        }catch (Exception e){
            System.out.println("Unexpected Error: " + e.getMessage());
        }

        stocksTable.setItems(data);
    }

//    public void resizeColumns(){
//        stocksTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
//        colSrNo.setPrefWidth(50);
//        colSrNo.setMinWidth(50);
//        colSrNo.setResizable(true);
//
//        stocksTable.widthProperty().addListener((observable, oldWidth, newWidth) -> {
//            double totalWidth = newWidth.doubleValue() - colSrNo.getWidth();
//            double remainingColumnCount = stocksTable.getColumns().size() - 1;
//
//            if (remainingColumnCount > 0) {
//                double columnWidth = totalWidth / remainingColumnCount;
//
//                for (TableColumn<?, ?> col : stocksTable.getColumns()) {
//                    if (col != colSrNo) {
//                        col.setPrefWidth(columnWidth);
//                        col.setMinWidth(50);
//                        col.setResizable(true);
//                    }
//                }
//            }
//        });
//    }

    public void addStock() {
        Stage popupStage = new Stage();
        popupStage.setTitle("Add new stock");

        VBox formLayout = new VBox(10);
        formLayout.setPadding(new Insets(20));

        Label lblProductId = new Label("Product ID:");
        TextField txtProductId = new TextField();

        Label lblProductName = new Label("Product Name:");
        TextField txtProductName = new TextField();

        Label lblQty = new Label("Quantity:");
        TextField txtQty = new TextField();

        Label lblPrice = new Label("Price:");
        TextField txtPrice = new TextField();

        Button btnSubmit = new Button("Submit");
        Button btnCancel = new Button("Cancel");

        HBox buttonLayout = new HBox(10, btnSubmit, btnCancel);

        formLayout.getChildren().addAll(lblProductId, txtProductId, lblProductName, txtProductName,
                lblQty, txtQty, lblPrice, txtPrice, buttonLayout);

        btnCancel.setOnAction(e -> popupStage.close());

        btnSubmit.setOnAction(e -> {
            try {
                String productId = txtProductId.getText();
                String productName = txtProductName.getText();
                int quantity = Integer.parseInt(txtQty.getText());
                double price = Double.parseDouble(txtPrice.getText());

                if (productId.isEmpty() || productName.isEmpty() || quantity < 0 || price < 0) {
                    AlertUtils.showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please provide valid details.");
                    return;
                }
                PreparedStatement insertProductStmt = conn.prepareStatement(INSERT_PRODUCT_QUERY);
                insertProductStmt.setString(1,productId);
                insertProductStmt.setString(2,productName);
                insertProductStmt.setInt(3,quantity);
                insertProductStmt.setDouble(4,price);
                insertProductStmt.executeUpdate();
                AlertUtils.showMsg("Product added successfully");
                popupStage.close();
            } catch (NumberFormatException ex) {
                AlertUtils.showAlert(Alert.AlertType.ERROR, "Invalid Input", "Quantity and Price must be numeric.");
            } catch (SQLException ex) {
                DatabaseErrorHandler.handleDatabaseError(ex);
                AlertUtils.showAlert(Alert.AlertType.ERROR, "Database Error", "Product ID already exists!");
            }
        });

        Scene popupScene = new Scene(formLayout, 300, 400);
        popupStage.setScene(popupScene);
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.showAndWait();
    }

    public void updateStock(){
        Product selected=stocksTable.getSelectionModel().getSelectedItem();
        if(selected!=null){
            try{
                PreparedStatement fetchProductByIdStmt = conn.prepareStatement(FETCH_PRODUCT_BY_ID_QUERY);
                fetchProductByIdStmt.setString(1,selected.getProductID());
                ResultSet rs=fetchProductByIdStmt.executeQuery();
                if(rs.next()){
                    launchUpdateWindow(rs.getString("product_id"),rs.getString("product_name"),rs.getInt("qty"),rs.getDouble("price"));
                }else{
                    AlertUtils.showAlert(Alert.AlertType.ERROR,"Something went wrong","Stock doesn't exists into database!");
                }
            }catch (SQLException s){
                DatabaseErrorHandler.handleDatabaseError(s);
                AlertUtils.showAlert(Alert.AlertType.ERROR,"Something went wrong","Unable to update!");
            }
        }else{
            AlertUtils.showAlert(Alert.AlertType.INFORMATION,"Unable to update.","Please select a row to update!");
        }
    }

    public void launchUpdateWindow(String selectedPrId,String selectedPrName,int selectedQty,double selectedPrice){
        Stage popupStage = new Stage();
        popupStage.setTitle("Update stock");
        VBox formLayout = new VBox(10);
        formLayout.setPadding(new Insets(20));

        Label lblProductId = new Label("Product ID:");
        TextField txtProductId = new TextField(selectedPrId);

        Label lblProductName = new Label("Product Name:");
        TextField txtProductName = new TextField(selectedPrName);

        Label lblQty = new Label("Quantity:");
        TextField txtQty = new TextField(Integer.toString(selectedQty));

        Label lblPrice = new Label("Price:");
        TextField txtPrice = new TextField(Double.toString(selectedPrice));

        Button btnUpdate = new Button("Update");
        Button btnCancel = new Button("Cancel");

        HBox buttonLayout = new HBox(10, btnUpdate, btnCancel);

        formLayout.getChildren().addAll(lblProductId, txtProductId, lblProductName, txtProductName,
                lblQty, txtQty, lblPrice, txtPrice, buttonLayout);

        btnCancel.setOnAction(e -> popupStage.close());

        btnUpdate.setOnAction(e -> {
            try {
                String productId = txtProductId.getText();
                String productName = txtProductName.getText();
                int quantity = Integer.parseInt(txtQty.getText());
                double price = Double.parseDouble(txtPrice.getText());

                if (productId.isEmpty() || productName.isEmpty() || quantity < 0 || price < 0) {
                    AlertUtils.showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please provide valid details.");
                    return;
                }

                PreparedStatement updateProductStmt = conn.prepareStatement(UPDATE_PRODUCT_QUERY);
                updateProductStmt.setString(1,productId);
                updateProductStmt.setString(2,productName);
                updateProductStmt.setInt(3,quantity);
                updateProductStmt.setDouble(4,price);
                updateProductStmt.setString(5,selectedPrId);
                updateProductStmt.executeUpdate();
                AlertUtils.showMsg("Product updated successfully!");
                popupStage.close();
            } catch (NumberFormatException ex) {
                AlertUtils.showAlert(Alert.AlertType.ERROR, "Invalid Input", "Quantity and Price must be numeric.");
            } catch (SQLException ex) {
                DatabaseErrorHandler.handleDatabaseError(ex);
                AlertUtils.showAlert(Alert.AlertType.ERROR, "Something went wrong", "Failed to update the product!");
            }
        });

        Scene popupScene = new Scene(formLayout, 300, 400);
        popupStage.setScene(popupScene);
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.showAndWait();
    }

    public void deleteStock(){
        Product selected=stocksTable.getSelectionModel().getSelectedItem();
        if(selected!=null){
            try{
                Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmationAlert.setTitle("Delete Confirmation");
                confirmationAlert.setHeaderText("Delete Product");
                confirmationAlert.setContentText("Do you really want to delete this product?");
                ButtonType deleteButton = new ButtonType("Delete");
                ButtonType cancelButton = new ButtonType("Cancel");
                confirmationAlert.getButtonTypes().setAll(deleteButton, cancelButton);

                Optional<ButtonType> result = confirmationAlert.showAndWait();
                if (result.isPresent() && result.get() == deleteButton) {
                    PreparedStatement deleteStockStmt = conn.prepareStatement(DELETE_PRODUCT_QUERY);
                    deleteStockStmt.setString(1,selected.getProductID());
                    deleteStockStmt.executeUpdate();
                    AlertUtils.showMsg("Product deleted successfully");
                }else{
                    AlertUtils.showAlert(Alert.AlertType.INFORMATION,"Deletion Failed","Unable to delete the product!");
                }
            }catch (SQLException s){
                AlertUtils.showAlert(Alert.AlertType.ERROR,"Something went wrong","Unable to delete!");
            }
        }else{
            AlertUtils.showAlert(Alert.AlertType.INFORMATION,"Unable to delete.","Please select a row to delete!");
        }
    }
}
