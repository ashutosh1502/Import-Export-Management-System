package com.project.application;

import com.project.models.Imports;
import com.project.models.Product;
import com.project.utils.*;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.sql.*;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import static com.project.application.MainPage.scrollPane;

public class ImportController {

    private static TableView<Imports> importsTable;
    private TableView<Product> tblProducts;
    private static Connection conn;
    private int srno = 1;
    private Label txtSubTotal, txtNetTotal;
    private ComboBox<Integer> gstComboBox;
    private static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("##,##,###");
    private static final String FETCH_IMPORTS_QUERY = "SELECT * FROM IMPORTS";
    private static final String FETCH_IMPORT_PRODUCTS_QUERY = "SELECT * FROM IMPORT_PRODUCTS WHERE invoice_number = ?";
    private static final String INSERT_IMPORTS_QUERY = "INSERT INTO IMPORTS (" +
            "supplier_name, supplier_id, address, city, state, phone_number, email, invoice_number, order_date, invoice_date, sub_total, payment_mode, payment_status, gst, net_total" +
            ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, TO_DATE(?, 'YYYY-MM-DD'), TO_DATE(?, 'YYYY-MM-DD'), ?, ?, ?, ?, ?)";
    private static final String INSERT_IMPORT_PRODUCTS_QUERY = "INSERT INTO IMPORT_PRODUCTS (invoice_number,product_name,product_id,quantity,price) VALUES (?,?,?,?,?)";
    private static final String UPDATE_IMPORTS_QUERY = "UPDATE IMPORTS SET supplier_id = ?, supplier_name = ?, address = ?, city = ?, state = ?, phone_number = ?, email = ?, order_date = TO_DATE(?,'YYYY-MM-DD'), invoice_date = TO_DATE(?,'YYYY-MM-DD'), sub_total = ?, payment_mode = ?, payment_status = ?, invoice_number = ?, gst = ?, net_total = ? WHERE invoice_number = ?";
    private static final String UPDATE_IMPORT_PRODUCTS_QUERY = "UPDATE IMPORT_PRODUCTS SET product_name = ?, product_id = ?, quantity = ?, price = ? WHERE invoice_number = ? AND product_id = ?";
    private static final String DELETE_IMPORT_QUERY = "DELETE FROM IMPORTS WHERE invoice_number = ?";

    //-------------------------------------------------------------------------------

    public TableView<Imports> initializeImportsTable(Connection connection) {
        conn = connection;

        importsTable = new TableView<>();
        initializeImportsTableColumns();
        importsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        loadImportsData();
        handleImportsTableDoubleClick();

        return importsTable;
    }

    private void initializeImportsTableColumns(){
        TableColumn<Imports, String> colSrNo, colInvoiceNo, colSupplierName, colProducts, colNetTotal, colPaymentStatus, colInvoiceDate;
        TableColumn<Imports, Integer> colTotalQty;
        colSrNo = new TableColumn<>("SrNo.");
        colInvoiceNo = new TableColumn<>("Invoice No.");
        colSupplierName = new TableColumn<>("Supplier Name");
        colProducts = new TableColumn<>("Products");
        colTotalQty = new TableColumn<>("Total Qty");
        colNetTotal = new TableColumn<>("Net Total");
        colPaymentStatus = new TableColumn<>("Payment Status");
        colInvoiceDate = new TableColumn<>("Invoice Date");
        colSrNo.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSrNo()));
        colInvoiceNo.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getInvoiceNo()));
        colSupplierName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSupplierName()));
        colProducts.setCellValueFactory(cellData -> new SimpleStringProperty(String.join(", ", cellData.getValue().getProducts())));
        colTotalQty.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getTotalQuantity()).asObject());
        colNetTotal.setCellValueFactory(cellData -> new SimpleStringProperty("Rs. " + CURRENCY_FORMAT.format(cellData.getValue().getNetTotal())));
        colPaymentStatus.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPaymentStatus()));
        colInvoiceDate.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getInvoiceDate()));
        importsTable.getColumns().addAll(colSrNo, colInvoiceNo, colSupplierName, colProducts, colTotalQty, colNetTotal, colPaymentStatus, colInvoiceDate);
    }

    //-------------------------------------------------------------------------------

    public void loadImportsData() {
        importsTable.getItems().clear();

        try (Statement fetchImportsStmt = conn.createStatement();
             ResultSet rs = fetchImportsStmt.executeQuery(FETCH_IMPORTS_QUERY)) {
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
                double netTotal = rs.getDouble("net_total");
                String paymentMode = rs.getString("payment_mode");
                String paymentStatus = rs.getString("payment_status");

                int totalQty = 0;
                ArrayList<String> productList = new ArrayList<>();
                try(PreparedStatement fetchImportProductsStmt = conn.prepareStatement(FETCH_IMPORT_PRODUCTS_QUERY)){
                    fetchImportProductsStmt.setString(1,invoiceNo);
                    try(ResultSet iprs = fetchImportProductsStmt.executeQuery()){
                        while (iprs.next()) {
                            productList.add(iprs.getString("product_name"));
                            totalQty += iprs.getInt("quantity");
                        }
                    }
                }
                Imports importItem = new Imports(
                        srNo++, invoiceNo, supplierId, supplierName, productList, totalQty,
                        netTotal, address, city, state, phno, email, orderDate, invoiceDate,
                        paymentMode, paymentStatus
                );
                importsTable.getItems().add(importItem);
            }
        } catch (SQLException e) {
            DatabaseErrorHandler.handleDatabaseError(e);
        }
    }

    //INSERTION--------------------------------------------------------------------------------------
    public void addEntry() {
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
        AutoCompleteUtils.setAutoCompleteStates(txtState);

        Label phone = new Label("Phone:");
        TextField txtPhone = new TextField();

        Label email = new Label("Email:");
        TextField txtEmail = new TextField();
        txtEmail.setMinWidth(200);

        setupProductsTable();

        Label lblInvoiceNumber = new Label("Invoice Number:");
        TextField txtInvoiceNumber = new TextField(generateInvoiceNumber());

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

        Label gst = new Label("GST (%):");
        gstComboBox = new ComboBox<>();
        gstComboBox.getItems().addAll(0,5,12,18,28);
        gstComboBox.setValue(18);
        gstComboBox.setMaxWidth(80);
        gstComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            calculateNetTotal();
        });

        Label netTotal = new Label("Net Total:");
        netTotal.setStyle("-fx-font-weight:bold;");
        txtNetTotal = new Label("0.00");
        txtNetTotal.setStyle("-fx-font-weight:bold;");

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
        pending.setSelected(true);
        HBox statusLayout = new HBox(5);
        statusLayout.getChildren().addAll(paid, pending);

        Button btnSubmit = new Button("Submit");
        btnSubmit.setMinHeight(30);
        btnSubmit.setMinWidth(100);
        btnSubmit.setOnAction(e -> {
            try {
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
                int gstVal = gstComboBox.getValue();
                double netTotalVal = Double.parseDouble(txtNetTotal.getText());

                conn.setAutoCommit(false);
                if(FormValidator.validateSupplierId(supplierIdEntered)){
                    txtSupplierId.setStyle("-fx-border-color: red; -fx-border-width: 1px;");
                    return;
                }else{
                    txtSupplierId.setStyle("-fx-border-width: 0px;");
                }
                if(FormValidator.validateSupplierName(supplierNameEntered)){
                    txtSupplierName.setStyle("-fx-border-color: red; -fx-border-width: 1px;");
                    return;
                }else{
                    txtSupplierName.setStyle("-fx-border-width: 0px;");
                }
                if(!FormValidator.validatePhoneNumber(phoneEntered)){
                    txtPhone.setStyle("-fx-border-color: red; -fx-border-width: 1px;");
                    return;
                }else{
                    txtPhone.setStyle("-fx-border-width: 0px;");
                }
                if(!FormValidator.validateEmail(emailEntered)){
                    txtEmail.setStyle("-fx-border-color: red; -fx-border-width: 1px;");
                    return;
                }else{
                    txtEmail.setStyle("-fx-border-width: 0px;");
                }
                if (!insertImport(supplierNameEntered, supplierIdEntered, addressEntered, cityEntered,
                        stateEntered, phoneEntered, emailEntered, invoiceNumberEntered,
                        orderDateEntered, invoiceDateEntered, subTotalEntered, paymentModeEntered,
                        paymentStatusEntered, gstVal, netTotalVal)) {
                    conn.rollback();
                    return;
                }
                if(!insertInvoiceNumber(invoiceNumberEntered)){
                    conn.rollback();
                    return;
                }
                if (!insertImportProducts(invoiceNumberEntered)) {
                    conn.rollback();
                    return;
                }
                conn.commit();
                AlertUtils.showAlert(Alert.AlertType.INFORMATION,"Success","Entry added successfully!");
                modifyStocks();
                scrollPane.setContent(initializeImportsTable(conn));
            } catch (Exception ex) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
                ex.printStackTrace();
                AlertUtils.showAlert(Alert.AlertType.ERROR, "Error", "Failed to add entry: " + ex.getMessage());
            }finally {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException sqlEx) {
                    sqlEx.printStackTrace();
                }
            }
        });

        Button btnCancel = new Button("Cancel");
        btnCancel.setMinHeight(30);
        btnCancel.setMinWidth(100);
        btnCancel.setOnAction(e -> scrollPane.setContent(initializeImportsTable(conn)));

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
        formLayout.add(gst,2,8);
        formLayout.add(gstComboBox,3,8);
        formLayout.add(netTotal,0,9);
        formLayout.add(txtNetTotal,1,9);
        formLayout.add(paymentMode, 0, 10);
        formLayout.add(payment, 1, 10);
        formLayout.add(status, 0, 11);
        formLayout.add(statusLayout, 1, 11);

        formLayout.setPrefSize(300, 400);

        VBox vBox = new VBox(10);
        HBox hBox = new HBox(10);
        hBox.setStyle("-fx-border-color: gray;-fx-border-width: 1 0 0 0;");
        hBox.getChildren().addAll(btnSubmit, btnCancel);
        hBox.setPadding(new Insets(10));
        VBox.setVgrow(formLayout, javafx.scene.layout.Priority.ALWAYS);
        vBox.getChildren().addAll(formLayout, hBox);

        scrollPane.setContent(vBox);
    }

    public void addProductInEntry() {
        Stage popupStage = new Stage();
        popupStage.setTitle("Add Product");
        popupStage.initModality(Modality.APPLICATION_MODAL);

        GridPane productForm = new GridPane(150,250);
        productForm.setPadding(new Insets(20));
        productForm.setHgap(10);
        productForm.setVgap(10);

        Label lblProductName = new Label("Product Name:");
        TextField txtProductName = new TextField();
        GridPane.setHgrow(txtProductName, Priority.ALWAYS);

        Label lblProductId = new Label("Product ID:");
        TextField txtProductId = new TextField();

        Label lblPrice = new Label("Price:");
        TextField txtPrice = new TextField();
        AutoCompleteUtils.setAutoCompleteProductName(conn, txtProductName, txtProductId, txtPrice);

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
                srno++;
                AlertUtils.showMsg("Product added successfully!");
                calculateSubTotal();
                calculateNetTotal();
                txtProductName.clear();
                txtProductId.clear();
                txtQuantity.clear();
                txtPrice.clear();

            } catch (NumberFormatException ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Quantity and Price must be valid numbers!", ButtonType.OK);
                alert.showAndWait();
            }
        });
        btnClose.setOnAction(e -> popupStage.close());

        productForm.add(lblProductName, 0, 0);
        productForm.add(txtProductName, 1, 0);
        productForm.add(lblProductId, 0, 1);
        productForm.add(txtProductId, 1, 1);
        productForm.add(lblPrice, 0, 2);
        productForm.add(txtPrice, 1, 2);
        productForm.add(lblQuantity, 0, 3);
        productForm.add(txtQuantity, 1, 3);
        productForm.add(btnAddItem, 0, 4);
        productForm.add(btnClose, 1, 4);

        Scene scene = new Scene(productForm, 440, 220);
        popupStage.setScene(scene);
        popupStage.show();
        popupStage.setX(popupStage.getX()+140);
        popupStage.setY(popupStage.getY()+80);
    }

    private void setupProductsTable(){
        tblProducts = new TableView<>();
        TableColumn<Product, String> colSrNo, colProductId, colProductName;
        TableColumn<Product, Integer> colQty;
        TableColumn<Product, Double> colPrice;
        colSrNo = new TableColumn<>("SrNo.");
        colSrNo.setPrefWidth(40);
        colProductId = new TableColumn<>("Product ID");
        colProductName = new TableColumn<>("Product Name");
        colProductName.setPrefWidth(120);
        colQty = new TableColumn<>("Qty");
        colPrice = new TableColumn<>("Price");
        colSrNo.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getSrNo()));
        colProductId.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getProductID()));
        colProductName.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getProductName()));
        colProductName.setMinWidth(60);
        colProductName.setPrefWidth(80);
        colQty.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getQuantity()).asObject());
        colPrice.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getPrice()).asObject());
        tblProducts.getColumns().addAll(colSrNo, colProductId, colProductName, colQty, colPrice);
        tblProducts.setPrefWidth(400);
        tblProducts.setMaxHeight(250);
    }

    private boolean insertImport(String supplierName, String supplierId, String address, String city, String state,
                                 String phone, String email, String invoiceNumber, String orderDate, String invoiceDate,
                                 double subTotal, String paymentMode, String paymentStatus, int gstVal, double netTotalVal) {
        try (PreparedStatement preparedStatement = conn.prepareStatement(INSERT_IMPORTS_QUERY)) {
            preparedStatement.setString(1, supplierName);
            preparedStatement.setString(2, supplierId);
            preparedStatement.setString(3, address);
            preparedStatement.setString(4, city);
            preparedStatement.setString(5, state);
            preparedStatement.setString(6, phone);
            preparedStatement.setString(7, email);
            preparedStatement.setString(8, invoiceNumber);
            preparedStatement.setString(9, orderDate);
            preparedStatement.setString(10, invoiceDate);
            preparedStatement.setDouble(11, subTotal);
            preparedStatement.setString(12, paymentMode);
            preparedStatement.setString(13, paymentStatus);
            preparedStatement.setInt(14, gstVal);
            preparedStatement.setDouble(15, netTotalVal);

            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            DatabaseErrorHandler.handleDatabaseError(e);
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Error", "Please fill all the details!");
            return false;
        }
    }

    private boolean insertImportProducts(String invoiceNumber) {
        try (PreparedStatement preparedStatement = conn.prepareStatement(INSERT_IMPORT_PRODUCTS_QUERY)) {
            for (Product product : tblProducts.getItems()) {
                preparedStatement.setString(1, invoiceNumber);
                preparedStatement.setString(2, product.getProductName());
                preparedStatement.setString(3, product.getProductID());
                preparedStatement.setInt(4, product.getQuantity());
                preparedStatement.setDouble(5, product.getPrice());

                if (preparedStatement.executeUpdate() <= 0) {
                    AlertUtils.showAlert(Alert.AlertType.ERROR, "Something went wrong", "Failed to insert product: " + product.getProductName());
                    return false;
                }
            }
            return true;
        } catch (SQLException e) {
            DatabaseErrorHandler.handleDatabaseError(e);
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Something went wrong", "Failed to insert products");
            return false;
        }
    }

    //---------------------------------------------------------------------------------------------
    //---------------------------------------------------------------------------------------------

    //UPDATION--------------------------------------------------------------------------------------
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
                PreparedStatement updateProductStmt = conn.prepareStatement(UPDATE_IMPORT_PRODUCTS_QUERY);
                updateProductStmt.setString(1, productName);
                updateProductStmt.setString(2, productId);
                updateProductStmt.setInt(3, quantity);
                updateProductStmt.setDouble(4, price);
                updateProductStmt.setString(5, invoiceNumber);
                updateProductStmt.setString(6,productId);

                PreparedStatement stockQtyUpdateStmt = conn.prepareStatement("UPDATE PRODUCTS SET qty = qty + ? WHERE product_id = ?");
                stockQtyUpdateStmt.setInt(1,(quantity-selectedQty));
                stockQtyUpdateStmt.setString(2,selectedPrId);

                boolean importProductsUpdated = updateProductStmt.executeUpdate() > 0;
                boolean stocksQtyUpdated = stockQtyUpdateStmt.executeUpdate() > 0;

                conn.setAutoCommit(false);
                if (importProductsUpdated && stocksQtyUpdated) {
                    int selectedIndex = tblProducts.getSelectionModel().getSelectedIndex();
                    tblProducts.getItems().set(selectedIndex, updatedProduct);
                    AlertUtils.showMsg("Product updated successfully!");
                    calculateSubTotal();
                    calculateNetTotal();
                    popupStage.close();
                } else {
                    conn.rollback();
                    AlertUtils.showMsg("Failed to update product!");
                    calculateSubTotal();
                    popupStage.close();
                }
            } catch (SQLException ex) {
                try{
                    conn.rollback();
                }catch (SQLException s){
                    DatabaseErrorHandler.handleDatabaseError(s);
                }
                DatabaseErrorHandler.handleDatabaseError(ex);
                Alert alert = new Alert(Alert.AlertType.WARNING, "Something went wrong!", ButtonType.OK);
                alert.showAndWait();
            }finally {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException sqlEx) {
                    sqlEx.printStackTrace();
                }
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

    public void viewUpdateEntry() {
        if (importsTable == null || importsTable.getItems().isEmpty() || importsTable.getSelectionModel().getSelectedItem() == null) {
            AlertUtils.showAlert(Alert.AlertType.INFORMATION, "Unable to update.", "Please select a row to update!");
            return;
        }

        String invoiceNumber = importsTable.getSelectionModel().getSelectedItem().getInvoiceNo();
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
                int gstVal = rs.getInt("gst");
                String netTotalVal = rs.getString("net_total");

                // fetch product data .........
                ArrayList<Product> productList = new ArrayList<>();
                try {
                    PreparedStatement prstmt = conn.prepareStatement(FETCH_IMPORT_PRODUCTS_QUERY);
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
                    DatabaseErrorHandler.handleDatabaseError(e);
                    AlertUtils.showAlert(Alert.AlertType.ERROR,"Something went wrong","Failed to load products");
                }
                // Pre-fill the form and allow updates
                updateEntryForm(supplierId, supplierName, address, city, state, phone, email, orderDate, invoiceDate, subTotal, paymentMode, status, productList, invoiceNumber, gstVal, netTotalVal, scrollPane);

            } else {
                AlertUtils.showAlert(Alert.AlertType.INFORMATION, "Not Found", "The selected invoice does not exist in the database.");
            }

        } catch (SQLException e) {
            DatabaseErrorHandler.handleDatabaseError(e);
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Error", "Error retrieving entry details: ");
        }
    }

    private void updateEntryForm(String selectedSupplierId, String selectedSupplierName, String selectedAddress, String selectedCity,
                                 String selectedState, String selectedPhone, String selectedEmail,String selectedOrderDate,
                                 String selectedInvoiceDate, String selectedSubTotal, String selectedPaymentMode, String selectedStatus,
                                 ArrayList<Product> selectedProductList, String selectedInvoiceNumber, int selectedGstVal, String selectedNetTotalVal,
                                 ScrollPane scrollPane) {

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
        AutoCompleteUtils.setAutoCompleteStates(txtState);

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
        col2 = new TableColumn<>("Product ID");
        col3 = new TableColumn<>("Product Name");
        col4 = new TableColumn<>("Qty");
        col5 = new TableColumn<>("Price");
        col1.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(String.valueOf(cellData.getValue().getSrNo())));
        col2.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getProductID()));
        col3.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getProductName()));
        col4.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getQuantity()).asObject());
        col5.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getPrice()).asObject());
        tblProducts.getColumns().addAll(col1, col2, col3, col4, col5);
        tblProducts.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
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

        Label gst = new Label("GST (%):");
        gstComboBox = new ComboBox<>();
        gstComboBox.getItems().addAll(0,5,12,18,28);
        gstComboBox.setValue(selectedGstVal);
        gstComboBox.setMaxWidth(80);
        gstComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            calculateNetTotal();
        });

        Label netTotal = new Label("Net Total:");
        netTotal.setStyle("-fx-font-weight:bold;");
        txtNetTotal = new Label(selectedNetTotalVal);
        txtNetTotal.setStyle("-fx-font-weight:bold;");

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
            if(!FormValidator.validatePhoneNumber(txtPhone.getText())){
                txtPhone.setStyle("-fx-border-color: red; -fx-border-width: 1px;");
                return;
            }else{
                txtPhone.setStyle("-fx-border-width: 0px;");
            }
            if(!FormValidator.validateEmail(txtEmail.getText())){
                txtEmail.setStyle("-fx-border-color: red; -fx-border-width: 1px;");
                return;
            }else{
                txtEmail.setStyle("-fx-border-width: 0px;");
            }

            try {
                conn.setAutoCommit(false);
                int gstVal = gstComboBox.getValue();
                boolean updatedImport = updateImportsEntry(txtSupplierId.getText(),txtSupplierName.getText(),txtAddress.getText(),
                        txtCity.getText(), txtState.getText(), txtPhone.getText(), txtEmail.getText(), dpOrderDate.getValue().toString(),
                        dpInvoiceDate.getValue().toString(), txtSubTotal.getText(), payment.getValue(), paid.isSelected() ? "Paid" : "Pending",
                        txtInvoiceNumber.getText(), gstVal, Double.parseDouble(txtNetTotal.getText()), selectedInvoiceNumber);
                boolean invoiceUpdated = updateInvoiceProductsEntry(txtInvoiceNumber.getText(),selectedInvoiceNumber);
                // Create a nested table for the products column
                if (updatedImport && invoiceUpdated) {
                    AlertUtils.showMsg("Entry updated successfully!");
                    popupStage.close();
                    loadImportsData();
                    conn.rollback();
                }else{
                    conn.rollback();
                    AlertUtils.showMsg("Failed to update entry!");
                    popupStage.close();
                    loadImportsData();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                AlertUtils.showAlert(Alert.AlertType.ERROR, "Update Failed", "Error updating record: " + ex.getMessage());
                try{
                    conn.rollback();
                }catch (SQLException rollBackEx){
                    rollBackEx.printStackTrace();
                }
            }finally {
                try{
                    conn.setAutoCommit(true);
                }catch (SQLException commitExp){
                    commitExp.printStackTrace();
                }
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
        formLayout.add(gst,2,8);
        formLayout.add(gstComboBox,3,8);
        formLayout.add(netTotal,0,9);
        formLayout.add(txtNetTotal,1,9);
        formLayout.add(paymentMode, 0, 10);
        formLayout.add(payment, 1, 10);
        formLayout.add(status, 0, 11);
        formLayout.add(statusLayout, 1, 11);

        formLayout.setPrefSize(300, 400);

        VBox vBox = new VBox(10);
        HBox hBox = new HBox(10);
        hBox.setStyle("-fx-border-color: gray;-fx-border-width: 1 0 0 0;");
        hBox.getChildren().addAll(btnUpdate, btnCancel);
        hBox.setPadding(new Insets(10));
        VBox.setVgrow(formLayout, javafx.scene.layout.Priority.ALWAYS);
        vBox.getChildren().addAll(formLayout, hBox);

        scrollPane.setContent(vBox);

        btnCancel.setOnAction(e -> scrollPane.setContent(initializeImportsTable(conn)));
    }

    private boolean updateImportsEntry(String supplierId, String supplierName, String address, String city,
                                       String state, String phone, String email, String orderDate, String invoiceDate,
                                       String subtotal, String paymentMode, String paymentStatus, String invoiceNumber,
                                       int gstVal, double netTotalVal, String selectedInvoiceNumber) throws SQLException{
        try(PreparedStatement updateStmt = conn.prepareStatement(UPDATE_IMPORTS_QUERY)){
            updateStmt.setString(1, supplierId);
            updateStmt.setString(2, supplierName);
            updateStmt.setString(3, address);
            updateStmt.setString(4, city);
            updateStmt.setString(5, state);
            updateStmt.setString(6, phone);
            updateStmt.setString(7, email);
            updateStmt.setString(8, orderDate);
            updateStmt.setString(9, invoiceDate);
            updateStmt.setString(10, subtotal);
            updateStmt.setString(11, paymentMode);
            updateStmt.setString(12, paymentStatus);
            updateStmt.setString(13, invoiceNumber);
            updateStmt.setInt(14, gstVal);
            updateStmt.setDouble(15, netTotalVal);
            updateStmt.setString(16, selectedInvoiceNumber);
            return updateStmt.executeUpdate() > 0;
        }
    }
    private boolean updateInvoiceProductsEntry(String invoiceNumber, String selectedInvoiceNumber) throws SQLException{
        try(PreparedStatement invoiceNumberUpdate = conn.prepareStatement("UPDATE import_products SET invoice_number =? WHERE invoice_number = ?")){
            invoiceNumberUpdate.setString(1, invoiceNumber);
            invoiceNumberUpdate.setString(2, selectedInvoiceNumber);
            return invoiceNumberUpdate.executeUpdate() > 0;
        }
    }
    //---------------------------------------------------------------------------------------------
    //---------------------------------------------------------------------------------------------

    //DELETION--------------------------------------------------------------------------------------

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
        try (PreparedStatement pstmt = conn.prepareStatement(DELETE_IMPORT_QUERY)) {
            pstmt.setString(1, invoiceNumber);

            conn.setAutoCommit(false);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                // Update TableView by removing the deleted item
                scrollPane.setContent(initializeImportsTable(conn));
                AlertUtils.showMsg("Entry deleted successfully!");
            } else {
                conn.rollback();
                AlertUtils.showAlert(Alert.AlertType.ERROR,
                        "Deletion Failed", "Failed to delete the entry. Please try again.");
            }
        } catch (SQLException ex) {
            try{
                conn.rollback();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            DatabaseErrorHandler.handleDatabaseError(ex);
            AlertUtils.showAlert(Alert.AlertType.ERROR,
                    "Database Error", "Error occurred while deleting entry: ");
        }finally {
            try{
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    //---------------------------------------------------------------------------------------------
    //---------------------------------------------------------------------------------------------

    //HELPER FUNCTIONS--------------------------------------------------------------------------------------
    private void calculateSubTotal() {
        double subTotal = 0.0;
        for (Product product : tblProducts.getItems()) {
            subTotal += product.getPrice() * product.getQuantity();
        }
        txtSubTotal.setText(String.format("%.2f", subTotal));
    }

    private void calculateNetTotal(){
        double netTotal;
        double subTotal = Double.parseDouble(txtSubTotal.getText());
        int gst = gstComboBox.getValue();
        netTotal = subTotal + (subTotal*((double) gst /100));
        txtNetTotal.setText(String.format("%.2f",netTotal));
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

    public void modifyStocks(){
        try{
            String selectQuery = "SELECT COUNT(*) FROM products WHERE product_id = ?";
            String updateQuery = "UPDATE products SET qty = qty + ? WHERE product_id = ?";
            String insertQuery = "INSERT INTO products (product_id,product_name,qty,price) VALUES(?,?,?,?)";
            PreparedStatement selectStmt = conn.prepareStatement(selectQuery);
            PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
            PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
            for(Product pr:tblProducts.getItems()){
                selectStmt.setString(1,pr.getProductID());
                ResultSet rs = selectStmt.executeQuery();
                if(rs.next() && rs.getInt(1)>0){
                    updateStmt.setInt(1,pr.getQuantity());
                    updateStmt.setString(2,pr.getProductID());
                    if(!(updateStmt.executeUpdate()>0)){
                        AlertUtils.showAlert(Alert.AlertType.ERROR,"Stock Update Failed","Failed to update stock in inventory!");
                        return;
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
                            return;
                        }
                    }
                }
            }
        }catch (SQLException e){
            DatabaseErrorHandler.handleDatabaseError(e);
            AlertUtils.showAlert(Alert.AlertType.ERROR,"Stock Update Failed","Failed to add stock in inventory!");
        }
    }

    private String processDateString(String date){
        String processedDate;
        processedDate = date.substring(date.lastIndexOf('-')+1,date.lastIndexOf('-')+3) + "-" + date.substring(date.indexOf('-')+1,date.indexOf('-')+3) + "-" + "20" + date.substring(2,4);
        return processedDate;
    }

    private void handleImportsTableDoubleClick(){
        importsTable.setRowFactory(it ->{
            TableRow<Imports> row = new TableRow<>();
            row.setOnMouseClicked(mouseEvent -> {
                if(mouseEvent.getClickCount()==2 && !row.isEmpty()){
                    viewUpdateEntry();
                }
            });
            return row;
        });
    }

    private boolean insertInvoiceNumber(String invoiceNum){
        String insertQuery = "INSERT INTO import_invoice_numbers (invoice_number) VALUES(?)";
        String selectQuery = "SELECT * FROM import_invoice_numbers WHERE invoice_number = ?";
        try{
            PreparedStatement selectStmt = conn.prepareStatement(selectQuery);
            selectStmt.setString(1,invoiceNum);
            ResultSet res = selectStmt.executeQuery();
            if(res.next()) {
                AlertUtils.showAlert(Alert.AlertType.ERROR,"Cannot insert","The invoice number already exists, please try a different one!");
                return false;
            }
            PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
            insertStmt.setString(1,invoiceNum);
            insertStmt.executeUpdate();
        }catch (SQLException inv){
            DatabaseErrorHandler.handleDatabaseError(inv);
            AlertUtils.showAlert(Alert.AlertType.ERROR,"Something went wrong","Failed to insert the invoice number!");
            return false;
        }
        return true;
    }

    private String generateInvoiceNumber(){
        int importsCount = 1;
        String invoiceNum;
        if (importsTable != null && importsTable.getItems() != null) {
            importsCount = importsTable.getItems().size() + 1;
        }
        invoiceNum = "IMP"+
                (java.time.LocalDate.now()).toString().replace("-","").substring(2)+
                String.format("%04d",importsCount);
        return invoiceNum;
    }
}


