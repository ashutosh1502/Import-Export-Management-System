package com.project.application;

import com.project.utils.AlertUtils;
import com.project.models.StatementEntity;
import com.project.utils.DatabaseErrorHandler;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;

public class StatementController {

    private HBox summaryPane;
    private VBox leftPart, rightPart;
    private Label[] stats = new Label[7];
    private HBox h1, h2, searchPane;
    private Label title;
    private Label from;
    private Label to;
    private DatePicker fromDate, toDate;
    private TextField searchField;
    private Button searchBtn, processBtn, resetBtn, printBtn;
    private TableView<StatementEntity> stmtTable;
    private TableColumn<StatementEntity, String> colType;
    private TableColumn<StatementEntity, String> colPaymentStatus;
    private DecimalFormat df;
    private Connection conn;
    private String fromDateStr, toDateStr;
    private static final String FETCH_DATA_QUERY = "SELECT 'Imports' AS type, invoice_number, supplier_name as party_name, supplier_id as party_id, sub_total, payment_status, invoice_date FROM IMPORTS "
            + "UNION ALL "
            + "SELECT 'Exports' AS type, invoice_number, customer_name as party_name, customer_id as party_id, sub_total, payment_status, invoice_date FROM EXPORTS "
            + "ORDER BY invoice_date";
    private static final String FETCH_TOP_SUPPLIER = "SELECT * FROM ( " +
            "SELECT i.supplier_id, i.supplier_name, SUM(ip.quantity) as total_import_qty FROM imports i " +
            "JOIN import_products ip ON i.invoice_number = ip.invoice_number " +
            "GROUP BY i.supplier_id,i.supplier_name " +
            "ORDER BY total_import_qty DESC " +
            ") " +
            "WHERE ROWNUM = 1";
    private static final String FETCH_TOP_CUSTOMER = "SELECT * FROM ( " +
            "SELECT e.customer_id, e.customer_name, SUM(ep.price * ep.quantity) as total_purchase FROM exports e " +
            "JOIN export_products ep ON e.invoice_number = ep.invoice_number " +
            "GROUP BY e.customer_id,e.customer_name " +
            "ORDER BY total_purchase DESC " +
            ") " +
            "WHERE ROWNUM = 1";
    private static final String FETCH_NET_PROFIT = "SELECT (SELECT NVL(SUM(sub_total),0) FROM exports) - (SELECT NVL(SUM(sub_total),0) FROM imports) AS net_profit FROM dual";
    private static final String FETCH_PENDING_PAYMENTS = "SELECT * FROM (" +
            "SELECT SUM(sub_total) AS imports_pending_payment FROM imports WHERE LOWER(payment_status) = 'pending'), " +
            "(SELECT SUM(sub_total) AS exports_pending_payment FROM exports WHERE LOWER(payment_status) = 'pending')";
    private static final String FETCH_HIGHEST_IMPORT = "SELECT MAX(sub_total) AS highest_import FROM imports";
    private static final String FETCH_HIGHEST_EXPORT = "SELECT MAX(sub_total) AS highest_export FROM exports";

    public VBox initializeStatementSection(Connection connection) {
        conn = connection;

        initializeUI();
        VBox main = new VBox();
        main.getChildren().addAll(h1, h2, stmtTable, summaryPane);
        addStyles();

        return main;
    }

    private void initializeUI() {
        createHeadingPane();
        setupFilterSection();
        setupStatementsTable();
        setupSummarySection();
    }

    private void createHeadingPane() {
        title = new Label("Statements");
        searchPane = new HBox();
        searchField = new TextField();
        searchField.setPrefWidth(200);
        searchBtn = new Button();
        searchBtn.setPrefWidth(25);
        searchPane.setAlignment(Pos.CENTER);
        searchPane.getChildren().addAll(searchField, searchBtn);
        h1 = new HBox();
        h1.getChildren().addAll(title, searchPane);
    }

    private void setupFilterSection() {
        from = new Label("From: ");
        fromDate = new DatePicker();
        to = new Label("To: ");
        toDate = new DatePicker();
        processBtn = new Button("Process");
        setProcessBtnAction();
        resetBtn = new Button("Reset");
        setResetBtnAction();
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        printBtn = new Button("Print");
        h2 = new HBox();
        h2.getChildren().addAll(from, fromDate, to, toDate, processBtn, resetBtn, spacer, printBtn);
    }

