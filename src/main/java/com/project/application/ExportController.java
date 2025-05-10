package com.project.application;

import com.project.models.ExportBill;
import com.project.models.ExportBillTableEntry;
import com.project.models.Exports;
import com.project.models.Product;
import com.project.utils.*;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.InputStream;
import java.sql.*;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import static com.project.application.MainPage.scrollPane;

public class ExportController {
    private static TableView<Exports> exportsTable;
    private TableView<Product> tblProducts;
    private static Connection conn;
    private int srno = 1;
    private Label txtSubTotal, txtNetTotal;
    private ComboBox<Integer> gstComboBox;
    private Button printInvoiceBtn;
    private String sectionName;
    private Stage refPrimaryStage;
    private static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("##,##,###");
    private static final String FETCH_EXPORTS_QUERY = "SELECT * FROM EXPORTS";
    private static final String FETCH_EXPORT_PRODUCTS_QUERY = "SELECT * FROM EXPORT_PRODUCTS WHERE invoice_number = ?";
    private static final String INSERT_EXPORTS_QUERY = "INSERT INTO EXPORTS (" +
            "customer_name, customer_id, address, city, state, phone_number, email, invoice_number, order_date, invoice_date, sub_total, payment_mode, payment_status, gst, net_total" +
            ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, TO_DATE(?, 'YYYY-MM-DD'), TO_DATE(?, 'YYYY-MM-DD'), ?, ?, ?, ?, ?)";
    private static final String INSERT_EXPORT_PRODUCTS_QUERY = "INSERT INTO EXPORT_PRODUCTS (invoice_number,product_name,product_id,quantity,price) VALUES (?,?,?,?,?)";
    private static final String UPDATE_EXPORTS_QUERY = "UPDATE EXPORTS SET customer_id = ?, customer_name = ?, address = ?, city = ?, state = ?, phone_number = ?, email = ?, order_date = TO_DATE(?, 'YYYY-MM-DD'), invoice_date = TO_DATE(?, 'YYYY-MM-DD'), sub_total = ?, payment_mode = ?, payment_status = ?, invoice_number = ?, gst = ?, net_total = ? WHERE invoice_number = ?";
    private static final String UPDATE_EXPORT_PRODUCTS_QUERY = "UPDATE EXPORT_PRODUCTS SET product_name = ?, product_id = ?, quantity = ?, price = ? WHERE invoice_number = ? AND product_id =?";
    private static final String DELETE_EXPORT_QUERY = "DELETE FROM EXPORTS WHERE invoice_number = ?";

    //-------------------------------------------------------------------------------

    public TableView<Exports> initializeExportsTable(Connection connection, String sectionName, Stage refPrimaryStage) {
        conn = connection;

        this.refPrimaryStage = refPrimaryStage;
        this.sectionName = sectionName;
        exportsTable = new TableView<>();
        initializeExportsTableColumns();
        exportsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        loadExportsData();
        handleExportsTableDoubleClick();

        return exportsTable;
    }

    private void initializeExportsTableColumns() {
        TableColumn<Exports, String> colSrNo, colInvoiceNo, colCustomerName, colProducts, colNetTotal, colPaymentStatus, colInvoiceDate;
        TableColumn<Exports, Integer> colTotalQty;
        colSrNo = new TableColumn<>("SrNo.");
        colInvoiceNo = new TableColumn<>("Invoice No.");
        colCustomerName = new TableColumn<>("Customer Name");
        colProducts = new TableColumn<>("Products");
        colTotalQty = new TableColumn<>("Total Qty");
        colNetTotal = new TableColumn<>("Amount");
        colPaymentStatus = new TableColumn<>("Payment Status");
        colInvoiceDate = new TableColumn<>("Invoice Date");
        colSrNo.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSrNo()));
        colInvoiceNo.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getInvoiceNo()));
        colCustomerName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCustomerName()));
        colProducts.setCellValueFactory(cellData -> new SimpleStringProperty(String.join(", ", cellData.getValue().getProducts())));
        colTotalQty.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getTotalQuantity()).asObject());
        colNetTotal.setCellValueFactory(cellData -> new SimpleStringProperty("Rs. " + CURRENCY_FORMAT.format(cellData.getValue().getNetTotal())));
        colPaymentStatus.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPaymentStatus()));
        colInvoiceDate.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getInvoiceDate()));
        exportsTable.getColumns().addAll(colSrNo, colInvoiceNo, colCustomerName, colProducts, colTotalQty, colNetTotal, colPaymentStatus, colInvoiceDate);
    }

    public void loadExportsData() {
        exportsTable.getItems().clear(); // Clear existing items before loading new data

        try (Statement fetchExportsStmt = conn.createStatement();
             ResultSet rs = fetchExportsStmt.executeQuery(FETCH_EXPORTS_QUERY)) {
            int srNo = 1;
            while (rs.next()) {
                String customerName = rs.getString("customer_name");
                String customerId = rs.getString("customer_id");
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
                try (PreparedStatement fetchExportProductsStmt = conn.prepareStatement(FETCH_EXPORT_PRODUCTS_QUERY)) {
                    fetchExportProductsStmt.setString(1, invoiceNo);
                    try (ResultSet iprs = fetchExportProductsStmt.executeQuery()) {
                        while (iprs.next()) {
                            productList.add(iprs.getString("product_name"));
                            totalQty += iprs.getInt("quantity");
                        }
                    }
                }
                Exports exportItem = new Exports(
                        srNo++, invoiceNo, customerId, customerName, productList, totalQty,
                        netTotal, address, city, state, phno, email, orderDate, invoiceDate,
                        paymentMode, paymentStatus
                );
                exportsTable.getItems().add(exportItem);
            }
        } catch (SQLException e) {
            DatabaseErrorHandler.handleDatabaseError(e);
        }
    }

    //OPERATIONS BUTTON ACTIONS--------------------------------------------------------------------------------------
    public void addEntry() {
        Stage popupStage = new Stage();
        popupStage.setTitle("Add Entry");

        GridPane formLayout = new GridPane();
        formLayout.setPadding(new Insets(20));
        formLayout.setHgap(10);
        formLayout.setVgap(10);

        Label customerDetails = new Label("Customer Details:");
        customerDetails.setStyle("-fx-font-weight:bold;");

        Label customerName = new Label("Customer Name:");
        TextField txtCustomerName = new TextField();

        Label customerId = new Label("Customer ID:");
        TextField txtCustomerId = new TextField();

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

        setUpProductsTable();

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
                String customerIdEntered = txtCustomerId.getText();
                String customerNameEntered = txtCustomerName.getText();
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
                if(!insertExport(customerNameEntered, customerIdEntered, addressEntered, cityEntered,
                        stateEntered, phoneEntered, emailEntered, invoiceNumberEntered,
                        orderDateEntered, invoiceDateEntered, subTotalEntered, paymentModeEntered,
                        paymentStatusEntered, gstVal, netTotalVal)){
                    conn.rollback();
                    return;
                }
                if(!insertInvoiceNumber(invoiceNumberEntered)){
                    conn.rollback();
                    return;
                }
                if (!insertExportProducts(invoiceNumberEntered)){
                    conn.rollback();
                    return;
                }
                conn.commit();
                AlertUtils.showAlert(Alert.AlertType.INFORMATION,"Success","Entry added successfully!");
                updateStocks();
                scrollPane.setContent(initializeExportsTable(conn,sectionName,refPrimaryStage));
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
        btnCancel.setOnAction(e -> scrollPane.setContent(initializeExportsTable(conn,sectionName,refPrimaryStage)));

        formLayout.add(customerDetails, 0, 0);
        formLayout.add(customerName, 0, 1);
        formLayout.add(txtCustomerName, 1, 1);
        formLayout.add(customerId, 2, 1);
        formLayout.add(txtCustomerId, 3, 1);
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
                if (!checkAvailableQty(productId, quantity)) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Insufficient Stock!", ButtonType.OK);
                    alert.showAndWait();
                    return;
                }
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

    private void setUpProductsTable() {
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

    private boolean insertExport(String customerName, String customerId, String address, String city, String state,
                                 String phone, String email, String invoiceNumber, String orderDate, String invoiceDate,
                                 double subTotal, String paymentMode, String paymentStatus, int gstVal, double netTotalVal) {
        try (PreparedStatement preparedStatement = conn.prepareStatement(INSERT_EXPORTS_QUERY)) {
            preparedStatement.setString(1, customerName);
            preparedStatement.setString(2, customerId);
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

    private boolean insertExportProducts(String invoiceNumber) {
        try (PreparedStatement preparedStatement = conn.prepareStatement(INSERT_EXPORT_PRODUCTS_QUERY)) {
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

    //-------------------------------------------------------------------------------

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
                PreparedStatement updateProductStmt = conn.prepareStatement(UPDATE_EXPORT_PRODUCTS_QUERY);
                updateProductStmt.setString(1, productName);
                updateProductStmt.setString(2, productId);
                updateProductStmt.setInt(3, quantity);
                updateProductStmt.setDouble(4, price);
                updateProductStmt.setString(5, invoiceNumber);
                updateProductStmt.setString(6, productId);

                PreparedStatement stockQtyUpdateStmt = conn.prepareStatement("UPDATE PRODUCTS SET qty = qty - ? WHERE product_id = ?");
                stockQtyUpdateStmt.setInt(1,(quantity-selectedQty));
                stockQtyUpdateStmt.setString(2,selectedPrId);

                boolean exportProductsUpdated = updateProductStmt.executeUpdate() > 0;
                boolean stocksQtyUpdated = stockQtyUpdateStmt.executeUpdate() > 0;

                conn.setAutoCommit(false);
                if (exportProductsUpdated && stocksQtyUpdated) {
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
        if (exportsTable == null || exportsTable.getItems().isEmpty() || exportsTable.getSelectionModel().getSelectedItem() == null) {
            AlertUtils.showAlert(Alert.AlertType.INFORMATION, "Unable to update.", "Please select a row to update!");
            return;
        }

        String invoiceNumber = exportsTable.getSelectionModel().getSelectedItem().getInvoiceNo();
        try {
            String query = "SELECT * FROM EXPORTS WHERE invoice_number = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, invoiceNumber);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String customerId = rs.getString("customer_id");
                String customerName = rs.getString("customer_name");
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
                    PreparedStatement prstmt = conn.prepareStatement(FETCH_EXPORT_PRODUCTS_QUERY);
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
                updateEntryForm(customerId, customerName, address, city, state, phone, email, orderDate, invoiceDate, subTotal, paymentMode, status, productList, invoiceNumber, gstVal, netTotalVal, scrollPane);

            } else {
                AlertUtils.showAlert(Alert.AlertType.INFORMATION, "Not Found", "The selected invoice does not exist in the database.");
            }

        } catch (SQLException e) {
            DatabaseErrorHandler.handleDatabaseError(e);
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Error", "Error retrieving entry details: ");
        }
    }

    @SuppressWarnings("unchecked")
    private void updateEntryForm(String selectedCustomerId, String selectCustomerName, String selectedAddress, String selectedCity,
                                 String selectedState, String selectedPhone, String selectedEmail, String selectedOrderDate,
                                 String selectedInvoiceDate, String selectedSubTotal, String selectedPaymentMode, String selectedStatus,
                                 ArrayList<Product> selectedProductList, String selectedInvoiceNumber, int selectedGstVal, String selectedNetTotalVal,
                                 ScrollPane scrollPane) {

        Stage popupStage = new Stage();
        popupStage.setTitle("Update Entry");

        GridPane formLayout = new GridPane();
        formLayout.setPadding(new Insets(20));
        formLayout.setHgap(10);
        formLayout.setVgap(10);

        Label customerDetails = new Label("Customer Details:");
        customerDetails.setStyle("-fx-font-weight:bold;");
        Label customerName = new Label("Customer Name:");
        TextField txtCustomerName = new TextField(selectCustomerName);

        Label customerId = new Label("Customer ID:");
        TextField txtCustomerId = new TextField(selectedCustomerId);

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
                boolean updatedExport = updateExportsEntry(txtCustomerId.getText(),txtCustomerName.getText(),txtAddress.getText(),
                        txtCity.getText(), txtState.getText(), txtPhone.getText(), txtEmail.getText(), dpOrderDate.getValue().toString(),
                        dpInvoiceDate.getValue().toString(), txtSubTotal.getText(), payment.getValue(), paid.isSelected() ? "Paid" : "Pending",
                        txtInvoiceNumber.getText(), gstVal, Double.parseDouble(txtNetTotal.getText()), selectedInvoiceNumber);
                boolean invoiceUpdated = updateInvoiceProductsEntry(txtInvoiceNumber.getText(),selectedInvoiceNumber);

                // Create a nested table for the products column
                if (updatedExport && invoiceUpdated) {
                    AlertUtils.showMsg("Entry updated successfully!");
                }else{
                    conn.rollback();
                    AlertUtils.showMsg("Failed to update entry!");
                }
                popupStage.close();
                loadExportsData();
            } catch (Exception ex) {
                ex.printStackTrace();
                AlertUtils.showAlert(Alert.AlertType.ERROR, "Update Failed", "Error updating record: " + ex.getMessage());
                try {
                    conn.rollback();
                } catch (SQLException rollBackEx) {
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

        formLayout.add(customerDetails, 0, 0);
        formLayout.add(customerName, 0, 1);
        formLayout.add(txtCustomerName, 1, 1);
        formLayout.add(customerId, 2, 1);
        formLayout.add(txtCustomerId, 3, 1);
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

        printInvoiceBtn = new Button("Print");
        try(InputStream stream = getClass().getResourceAsStream("/print.png")){
            if(stream != null){
                Image printImage = new Image(stream);
                ImageView printImageView = new ImageView(printImage);
                printImageView.setFitWidth(30);
                printImageView.setFitHeight(25);
                printInvoiceBtn.setGraphic(printImageView);
            }
        }catch (Exception ex){
            System.out.println(ex.getMessage());
        }

        ArrayList<ExportBillTableEntry> tableEntries = getTableEntries(dpInvoiceDate.getValue().toString());
        setPrintInvoiceBtnAction(txtCustomerName.getText(),txtAddress.getText(),txtPhone.getText(),txtInvoiceNumber.getText(),tableEntries, selectedStatus);

        hBox.getChildren().addAll(printInvoiceBtn, btnUpdate, btnCancel);
        hBox.setPadding(new Insets(10));
        VBox.setVgrow(formLayout, javafx.scene.layout.Priority.ALWAYS);
        vBox.getChildren().addAll(formLayout, hBox);

        scrollPane.setContent(vBox);

        btnCancel.setOnAction(e -> scrollPane.setContent(initializeExportsTable(conn,sectionName,refPrimaryStage)));
    }
    private boolean updateExportsEntry(String customerId, String customerName, String address, String city,
                                       String state, String phone, String email, String orderDate, String invoiceDate,
                                       String subtotal, String paymentMode, String paymentStatus, String invoiceNumber,
                                       int gstVal, double netTotalVal, String selectedInvoiceNumber) throws SQLException{
//        System.out.println(customerId+customerName+invoiceDate+orderDate);
        try(PreparedStatement updateStmt = conn.prepareStatement(UPDATE_EXPORTS_QUERY)){
            updateStmt.setString(1, (customerId!=null) ? customerId : "");
            updateStmt.setString(2, (customerName!=null) ? customerName : "");
            updateStmt.setString(3, (address!=null) ? address : "");
            updateStmt.setString(4, (city!=null) ? city : "");
            updateStmt.setString(5, (state!=null) ? state : "");
            updateStmt.setString(6, (phone!=null) ? phone : "");
            updateStmt.setString(7, (email!=null) ? email : "");
            updateStmt.setString(8, (orderDate!=null) ? orderDate : "");
            updateStmt.setString(9, (invoiceDate!=null) ? invoiceDate : "");
            updateStmt.setString(10, (subtotal!=null) ? subtotal : "");
            updateStmt.setString(11, (paymentMode!=null) ? paymentMode : "");
            updateStmt.setString(12, (paymentStatus!=null) ? paymentStatus : "");
            updateStmt.setString(13, (invoiceNumber!=null) ? invoiceNumber : "");
            updateStmt.setInt(14, gstVal);
            updateStmt.setDouble(15, netTotalVal);
            updateStmt.setString(16, selectedInvoiceNumber);
            return updateStmt.executeUpdate() > 0;
        }
    }
    private boolean updateInvoiceProductsEntry(String invoiceNumber, String selectedInvoiceNumber) throws SQLException{
        try(PreparedStatement invoiceNumberUpdate = conn.prepareStatement("UPDATE export_products SET invoice_number =? WHERE invoice_number = ?")){
            invoiceNumberUpdate.setString(1, invoiceNumber);
            invoiceNumberUpdate.setString(2, selectedInvoiceNumber);
            return invoiceNumberUpdate.executeUpdate() > 0;
        }
    }
    //---------------------------------------------------------------------------------------------
    //---------------------------------------------------------------------------------------------

    //DELETION--------------------------------------------------------------------------------------
    public void deleteEntry() {
        if (exportsTable == null || exportsTable.getItems().isEmpty() || exportsTable.getSelectionModel().getSelectedItem() == null) {
            AlertUtils.showAlert(Alert.AlertType.INFORMATION, "Unable to delete.", "Please select a row to delete!");
            return;
        }
        Exports selectedEntry = exportsTable.getSelectionModel().getSelectedItem();

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
        try (PreparedStatement pstmt = conn.prepareStatement(DELETE_EXPORT_QUERY)) {
            pstmt.setString(1, invoiceNumber);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                // Update TableView by removing the deleted item
                loadExportsData();
                AlertUtils.showMsg("Entry deleted successfully!");
            } else {
                AlertUtils.showAlert(Alert.AlertType.ERROR,
                        "Deletion Failed", "Failed to delete the entry. Please try again.");
            }
        } catch (SQLException ex) {
            DatabaseErrorHandler.handleDatabaseError(ex);
            AlertUtils.showAlert(Alert.AlertType.ERROR,
                    "Database Error", "Error occurred while deleting entry: ");
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

    public boolean checkAvailableQty(String prId, int enteredQty) {
        try {
            String selectQtyQuery = "SELECT qty FROM products WHERE product_id = ?";
            PreparedStatement selectStmt = conn.prepareStatement(selectQtyQuery);
            selectStmt.setString(1,prId);
            ResultSet rs = selectStmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("qty") >= enteredQty;
            }
        } catch (Exception e) {
            AlertUtils.showAlert(Alert.AlertType.ERROR,"Something went wrong","Unable to fetch Product Qty from database!");
        }
        return false;
    }

    public void updateStocks() {
        try {
            String updateProductQtyQuery = "UPDATE products SET qty = qty - ? WHERE product_id = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updateProductQtyQuery);
            for (Product pr : tblProducts.getItems()) {
                updateStmt.setInt(1, pr.getQuantity());
                updateStmt.setString(2, pr.getProductID());
                if (!(updateStmt.executeUpdate() > 0)) {
                    AlertUtils.showAlert(Alert.AlertType.ERROR, "Stock Update Failed", "Failed to update stock in inventory!");
                }
            }
        } catch (SQLException e) {
            DatabaseErrorHandler.handleDatabaseError(e);
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Stock Update Failed", "Failed to update stock in inventory!");
        }
    }

    private String processDateString(String date){
        String processedDate;
        processedDate = date.substring(date.lastIndexOf('-')+1,date.lastIndexOf('-')+3) + "-" + date.substring(date.indexOf('-')+1,date.indexOf('-')+3) + "-" + "20" + date.substring(2,4);
        return processedDate;
    }

    private void handleExportsTableDoubleClick() {
        exportsTable.setRowFactory(it -> {
            TableRow<Exports> row = new TableRow<>();
            row.setOnMouseClicked(mouseEvent -> {
                if (mouseEvent.getClickCount() == 2 && !row.isEmpty()) {
                    viewUpdateEntry();
                }
            });
            return row;
        });
    }

    private void setPrintInvoiceBtnAction(String customerName, String customerAddress, String customerPhoneNumber, String invoiceNumber,
                                          ArrayList<ExportBillTableEntry> tableEntries, String paymentStatus){
        printInvoiceBtn.setOnAction( e-> {
            try{
                String address,contact;
                address = "Plot No-08, MIDC Urun Islampur- 415409, Tal-Walwa, Dist-Sangli.";
                contact = "Contact: +91 8275057797, +91 9960013301.";
                ExportBill exportBill = new ExportBill(sectionName,address,contact,
                        customerName,customerAddress,customerPhoneNumber, invoiceNumber,
                        tableEntries, paymentStatus);
                String filePath = PDFGenerator.getSaveLocation(refPrimaryStage,"");
                if (filePath==null){
                    AlertUtils.showAlert(Alert.AlertType.ERROR,"Something went wrong.","Please select a correct file path to store!");
                    return;
                }
                PDFGenerator.generateExportBillPDF(filePath,exportBill);
            }catch (Exception ex){
                System.out.println(ex);
                AlertUtils.showAlert(Alert.AlertType.ERROR,"Something went wrong.","Unable to print!");
            }
        });
    }

    private ArrayList getTableEntries(String invoiceDate){
        ArrayList<ExportBillTableEntry> entries = new ArrayList<>();
        ExportBillTableEntry entry;
        for(Product pr : tblProducts.getItems()){
            int gst = gstComboBox.getValue();
            entry = new ExportBillTableEntry(processDateString(invoiceDate),pr.getProductName(),pr.getProductID(),pr.getQuantity(),(double) pr.getPrice()*pr.getQuantity(),gst);
            entries.add(entry);
        }
        return entries;
    }

    private boolean insertInvoiceNumber(String invoiceNum){
        String insertQuery = "INSERT INTO export_invoice_numbers (invoice_number) VALUES(?)";
        String selectQuery = "SELECT * FROM export_invoice_numbers WHERE invoice_number = ?";
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
        if (exportsTable != null && exportsTable.getItems() != null) {
            importsCount = exportsTable.getItems().size() + 1;
        }
        invoiceNum = "EXP"+
                (java.time.LocalDate.now()).toString().replace("-","").substring(2)+
                String.format("%04d",importsCount);
        return invoiceNum;
    }
}