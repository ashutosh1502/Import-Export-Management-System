package com.project.application;

import com.project.utils.AlertUtils;
import com.project.utils.AutoCompleteUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.sql.*;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;

public class ImportController {
    private static TableView<Imports> importsTable;
    private TableView<Product> tblProducts;
    private TableColumn<Imports, String> col1, col2;
    private static final ObservableList<String> productNames = FXCollections.observableArrayList();
    private static Connection conn;
    private int srno = 1;
    private Label txtSubTotal;

    @SuppressWarnings("unchecked")
    public TableView<Imports> loadHistory(Connection connection) {
        conn = connection;
//        System.out.println("Called import controller:loadHistory");
        importsTable = new TableView<>();
        col1 = new TableColumn<>("SrNo.");
        col2 = new TableColumn<>("Invoice No.");
        TableColumn<Imports, String> col3 = new TableColumn<>("Supplier Name");
        TableColumn<Imports, String> col4 = new TableColumn<>("Products");
        TableColumn<Imports, Integer> col5 = new TableColumn<>("Total Qty");
        TableColumn<Imports, String> col6 = new TableColumn<>("Amount");
        TableColumn<Imports, String> col7 = new TableColumn<>("Payment Status");
        TableColumn<Imports, String> col8 = new TableColumn<>("Invoice Date");
        col1.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getSrNo()));
        col2.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getInvoiceNo()));
        col3.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getSupplierName()));
        col4.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(String.join(", ", cellData.getValue().getProducts())));
        col5.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getTotalQuantity()).asObject());
        DecimalFormat df = new DecimalFormat("##,##,###");
        col6.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty("Rs. " + df.format(cellData.getValue().getSubTotal())));
        col7.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getPaymentStatus()));
        col8.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getInvoiceDate()));
        importsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        try {
            importsTable.getColumns().addAll(col1, col2, col3, col4, col5, col6, col7, col8);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        loadImportsData();
//        System.out.println(importsTable);   //debug-test.
        return importsTable;
    }

    public void loadImportsData() {
//        System.out.println("called loadImportsData in ImportController.java."); //debug-test.
        String query = "SELECT * FROM IMPORTS";

        importsTable.getItems().clear(); // Clear existing items before loading new data

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            int srNo = 1;
            while (rs.next()) {
                String supplierName = rs.getString("supplier_name");
                String supplierId = rs.getString("supplier_id");
                String address = rs.getString("address");
                String city = rs.getString("city");
                String state = rs.getString("state");
                String phno = rs.getString("phone_number");
                String email = rs.getString("email");
                String invoiceNo = rs.getString("invoice_number");
                String orderDate = processDateString(rs.getString("order_date"));
                String invoiceDate = processDateString(rs.getString("invoice_date"));
                double subTotal = rs.getDouble("sub_total");
                String paymentMode = rs.getString("payment_mode");
                String paymentStatus = rs.getString("payment_status");

                //Fetching products related to specific invoice number...
                String importProductQuery = "SELECT * FROM IMPORT_PRODUCTS WHERE INVOICE_NUMBER = '" + invoiceNo + "'";
                Statement stmt2 = conn.createStatement();
                ResultSet iprs = stmt2.executeQuery(importProductQuery);
                ArrayList<String> productList = new ArrayList<>();
                int totalQty = 0;
                while (iprs.next()) {
                    productList.add(iprs.getString("product_name"));
                    totalQty += iprs.getInt("quantity");
                }

                Imports importItem = new Imports(srNo++, invoiceNo, supplierId, supplierName, productList, totalQty, subTotal, address, city, state, phno, email, orderDate, invoiceDate, paymentMode, paymentStatus);
//                System.out.println("item added");   //debug-test.
                importsTable.getItems().add(importItem);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<String> getProductsList(Array productsArray) {
        ArrayList<String> productsList = new ArrayList<>();
        try {
            Object[] products = (Object[]) productsArray.getArray();
            for (Object productObj : products) {
                Struct productStruct = (Struct) productObj;
                Object[] productAttributes = productStruct.getAttributes();
                String productName = (String) productAttributes[1];
                productsList.add(productName);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
//        System.out.println(productsList);   //test.
        return productsList;
    }

    public int getTotalProductQty(Array productsArray) {
        int totalQuantity = 0;
        try {
            Object[] products = (Object[]) productsArray.getArray();
            for (Object productObj : products) {
                Struct productStruct = (Struct) productObj;
                Object[] productAttributes = productStruct.getAttributes();
                totalQuantity += ((Number) productAttributes[2]).intValue();
            }
        } catch (SQLException e) {
            System.out.println("Error calculating total quantity: " + e.getMessage());
        }
        return totalQuantity;
    }

    //OPERATIONS BUTTON ACTIONS--------------------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public void addEntry(ScrollPane scrollPane) {
        Stage popupStage = new Stage();
        popupStage.setTitle("Add Entry");

        GridPane formLayout = new GridPane();
        formLayout.setPadding(new Insets(20));
        formLayout.setHgap(10);
        formLayout.setVgap(10);

        Label supplierDetails = new Label("Supplier Details:");
        supplierDetails.setStyle("-fx-font-weight:bold;");
        Label supplierName = new Label("Supplier Name:");
        TextField txtSupplierName = new TextField();

        Label supplierId = new Label("Supplier ID:");
        TextField txtSupplierId = new TextField();

        Label address = new Label("Address:");
        TextField txtAddress = new TextField();

        Label city = new Label("City:");
        TextField txtCity = new TextField();

        Label state = new Label("State:");
        TextField txtState = new TextField();

        Label phone = new Label("Phone:");
        TextField txtPhone = new TextField();

        Label email = new Label("Email:");
        TextField txtEmail = new TextField();
        txtEmail.setMinWidth(200);

        tblProducts = new TableView<>();
        TableColumn<Product, String> col1, col2, col3;
        TableColumn<Product, Integer> col4;
        TableColumn<Product, Double> col5;
        col1 = new TableColumn<>("SrNo.");
        col1.setPrefWidth(40);
        col2 = new TableColumn<>("Product ID");
        col3 = new TableColumn<>("Product Name");
        col3.setPrefWidth(120);
        col4 = new TableColumn<>("Qty");
        col5 = new TableColumn<>("Price");
        col1.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getSrNo()));
        col2.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getProductID()));
        col3.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getProductName()));
        col3.setMinWidth(60);
        col3.setPrefWidth(80);
        col4.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getQuantity()).asObject());
        col5.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getPrice()).asObject());
        tblProducts.getColumns().addAll(col1, col2, col3, col4, col5);
        tblProducts.setPrefWidth(400);
        tblProducts.setMaxHeight(250);

        Label lblInvoiceNumber = new Label("Invoice Number:");
        TextField txtInvoiceNumber = new TextField();
        int invoiceNum = 1;
        if (importsTable != null && importsTable.getItems() != null) {
            invoiceNum = importsTable.getItems().size() + 1;
        }
        txtInvoiceNumber.setText(String.format("%04d", invoiceNum));

        Label lblOrderDate = new Label("Order Date:");
        DatePicker dpOrderDate = new DatePicker(java.time.LocalDate.now());

        Label lblInvoiceDate = new Label("Invoice Date:");
        DatePicker dpInvoiceDate = new DatePicker(java.time.LocalDate.now());

        Button addProduct = new Button("Add Product");
        Button updateProduct = new Button("Update Product");
        Button deleteProduct = new Button("Delete Product");
        addProduct.setMinWidth(100);
        addProduct.setOnAction(e -> addProductInEntry());
        updateProduct.setMinWidth(100);
        updateProduct.setOnAction(e -> updateProductInEntry(txtInvoiceNumber.getText()));
        deleteProduct.setMinWidth(100);
        deleteProduct.setOnAction(e -> deleteProductFromEntry());
        VBox btnVBox = new VBox(5);
        btnVBox.getChildren().addAll(addProduct, updateProduct, deleteProduct);

        Label paymentDetails = new Label("Payment Details:");
        paymentDetails.setStyle("-fx-font-weight:bold;");
        Label subTotal = new Label("Sub Total:");
        txtSubTotal = new Label("0.00");

        Label paymentMode = new Label("Payment Mode:");
        ComboBox<String> payment = new ComboBox<>();
        payment.getItems().addAll("Cash", "Cheque", "Net-Banking", "Credit Card");
        payment.setValue("Cash");

        Label status = new Label("Status:");
        RadioButton paid = new RadioButton("Paid");
        RadioButton pending = new RadioButton("Pending");
        ToggleGroup group = new ToggleGroup();
        paid.setToggleGroup(group);
        pending.setToggleGroup(group);
        HBox statusLayout = new HBox(5);
        statusLayout.getChildren().addAll(paid, pending);

        Button btnSubmit = new Button("Submit");