    private void setupStatementsTable() {
        stmtTable = new TableView<>();
        stmtTable.setMaxHeight(450);
        stmtTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableColumn<StatementEntity, Integer> colSrno = new TableColumn<>("Sr No.");
        colType = new TableColumn<>("Transaction Type");
        TableColumn<StatementEntity, String> colInvoiceNo = new TableColumn<>("Invoice No");
        TableColumn<StatementEntity, String> colSCName = new TableColumn<>("Supplier/Customer Name");
        TableColumn<StatementEntity, String> colSCId = new TableColumn<>("Supplier/Customer Id");
        TableColumn<StatementEntity, String> colSubTotal = new TableColumn<>("Subtotal");
        colPaymentStatus = new TableColumn<>("Payment Status");
        TableColumn<StatementEntity, String> colInvoiceDate = new TableColumn<>("Invoice Date");

        colSrno.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getSrno()).asObject());
        colType.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getType()));
        colInvoiceNo.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getInvoiceNo()));
        colSCName.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getSCName()));
        colSCId.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getSCId()));
        colInvoiceDate.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getInvoiceDate()));

        df = new DecimalFormat("##,##,###");
        colSubTotal.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty("Rs. " + df.format(cellData.getValue().getSubTotal())));
        colPaymentStatus.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getPaymentStatus()));
        colSrno.setPrefWidth(20);
        colType.setPrefWidth(70);
        colInvoiceNo.setPrefWidth(70);
        colSubTotal.setPrefWidth(50);
        setColumnDataColors();

        stmtTable.getColumns().addAll(colSrno, colType, colInvoiceNo, colSCName, colSCId, colSubTotal, colPaymentStatus, colInvoiceDate);
        loadStatementsData(FETCH_DATA_QUERY);
    }

    private void setupSummarySection() {
        summaryPane = new HBox();
        leftPart = new VBox(5);
        rightPart = new VBox(5);
        stats[0] = new Label("Total Transactions:  ");
        stats[1] = new Label("Top Supplier:  ");
        stats[2] = new Label("Top Customer:  ");
        stats[3] = new Label("Net Profit:  ");
        stats[4] = new Label("Pending Payments:  ");
        stats[5] = new Label("Highest Import:  ");
        stats[6] = new Label("Highest Export:  ");
        setSummaryLabels();
        leftPart.getChildren().addAll(stats[0], stats[1], stats[2]);
        rightPart.getChildren().addAll(stats[3], stats[4], stats[5], stats[6]);
        summaryPane.getChildren().addAll(leftPart, rightPart);
    }

    private void setSummaryLabels() {
        int totalTransactions = stmtTable.getItems().size();
        stats[0].setText(stats[0].getText() + totalTransactions);
        stats[1].setText(stats[1].getText() + getTopSupplier());
        stats[2].setText(stats[2].getText() + getTopCustomer());
        stats[3].setText(stats[3].getText() + getNetProfit());
        stats[4].setText(stats[4].getText() + getPendingPayments());
        stats[5].setText(stats[5].getText() + getHighestImport());
        stats[6].setText(stats[6].getText() + getHighestExport());
    }

    public void addStyles() {
        title.setStyle("-fx-font-size:16px;-fx-font-weight:bold;");
        HBox.setMargin(searchPane, new Insets(0, 0, 0, 450));
        HBox.setMargin(from, new Insets(5, 0, 0, 0));
        HBox.setMargin(to, new Insets(5, 0, 0, 20));
        HBox.setMargin(processBtn, new Insets(0, 0, 0, 20));
        HBox.setMargin(resetBtn, new Insets(0, 0, 0, 10));
        HBox.setMargin(printBtn, new Insets(0, 10, 5, 0));
        from.setStyle("-fx-font-weight:bold;");
        to.setStyle("-fx-font-weight:bold;");
        searchBtn.setStyle("-fx-background-image: url('/search-icon.png'); " +
                "-fx-background-size: 15px; " +
                "-fx-background-repeat: no-repeat; " +
                "-fx-background-position: center;");
        processBtn.setPrefWidth(150);
        resetBtn.setPrefWidth(150);
        printBtn.setPrefWidth(150);
        h1.setPadding(new Insets(5, 0, 5, 10));
        h1.setStyle("-fx-border-color:black;-fx-border-width: 0 0 1px 0;");
        h2.setPadding(new Insets(5, 0, 0, 10));
        h2.setStyle("-fx-border-color:black;-fx-border-width: 0 0 1px 0;");

        HBox.setHgrow(leftPart, Priority.ALWAYS);
        HBox.setHgrow(rightPart, Priority.ALWAYS);
        leftPart.setPadding(new Insets(10, 10, 10, 10));
        rightPart.setPadding(new Insets(10, 10, 10, 10));
        for (Label stat : stats) {
            stat.setStyle("-fx-font-size:12px;-fx-font-weight:bold;");
        }
        stats[5].setTextFill(Color.BLUEVIOLET);
        stats[6].setTextFill(Color.BLUEVIOLET);
    }

    private void loadStatementsData(String query) {
        stmtTable.getItems().clear();
//        double totalImportsAmount = 0;
//        double totalExportsAmount = 0;

        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            int srno = 1;
            while (rs.next()) {
                StatementEntity stmtEntity = new StatementEntity(srno, rs.getString("type"),
                        rs.getString("invoice_number"), rs.getString("party_name"),
                        rs.getString("party_id"), rs.getDouble("sub_total"),
                        rs.getString("payment_status"), rs.getString("invoice_date"));
                stmtTable.getItems().add(stmtEntity);
//                if (rs.getString("type").equalsIgnoreCase("exports")) {
//                    totalExportsAmount += rs.getDouble("sub_total");
//                } else {
//                    totalImportsAmount += rs.getDouble("sub_total");
//                }
                srno += 1;
            }
            Platform.runLater(() -> {
                DecimalFormat df = new DecimalFormat("##,##,###.##");
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void setColumnDataColors() {
        colType.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    if (item.equalsIgnoreCase("imports")) {
                        setTextFill(Color.BLUE);
                    } else if (item.equalsIgnoreCase("exports")) {
                        setTextFill(Color.GREEN);
                    } else {
                        setTextFill(Color.BLACK);
                    }
                }
            }
        });
        colPaymentStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    if (item.equalsIgnoreCase("pending")) {
                        setTextFill(Color.RED);
                    } else if (item.equalsIgnoreCase("paid")) {
                        setTextFill(Color.GREEN);
                    } else {
                        setTextFill(Color.BLACK);
                    }
                }
            }
        });
    }

    private void setProcessBtnAction() {
        processBtn.setOnAction(event -> {
            if (fromDate.getValue() == null || toDate.getValue() == null) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Please select date range!", ButtonType.OK);
                alert.showAndWait();
                return;
            }
            if (fromDate.getValue().isAfter(toDate.getValue())) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Invalid Date Range!", ButtonType.OK);
                alert.showAndWait();
                return;
            }
            fromDateStr = fromDate.getValue().toString();
            toDateStr = toDate.getValue().toString();
            System.out.println(fromDateStr + " " + toDateStr);
            String rangeBasedDataQuery = "SELECT 'Imports' AS type, invoice_number, supplier_name AS party_name, supplier_id AS party_id, " +
                    "sub_total, payment_status, invoice_date FROM imports " +
                    "WHERE invoice_date BETWEEN TO_DATE('" + fromDateStr + "','YYYY-MM-DD')" + " AND TO_DATE('" + toDateStr + "','YYYY-MM-DD') " +
                    "UNION ALL " +
                    "SELECT 'Exports' AS type, invoice_number, customer_name AS party_name, customer_id AS party_id, " +
                    "sub_total, payment_status, invoice_date FROM exports " +
                    "WHERE invoice_date BETWEEN TO_DATE('" + fromDateStr + "','YYYY-MM-DD')" + " AND TO_DATE('" + toDateStr + "','YYYY-MM-DD') " +
                    "ORDER BY invoice_date";
            loadStatementsData(rangeBasedDataQuery);
        });
    }

    private void setResetBtnAction() {
        resetBtn.setOnAction(event -> {
            fromDate.setValue(null);
            toDate.setValue(null);
            searchField.setText("");
            loadStatementsData(FETCH_DATA_QUERY);
        });
    }

    private String getTopSupplier() {
        StringBuilder topSupplier = new StringBuilder();
        try {
            Statement fetchTopSupStmt = conn.createStatement();
            ResultSet rs = fetchTopSupStmt.executeQuery(FETCH_TOP_SUPPLIER);
            rs.next();
            topSupplier.append(rs.getString("supplier_name"));
            topSupplier.append(" (").append(rs.getInt("total_import_qty")).append(" Qty Imported)");
        } catch (SQLException s) {
            DatabaseErrorHandler.handleDatabaseError(s);
            AlertUtils.showAlert(Alert.AlertType.INFORMATION, "Failed!", "Failed to fetch top supplier details");
        }
        return topSupplier.toString();
    }

    private String getTopCustomer() {
        StringBuilder topCustomer = new StringBuilder();
        try {
            Statement fetchTopCustStmt = conn.createStatement();
            ResultSet rs = fetchTopCustStmt.executeQuery(FETCH_TOP_CUSTOMER);
            rs.next();
            topCustomer.append(rs.getString("customer_name"));
            topCustomer.append(" (Total purchase Rs.").append(df.format(rs.getDouble("total_purchase"))).append("/-)");
        } catch (SQLException s) {
            DatabaseErrorHandler.handleDatabaseError(s);
            AlertUtils.showAlert(Alert.AlertType.INFORMATION, "Failed!", "Failed to fetch top customer details");
        }
        return topCustomer.toString();
    }

    private String getNetProfit() {
        StringBuilder netProfit = new StringBuilder();
        try {
            Statement fetchNetProfitStmt = conn.createStatement();
            ResultSet rs = fetchNetProfitStmt.executeQuery(FETCH_NET_PROFIT);
            rs.next();
            Double value = rs.getDouble("net_profit");
            if (value < 0) {
                stats[3].setTextFill(Color.RED);
            } else {
                stats[3].setTextFill(Color.GREEN);
            }
            netProfit.append("Rs. ").append(df.format(value)).append("/-");
        } catch (SQLException s) {
            DatabaseErrorHandler.handleDatabaseError(s);
            AlertUtils.showAlert(Alert.AlertType.INFORMATION, "Failed!", "Failed to calculate net profit");
        } catch (Exception e) {
            AlertUtils.showAlert(Alert.AlertType.INFORMATION, "Something went wrong!", "Unable to calculate net profit");
        }
        return netProfit.toString();
    }

    private String getPendingPayments() {
        StringBuilder pendingPayments = new StringBuilder();
        try {
            Statement fetchPendingPaymentsStmt = conn.createStatement();
            ResultSet rs = fetchPendingPaymentsStmt.executeQuery(FETCH_PENDING_PAYMENTS);
            rs.next();
            Double pendingAmountImports = rs.getDouble("imports_pending_payment");
            Double pendingAmountExports = rs.getDouble("exports_pending_payment");
            pendingPayments.append(" (Imports: Rs.").append(df.format(pendingAmountImports)).append("/-) (Exports: Rs.").append(df.format(pendingAmountExports)).append("/-)");
        } catch (SQLException s) {
            DatabaseErrorHandler.handleDatabaseError(s);
            AlertUtils.showAlert(Alert.AlertType.INFORMATION, "Failed!", "Failed to get pending payments");
        } catch (Exception e) {
            AlertUtils.showAlert(Alert.AlertType.INFORMATION, "Something went wrong!", "Unable to get pending amounts");
        }
        return pendingPayments.toString();
    }

    private String getHighestImport() {
        StringBuilder highestImport = new StringBuilder();
        try {
            Statement fetchHighestImportStmt = conn.createStatement();
            ResultSet rs = fetchHighestImportStmt.executeQuery(FETCH_HIGHEST_IMPORT);
            rs.next();
            Double value = rs.getDouble("highest_import");
            highestImport.append(" Rs.").append(df.format(value)).append("/-");
        } catch (SQLException s) {
            DatabaseErrorHandler.handleDatabaseError(s);
            AlertUtils.showAlert(Alert.AlertType.INFORMATION, "Failed!", "Failed to get highest import");
        } catch (Exception e) {
            AlertUtils.showAlert(Alert.AlertType.INFORMATION, "Something went wrong!", "Unable to get highest import amount");
        }
        return highestImport.toString();
    }

    private String getHighestExport() {
        StringBuilder highestExport = new StringBuilder();
        try {
            Statement fetchHighestExportStmt = conn.createStatement();
            ResultSet rs = fetchHighestExportStmt.executeQuery(FETCH_HIGHEST_EXPORT);
            rs.next();
            Double value = rs.getDouble("highest_export");
            highestExport.append(" Rs.").append(df.format(value)).append("/-");
        } catch (SQLException s) {
            DatabaseErrorHandler.handleDatabaseError(s);
            AlertUtils.showAlert(Alert.AlertType.INFORMATION, "Failed!", "Failed to get highest export");
        } catch (Exception e) {
            AlertUtils.showAlert(Alert.AlertType.INFORMATION, "Something went wrong!", "Unable to get highest export amount");
        }
        return highestExport.toString();
    }
}

