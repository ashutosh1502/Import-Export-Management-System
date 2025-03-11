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
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

public class StockController {
    private TableView<Product> stocksTable;
    private int SrNoCounter;
    private TableColumn<Product,String> col1,col2,col3;
    private TableColumn<Product,Integer> col4;
    private TableColumn<Product,Double> col5;
    private Statement stmt;
    private String selectQuery="SELECT * FROM products";
    private ResultSet resultSet;
    private static Connection conn;

    @SuppressWarnings("unchecked")
    public TableView<Product> loadStocks(Connection connection){
        conn=connection;
//        System.out.println("Called stock controller");
        stocksTable = new TableView<>();
        stocksTable.setId("stocks-table");
        col1=new TableColumn<>("SrNo.");
        col2 = new TableColumn<>("Product ID");
        col3= new TableColumn<>("Product Name");
        col4= new TableColumn<>("Available Qty");
        col5= new TableColumn<>("Price");

        col1.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getSrNo()));
        col2.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getProductID()));
        col3.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getProductName()));
        col4.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getQuantity()).asObject());
        col5.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getPrice()).asObject());

        resizeColumns();
        stocksTable.getColumns().addAll(col1,col2,col3,col4,col5);
        loadData();

        return stocksTable;
    }

    public void loadData(){
        ObservableList<Product> data= FXCollections.observableArrayList();
        SrNoCounter=1;
        try{
            stmt=conn.createStatement();
            resultSet=stmt.executeQuery(selectQuery);
            while(resultSet.next()){
                String productID=resultSet.getString("product_id");
                String productName=resultSet.getString("product_name");
                int qty=resultSet.getInt("qty");
                double price=resultSet.getDouble("price");
                data.add(new Product(SrNoCounter++,productID,productName,qty,price));
            }
        }catch (SQLException s){
            System.out.println("SQL Exception: "+s.getMessage());
        }catch (Exception e){
            System.out.println("Unexpected Error: " + e.getMessage());
        }

        stocksTable.setItems(data);
    }

    public void resizeColumns(){
        stocksTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        col1.setPrefWidth(50);
        col1.setMinWidth(50);
        col1.setResizable(true);

        stocksTable.widthProperty().addListener((observable, oldWidth, newWidth) -> {
            double totalWidth = newWidth.doubleValue() - col1.getWidth();
            double remainingColumnCount = stocksTable.getColumns().size() - 1;

            if (remainingColumnCount > 0) {
                double columnWidth = totalWidth / remainingColumnCount;

                for (TableColumn<?, ?> col : stocksTable.getColumns()) {
                    if (col != col1) {
                        col.setPrefWidth(columnWidth);
                        col.setMinWidth(50);
                        col.setResizable(true);
                    }
                }
            }
        });
    }

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
                String insertQuery = "INSERT INTO products (product_id, product_name, qty, price) " +
                        "VALUES ('" + productId + "', '" + productName + "', " + quantity + ", " + price + ")";

                if (productId.isEmpty() || productName.isEmpty() || quantity < 0 || price < 0) {
                    AlertUtils.showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please provide valid details.");
                    return;
                }
                stmt.executeUpdate(insertQuery);
                AlertUtils.showMsg("Product added successfully");
                popupStage.close();
            } catch (NumberFormatException ex) {
                AlertUtils.showAlert(Alert.AlertType.ERROR, "Invalid Input", "Quantity and Price must be numeric.");
            } catch (SQLException ex) {
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
            String selectQuery="SELECT * FROM PRODUCTS WHERE product_id='"+selected.getProductID()+"'";
            try{
                ResultSet rs=stmt.executeQuery(selectQuery);
                rs.next();
                launchUpdateWindow(rs.getString("product_id"),rs.getString("product_name"),rs.getInt("qty"),rs.getDouble("price"));
            }catch (SQLException s){
                AlertUtils.showAlert(Alert.AlertType.ERROR,"Unable to update.","Something went wrong!");
                System.out.println("SQL Exception: "+s.getMessage()+"\nUnable to fetch data.");
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
                String updateQuery = "UPDATE PRODUCTS SET product_id='"+productId+"', product_name='"+productName+"', qty="+quantity+", price="+price+" WHERE product_id='"+selectedPrId+"'";

                if (productId.isEmpty() || productName.isEmpty() || quantity < 0 || price < 0) {
                    AlertUtils.showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please provide valid details.");
                    return;
                }
                stmt.executeUpdate(updateQuery);
                AlertUtils.showMsg("Product updated successfully");
                popupStage.close();
            } catch (NumberFormatException ex) {
                AlertUtils.showAlert(Alert.AlertType.ERROR, "Invalid Input", "Quantity and Price must be numeric.");
            } catch (SQLException ex) {
                AlertUtils.showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to update the product: " + ex.getMessage());
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
            String deleteQuery="DELETE FROM products WHERE product_id='"+selected.getProductID()+"'";
            try{
                Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmationAlert.setTitle("Delete Confirmation");
                confirmationAlert.setHeaderText("Delete Item");
                confirmationAlert.setContentText("Do you really want to delete this item?");
                ButtonType deleteButton = new ButtonType("Delete");
                ButtonType cancelButton = new ButtonType("Cancel");
                confirmationAlert.getButtonTypes().setAll(deleteButton, cancelButton);

                Optional<ButtonType> result = confirmationAlert.showAndWait();
                if (result.isPresent() && result.get() == deleteButton) {
                    stmt.executeUpdate(deleteQuery);
                    AlertUtils.showMsg("Item deleted successfully");
                    System.out.println("Item deleted successfully.");
                }
            }catch (SQLException s){
                AlertUtils.showAlert(Alert.AlertType.ERROR,"Unable to delete.","Something went wrong!");
                System.out.println("SQL Exception: "+s.getMessage()+"\nUnable to delete data.");
            }
        }else{
            AlertUtils.showAlert(Alert.AlertType.INFORMATION,"Unable to delete.","Please select a row to delete!");
        }
    }
}