//        System.out.println("dpOrderDate: "+dpOrderDate);
//        System.out.println("dpOrderDate.getValue(): "+dpOrderDate.getValue());
//        System.out.println("java util date: "+Date.from(dpOrderDate.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()));

        btnSubmit.setOnAction(e -> {
            try {
//                 Prepare SQL INSERT query
                String insertQuery = "INSERT INTO imports (" +
                        "supplier_name, supplier_id, address, city, state, phone_number, email, invoice_number, order_date, invoice_date, sub_total, payment_mode, payment_status" +
                        ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, TO_DATE(?, 'YYYY-MM-DD'), TO_DATE(?, 'YYYY-MM-DD'), ?, ?, ?)";
                String insertProductQuery = "INSERT INTO import_products (invoice_number,product_name,product_id,quantity,price) VALUES (?,?,?,?,?)";
                // Retrieve values from the form
                String supplierIdEntered = txtSupplierId.getText();
                String supplierNameEntered = txtSupplierName.getText();
                String addressEntered = txtAddress.getText();
                String cityEntered = txtCity.getText();
                String stateEntered = txtState.getText();
                String phoneEntered = txtPhone.getText();
                String emailEntered = txtEmail.getText();
                double subTotalEntered = Double.parseDouble(txtSubTotal.getText());
                String paymentModeEntered = payment.getValue();
                String paymentStatusEntered = paid.isSelected() ? "Paid" : "Pending";
                String invoiceNumberEntered = txtInvoiceNumber.getText();
                String orderDateEntered = dpOrderDate.getValue().toString();
                String invoiceDateEntered = dpInvoiceDate.getValue().toString();
                try {
                    PreparedStatement preparedStatementImports = conn.prepareStatement(insertQuery);
                    preparedStatementImports.setString(1, supplierNameEntered);
                    preparedStatementImports.setString(2, supplierIdEntered);
                    preparedStatementImports.setString(3, addressEntered);
                    preparedStatementImports.setString(4, cityEntered);
                    preparedStatementImports.setString(5, stateEntered);
                    preparedStatementImports.setString(6, phoneEntered);
                    preparedStatementImports.setString(7, emailEntered);
                    preparedStatementImports.setString(8, invoiceNumberEntered);
                    preparedStatementImports.setString(9, orderDateEntered);
                    preparedStatementImports.setString(10, invoiceDateEntered);
                    preparedStatementImports.setDouble(11, subTotalEntered);
                    preparedStatementImports.setString(12, paymentModeEntered);
                    preparedStatementImports.setString(13, paymentStatusEntered);
                    int rowsAffected = preparedStatementImports.executeUpdate();
                    if (rowsAffected > 0) {
                        for (Product product : tblProducts.getItems()) {
                            PreparedStatement preparedStatementProducts = conn.prepareStatement(insertProductQuery);
                            preparedStatementProducts.setString(1, invoiceNumberEntered);
                            preparedStatementProducts.setString(2, product.getProductName());
                            preparedStatementProducts.setString(3, product.getProductID());
                            preparedStatementProducts.setInt(4, product.getQuantity());
                            preparedStatementProducts.setDouble(5, product.getPrice());
                            rowsAffected = 0;
                            rowsAffected = preparedStatementProducts.executeUpdate();
                            if (!(rowsAffected > 0)) {
                                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                alert.setTitle("Failed");
                                alert.setHeaderText(null);
                                alert.setContentText("Failed to add products!");
                                alert.showAndWait();
                                // Reload the data (refresh table)
                                scrollPane.setContent(loadHistory(conn));
                                return;
                            }
                        }
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Success");
                        alert.setHeaderText(null);
                        alert.setContentText("Entry added successfully!");
                        alert.showAndWait();
                        updateStocks();
                        // Reload the data (refresh table)
                        scrollPane.setContent(loadHistory(conn));
                    }
                } catch (Exception sqlException) {
                    System.out.println(sqlException);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Failed to add entry: " + ex.getMessage());
                alert.showAndWait();
            }
        });

        Button btnCancel = new Button("Cancel");
        btnSubmit.setMinHeight(30);
        btnSubmit.setMinWidth(100);
        btnCancel.setMinHeight(30);
        btnCancel.setMinWidth(100);

        formLayout.add(supplierDetails, 0, 0);
        formLayout.add(supplierName, 0, 1);
        formLayout.add(txtSupplierName, 1, 1);
        formLayout.add(supplierId, 2, 1);
        formLayout.add(txtSupplierId, 3, 1);
        formLayout.add(new Label("                                                                             "), 4, 1);
        formLayout.add(lblInvoiceNumber, 5, 1);
        formLayout.add(txtInvoiceNumber, 6, 1);
        formLayout.add(address, 0, 2);
        formLayout.add(txtAddress, 1, 2);
        formLayout.add(city, 2, 2);
        formLayout.add(txtCity, 3, 2);
        formLayout.add(new Label(), 4, 2);
        formLayout.add(lblOrderDate, 5, 2);
        formLayout.add(dpOrderDate, 6, 2);
        formLayout.add(state, 0, 3);
        formLayout.add(txtState, 1, 3);
        formLayout.add(lblInvoiceDate, 5, 3);
        formLayout.add(dpInvoiceDate, 6, 3);
        formLayout.add(phone, 0, 4);
        formLayout.add(txtPhone, 1, 4);
        formLayout.add(email, 2, 4);
        formLayout.add(txtEmail, 3, 4);
        formLayout.add(btnVBox, 0, 6);
        formLayout.add(tblProducts, 1, 6);
        formLayout.add(paymentDetails, 0, 7);
        formLayout.add(subTotal, 0, 8);
        formLayout.add(txtSubTotal, 1, 8);
        formLayout.add(paymentMode, 0, 9);
        formLayout.add(payment, 1, 9);
        formLayout.add(status, 0, 10);
        formLayout.add(statusLayout, 1, 10);

        formLayout.setPrefSize(300, 400);

        VBox vBox = new VBox(10);
        HBox hBox = new HBox(10);
        hBox.setStyle("-fx-border-color: gray;-fx-border-width: 1 0 0 0;");
        hBox.getChildren().addAll(btnSubmit, btnCancel);
        hBox.setPadding(new Insets(10));
        VBox.setVgrow(formLayout, javafx.scene.layout.Priority.ALWAYS);
        vBox.getChildren().addAll(formLayout, hBox);

        scrollPane.setContent(vBox);

        btnCancel.setOnAction(e -> scrollPane.setContent(loadHistory(conn)));
    }

    public void addProductInEntry() {
        Stage popupStage = new Stage();
        popupStage.setTitle("Add Product");
        popupStage.initModality(Modality.APPLICATION_MODAL);

        GridPane productForm = new GridPane();
        productForm.setPadding(new Insets(20));
        productForm.setHgap(10);
        productForm.setVgap(10);

        Label lblProductName = new Label("Product Name:");
        TextField txtProductName = new TextField();
        ListView<String> suggestionList = new ListView<>();
        VBox prBox = new VBox();
        VBox.setMargin(txtProductName, new Insets(20, 0, 0, 0));
        prBox.getChildren().addAll(txtProductName, suggestionList);

        Label lblProductId = new Label("Product ID:");
        TextField txtProductId = new TextField();

        Label lblPrice = new Label("Price:");
        TextField txtPrice = new TextField();
        AutoCompleteUtils autoCompleteUtils = new AutoCompleteUtils();
        autoCompleteUtils.setupAutoCompleteProductName(conn, txtProductName, txtProductId, txtPrice, suggestionList);

        Label lblQuantity = new Label("Quantity:");
        TextField txtQuantity = new TextField();

        Button btnAddItem = new Button("Add item to Entry");
        Button btnClose = new Button("Close");

        btnAddItem.setOnAction(e -> {
            String productName = txtProductName.getText();
            String productId = txtProductId.getText();
            String quantityText = txtQuantity.getText();
            String priceText = txtPrice.getText();

            if (productName.isEmpty() || productId.isEmpty() || quantityText.isEmpty() || priceText.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "All fields must be filled!", ButtonType.OK);
                alert.showAndWait();
                return;
            }

            try {
                int quantity = Integer.parseInt(quantityText);
                float price = Float.parseFloat(priceText);

                Product newProduct = new Product(srno, productId, productName, quantity, price);
                tblProducts.getItems().add(newProduct);
//                System.out.println("Product added: " + newProduct);
                srno++;
                AlertUtils.showMsg("Product added successfully!");
                calculateSubTotal();
                txtProductName.clear();
                txtProductId.clear();
                txtQuantity.clear();
                txtPrice.clear();

            } catch (NumberFormatException ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Quantity and Price must be valid numbers!", ButtonType.OK);
                alert.showAndWait();
            }
        });

        productForm.add(lblProductName, 0, 0);
        productForm.add(prBox, 1, 0);
        productForm.add(lblProductId, 0, 1);
        productForm.add(txtProductId, 1, 1);
        productForm.add(lblPrice, 0, 2);
        productForm.add(txtPrice, 1, 2);
        productForm.add(lblQuantity, 0, 3);
        productForm.add(txtQuantity, 1, 3);
        productForm.add(btnAddItem, 0, 4);
        productForm.add(btnClose, 1, 4);

        Scene scene = new Scene(productForm, 350, 350);
        popupStage.setScene(scene);


        btnClose.setOnAction(e -> popupStage.close());
        popupStage.show();
    }

    public void updateProductInEntry(String invoiceNumber) {
        Product selected = tblProducts.getSelectionModel().getSelectedItem();
        if (selected != null) {
            launchEntryProductUpdateWindow(selected.getSrNo(), selected.getProductID(), selected.getProductName(), selected.getQuantity(), selected.getPrice(), invoiceNumber);
        } else {
            AlertUtils.showAlert(Alert.AlertType.INFORMATION, "Unable to update.", "Please select a row to update!");
        }
    }

    public void launchEntryProductUpdateWindow(String selectedSrRo, String selectedPrId, String selectedPrName, int selectedQty, double selectedPrice, String invoiceNumber) {
        Stage popupStage = new Stage();
        popupStage.setTitle("Update Product");
        popupStage.initModality(Modality.APPLICATION_MODAL);

        GridPane productForm = new GridPane();
        productForm.setPadding(new Insets(20));
        productForm.setHgap(10);
        productForm.setVgap(10);

        Label lblProductName = new Label("Product Name:");
        TextField txtProductName = new TextField(selectedPrName);

        Label lblProductId = new Label("Product ID:");
        TextField txtProductId = new TextField(selectedPrId);

        Label lblQuantity = new Label("Quantity:");
        TextField txtQuantity = new TextField(Integer.toString(selectedQty));

        Label lblPrice = new Label("Price:");
        TextField txtPrice = new TextField(Double.toString(selectedPrice));

        Button btnUpdateItem = new Button("Update Item");
        Button btnClose = new Button("Close");

        btnUpdateItem.setOnAction(e -> {
            String productName = txtProductName.getText();
            String productId = txtProductId.getText();
            String quantityText = txtQuantity.getText();
            String priceText = txtPrice.getText();

            if (productName.isEmpty() || productId.isEmpty() || quantityText.isEmpty() || priceText.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "All fields must be filled!", ButtonType.OK);
                alert.showAndWait();
                return;
            }

            try {
                Product updatedProduct = null;
                int quantity = 0;
                double price = 0.0;
                try {
                    quantity = Integer.parseInt(quantityText);
                    price = Double.parseDouble(priceText);
                    updatedProduct = new Product(Integer.parseInt(selectedSrRo.substring(0, selectedSrRo.length() - 1)), productId, productName, quantity, price);
                } catch (NumberFormatException nfe) {
                    nfe.printStackTrace();
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Quantity and Price must be valid numbers!", ButtonType.OK);
                    alert.showAndWait();
                }
                String updateProductQuery = "UPDATE import_products SET product_name = ?, product_id = ?, quantity = ?, price = ? WHERE invoice_number = ?";
                PreparedStatement updateProductStmt = conn.prepareStatement(updateProductQuery);
                updateProductStmt.setString(1, productName);
                updateProductStmt.setString(2, productId);
                updateProductStmt.setInt(3, quantity);
                updateProductStmt.setDouble(4, price);
                updateProductStmt.setString(5, invoiceNumber);
                int rowsAffected = updateProductStmt.executeUpdate();
                if (rowsAffected > 0) {
                    int selectedIndex = tblProducts.getSelectionModel().getSelectedIndex();
                    tblProducts.getItems().set(selectedIndex, updatedProduct);
                    AlertUtils.showMsg("Product updated successfully!");
                    calculateSubTotal();
                    popupStage.close();
                } else {
                    AlertUtils.showMsg("Failed to update product!");
                    calculateSubTotal();
                    popupStage.close();
                }
            } catch (Exception ex) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Something went wrong!", ButtonType.OK);
                alert.showAndWait();
            }
        });

        productForm.add(lblProductName, 0, 0);
        productForm.add(txtProductName, 1, 0);
        productForm.add(lblProductId, 0, 1);
        productForm.add(txtProductId, 1, 1);
        productForm.add(lblPrice, 0, 2);
        productForm.add(txtPrice, 1, 2);
        productForm.add(lblQuantity, 0, 3);
        productForm.add(txtQuantity, 1, 3);
        productForm.add(btnUpdateItem, 0, 4);
        productForm.add(btnClose, 1, 4);

        Scene scene = new Scene(productForm, 350, 250);
        popupStage.setScene(scene);

        btnClose.setOnAction(e -> popupStage.close());
        popupStage.show();
    }

    public void viewUpdateEntry(ScrollPane scrollPane) {
        System.out.println(importsTable);   //debug-test
        if (importsTable == null || importsTable.getItems().isEmpty() || importsTable.getSelectionModel().getSelectedItem() == null) {
            AlertUtils.showAlert(Alert.AlertType.INFORMATION, "Unable to update.", "Please select a row to update!");
            return;
        }

        String invoiceNumber = importsTable.getSelectionModel().getSelectedItem().getInvoiceNo();
//        System.out.println(invoiceNumber);  //debug-test.
        try {
            String query = "SELECT * FROM IMPORTS WHERE invoice_number = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, invoiceNumber);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String supplierId = rs.getString("supplier_id");
                String supplierName = rs.getString("supplier_name");
                String address = rs.getString("address");
                String city = rs.getString("city");
                String state = rs.getString("state");
                String phone = rs.getString("phone_number");
                String email = rs.getString("email");
                String orderDate = processDateString(rs.getString("order_date"));
                String invoiceDate = processDateString(rs.getString("invoice_date"));
                String subTotal = rs.getString("sub_total");
                String paymentMode = rs.getString("payment_mode");
                String status = rs.getString("payment_status");

                // fetch product data .........
                ArrayList<Product> productList = new ArrayList<>();
                try {
                    String productsQuery = "SELECT * FROM IMPORT_PRODUCTS WHERE invoice_number = ?";
                    PreparedStatement prstmt = conn.prepareStatement(productsQuery);
                    prstmt.setString(1, invoiceNumber);
                    ResultSet prrs = prstmt.executeQuery();
                    int srno = 1;
                    while (prrs.next()) {
                        String productName = prrs.getString("product_name");
                        String productId = prrs.getString("product_id");
                        int qty = prrs.getInt("quantity");
                        double price = prrs.getDouble("price");
                        productList.add(new Product(srno, productId, productName, qty, price));
                        srno += 1;
                    }
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }

                // Pre-fill the form and allow updates
                updateEntryForm(supplierId, supplierName, address, city, state, phone, email, orderDate, invoiceDate, subTotal, paymentMode, status, productList, invoiceNumber, scrollPane);
            } else {
                AlertUtils.showAlert(Alert.AlertType.INFORMATION, "Not Found", "The selected invoice does not exist in the database.");
            }

        } catch (SQLException e) {
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Error", "Error retrieving entry details: " + e.getMessage() + "\n" + e.getCause());
        }
    }

    @SuppressWarnings("unchecked")
    private void updateEntryForm(String selectedSupplierId, String selectedSupplierName, String selectedAddress, String selectedCity, String selectedState, String selectedPhone, String selectedEmail,
                                 String selectedOrderDate, String selectedInvoiceDate, String selectedSubTotal, String selectedPaymentMode, String selectedStatus, ArrayList<Product> selectedProductList, String selectedInvoiceNumber, ScrollPane scrollPane) {

        Stage popupStage = new Stage();
        popupStage.setTitle("Update Entry");

        GridPane formLayout = new GridPane();
        formLayout.setPadding(new Insets(20));
        formLayout.setHgap(10);
        formLayout.setVgap(10);

        Label supplierDetails = new Label("Supplier Details:");
        supplierDetails.setStyle("-fx-font-weight:bold;");
        Label supplierName = new Label("Supplier Name:");
        TextField txtSupplierName = new TextField(selectedSupplierName);

        Label supplierId = new Label("Supplier ID:");
        TextField txtSupplierId = new TextField(selectedSupplierId);

        Label address = new Label("Address:");
        TextField txtAddress = new TextField(selectedAddress);

        Label city = new Label("City:");
        TextField txtCity = new TextField(selectedCity);

        Label state = new Label("State:");
        TextField txtState = new TextField(selectedState);

        Label phone = new Label("Phone:");
        TextField txtPhone = new TextField(selectedPhone);

        Label email = new Label("Email:");
        TextField txtEmail = new TextField(selectedEmail);
        txtEmail.setMinWidth(200);

        tblProducts = new TableView<>();
        TableColumn<Product, String> col1, col2, col3;
        TableColumn<Product, Integer> col4;
        TableColumn<Product, Double> col5;
        col1 = new TableColumn<>("SrNo.");
//        col1.setPrefWidth(40);
        col2 = new TableColumn<>("Product ID");
        col3 = new TableColumn<>("Product Name");
//        col3.setMinWidth(65);
        col4 = new TableColumn<>("Qty");
        col5 = new TableColumn<>("Price");
        col1.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(String.valueOf(cellData.getValue().getSrNo())));
        col2.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getProductID()));
        col3.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getProductName()));
        col4.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getQuantity()).asObject());
        col5.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getPrice()).asObject());
        tblProducts.getColumns().addAll(col1, col2, col3, col4, col5);
        tblProducts.setColumnResizePolicy(tblProducts.CONSTRAINED_RESIZE_POLICY);
        tblProducts.setPrefWidth(400);
        tblProducts.setMaxHeight(250);

        for (Product pr : selectedProductList) {
            tblProducts.getItems().add(pr);
        }

        Label lblInvoiceNumber = new Label("Invoice Number:");
        TextField txtInvoiceNumber = new TextField();
        txtInvoiceNumber.setText(selectedInvoiceNumber);
        txtInvoiceNumber.setEditable(false);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        Label lblOrderDate = new Label("Order Date:");
        DatePicker dpOrderDate = new DatePicker(LocalDate.parse(selectedOrderDate,dtf));

        Label lblInvoiceDate = new Label("Invoice Date:");
        DatePicker dpInvoiceDate = new DatePicker(LocalDate.parse(selectedInvoiceDate,dtf));

        Button addProduct = new Button("Add Product");
        Button updateProduct = new Button("Update Product");
        Button deleteProduct = new Button("Delete Product");
        addProduct.setMinWidth(100);
        addProduct.setOnAction(e -> addProductInEntry());
        updateProduct.setMinWidth(100);
        updateProduct.setOnAction(e -> updateProductInEntry(selectedInvoiceNumber));
        deleteProduct.setMinWidth(100);
        deleteProduct.setOnAction(e -> deleteProductFromEntry());
        VBox btnVBox = new VBox(5);
        btnVBox.getChildren().addAll(addProduct, updateProduct, deleteProduct);

        Label paymentDetails = new Label("Payment Details:");
        paymentDetails.setId("payment-details-label");
        Label subTotal = new Label("Sub Total:");
        txtSubTotal = new Label(selectedSubTotal);

        Label paymentMode = new Label("Payment Mode:");
        ComboBox<String> payment = new ComboBox<>();
        payment.getItems().addAll("Cash", "Cheque", "Net-Banking", "Credit Card");
        payment.setValue(selectedPaymentMode);

        Label status = new Label("Status:");
        RadioButton paid = new RadioButton("Paid");
        RadioButton pending = new RadioButton("Pending");
        ToggleGroup group = new ToggleGroup();
        paid.setToggleGroup(group);
        pending.setToggleGroup(group);
        if (selectedStatus.equals("Paid")) {
            paid.setSelected(true);
        } else {
            pending.setSelected(true);
        }
        HBox statusLayout = new HBox(5);
        statusLayout.getChildren().addAll(paid, pending);

        Button btnUpdate = new Button("Update");
        btnUpdate.setOnAction(e -> {
            try {
                String updateImportsQuery = "UPDATE imports SET supplier_id = ?, supplier_name = ?, address = ?, city = ?, state = ?, phone_number = ?, email = ?, order_date = TO_DATE(?,'YYYY-MM-DD'), invoice_date = TO_DATE(?,'YYYY-MM-DD'), sub_total = ?, payment_mode = ?, payment_status = ?, invoice_number = ? WHERE invoice_number = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateImportsQuery);
                updateStmt.setString(1, txtSupplierId.getText());
                updateStmt.setString(2, txtSupplierName.getText());
                updateStmt.setString(3, txtAddress.getText());
                updateStmt.setString(4, txtCity.getText());
                updateStmt.setString(5, txtState.getText());
                updateStmt.setString(6, txtPhone.getText());
                updateStmt.setString(7, txtEmail.getText());
                updateStmt.setString(8, dpOrderDate.getValue().toString());
                updateStmt.setString(9, dpInvoiceDate.getValue().toString());
                updateStmt.setString(10, txtSubTotal.getText());
                updateStmt.setString(11, payment.getValue());
                updateStmt.setString(12, paid.isSelected() ? "Paid" : "Pending");
                updateStmt.setString(13, txtInvoiceNumber.getText());
                updateStmt.setString(14, selectedInvoiceNumber);

                int rowsAffected = updateStmt.executeUpdate();
                // Create a nested table for the products column
                if (rowsAffected > 0) {
                    PreparedStatement invoiceNumberUpdate = conn.prepareStatement("UPDATE import_products SET invoice_number =? WHERE invoice_number = ?");
                    invoiceNumberUpdate.setString(1, txtInvoiceNumber.getText());
                    invoiceNumberUpdate.setString(2, selectedInvoiceNumber);
                    rowsAffected = 0;
                    rowsAffected = invoiceNumberUpdate.executeUpdate();
                    if (rowsAffected > 0) {
                        AlertUtils.showMsg("Entry updated successfully!");
                        popupStage.close();
                        loadImportsData();
                    } else {
                        AlertUtils.showMsg("Failed to update entry!");
                        popupStage.close();
                        loadImportsData();
                    }
                }
            } catch (Exception ex) {
                AlertUtils.showAlert(Alert.AlertType.ERROR, "Update Failed", "Error updating record: " + ex.getMessage());
            }
        });
        Button btnCancel = new Button("Cancel");
        btnUpdate.setMinHeight(30);
        btnUpdate.setMinWidth(100);
        btnCancel.setMinHeight(30);
        btnCancel.setMinWidth(100);

        formLayout.add(supplierDetails, 0, 0);
        formLayout.add(supplierName, 0, 1);
        formLayout.add(txtSupplierName, 1, 1);
        formLayout.add(supplierId, 2, 1);
        formLayout.add(txtSupplierId, 3, 1);
        formLayout.add(new Label("                                                                             "), 4, 1);
        formLayout.add(lblInvoiceNumber, 5, 1);
        formLayout.add(txtInvoiceNumber, 6, 1);
        formLayout.add(address, 0, 2);
        formLayout.add(txtAddress, 1, 2);
        formLayout.add(city, 2, 2);
        formLayout.add(txtCity, 3, 2);
        formLayout.add(new Label(), 4, 2);
        formLayout.add(lblOrderDate, 5, 2);
        formLayout.add(dpOrderDate, 6, 2);
        formLayout.add(state, 0, 3);
        formLayout.add(txtState, 1, 3);
        formLayout.add(lblInvoiceDate, 5, 3);
        formLayout.add(dpInvoiceDate, 6, 3);
        formLayout.add(phone, 0, 4);
        formLayout.add(txtPhone, 1, 4);
        formLayout.add(email, 2, 4);
        formLayout.add(txtEmail, 3, 4);
        formLayout.add(btnVBox, 0, 6);
        formLayout.add(tblProducts, 1, 6);
        formLayout.add(paymentDetails, 0, 7);
        formLayout.add(subTotal, 0, 8);
        formLayout.add(txtSubTotal, 1, 8);
        formLayout.add(paymentMode, 0, 9);
        formLayout.add(payment, 1, 9);
        formLayout.add(status, 0, 10);
        formLayout.add(statusLayout, 1, 10);

        formLayout.setPrefSize(300, 400);

        VBox vBox = new VBox(10);
        HBox hBox = new HBox(10);
        hBox.setStyle("-fx-border-color: gray;-fx-border-width: 1 0 0 0;");
        hBox.getChildren().addAll(btnUpdate, btnCancel);
        hBox.setPadding(new Insets(10));
        VBox.setVgrow(formLayout, javafx.scene.layout.Priority.ALWAYS);
        vBox.getChildren().addAll(formLayout, hBox);

        scrollPane.setContent(vBox);

        btnCancel.setOnAction(e -> scrollPane.setContent(loadHistory(conn)));
    }

    public void deleteEntry() {
        if (importsTable == null || importsTable.getItems().isEmpty() || importsTable.getSelectionModel().getSelectedItem() == null) {
            AlertUtils.showAlert(Alert.AlertType.INFORMATION, "Unable to delete.", "Please select a row to delete!");
            return;
        }
        Imports selectedEntry = importsTable.getSelectionModel().getSelectedItem();

        // Confirm deletion
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Confirm Deletion");
        confirmationAlert.setHeaderText(null);
        confirmationAlert.setContentText("Are you sure you want to delete this entry?");

        // Wait for user's response
        ButtonType result = confirmationAlert.showAndWait().orElse(ButtonType.CANCEL);
        if (result != ButtonType.OK) {
            return; // User canceled the deletion
        }

        // Get the selected entry's invoice number (to delete in DB)
        String invoiceNumber = selectedEntry.getInvoiceNo();

        // Delete from the database
        String deleteQuery = "DELETE FROM imports WHERE invoice_number = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(deleteQuery)) {
            pstmt.setString(1, invoiceNumber);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                // Update TableView by removing the deleted item
                loadImportsData();
                AlertUtils.showMsg("Entry deleted successfully!");
            } else {
                AlertUtils.showAlert(Alert.AlertType.ERROR,
                        "Deletion Failed", "Failed to delete the entry. Please try again.");
            }
        } catch (SQLException ex) {
            AlertUtils.showAlert(Alert.AlertType.ERROR,
                    "Database Error", "Error occurred while deleting entry: " + ex.getMessage());
        }
    }

    public void deleteProductFromEntry() {
        Product selected = tblProducts.getSelectionModel().getSelectedItem();
        if (selected != null) {
            tblProducts.getItems().remove(selected);
            AlertUtils.showMsg("Product delete successfully!");
            calculateSubTotal();
        } else {
            AlertUtils.showAlert(Alert.AlertType.INFORMATION, "Unable to delete.", "Please select a row to delete!");
        }
    }

    public void calculateSubTotal() {
        double subTotal = 0.0;
        for (Product product : tblProducts.getItems()) {
            subTotal += product.getPrice() * product.getQuantity();
        }
        txtSubTotal.setText(String.format("%.2f", subTotal));
    }

    public void updateStocks(){
        try{
            String selectQuery = "SELECT COUNT(*) FROM products WHERE product_id = ?";
            String updateQuery = "UPDATE products SET qty = qty + ? WHERE product_id = ?";
            String insertQuery = "INSERT INTO products (product_id,product_name,qty,price) VALUES(?,?,?,?)";
            PreparedStatement selectStmt = conn.prepareStatement(selectQuery);
            PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
            PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
            for(Product pr:tblProducts.getItems()){
                selectStmt.setString(1,pr.getProductID());
                if(selectStmt.execute()){
                    updateStmt.setInt(1,pr.getQuantity());
                    updateStmt.setString(2,pr.getProductID());
                    if(!(updateStmt.executeUpdate()>0)){
                        AlertUtils.showAlert(Alert.AlertType.ERROR,"Stock Update Failed","Failed to update stock in inventory!");
                    }
                }else{
                    insertStmt.setString(1,pr.getProductID());
                    insertStmt.setString(2,pr.getProductName());
                    insertStmt.setInt(3,pr.getQuantity());
                    insertStmt.setDouble(4,pr.getPrice());
                    if(!(insertStmt.executeUpdate()>0)){
                        updateStmt.setInt(1,pr.getQuantity());
                        updateStmt.setString(2,pr.getProductID());
                        if(!(updateStmt.executeUpdate()>0)){
                            AlertUtils.showAlert(Alert.AlertType.ERROR,"Stock Update Failed","Failed to add stock in inventory!");
                        }
                    }
                }
            }
        }catch (Exception e){
            System.out.println("ImportController: 922 \n"+e);
        }
    }

    private String processDateString(String date){
        String processedDate;
        processedDate = date.substring(date.lastIndexOf('-')+1,date.lastIndexOf('-')+3) + "-" + date.substring(date.indexOf('-')+1,date.indexOf('-')+3) + "-" + "20" + date.substring(2,4);
        return processedDate;
    }
}


