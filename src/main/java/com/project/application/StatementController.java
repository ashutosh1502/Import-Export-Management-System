package com.project.application;

import com.project.models.*;
import com.project.utils.AlertUtils;
import com.project.utils.DatabaseErrorHandler;
import com.project.utils.PDFGenerator;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Locale;

public class StatementController {

    private HBox summaryPane = new HBox();
    private VBox leftPart, rightPart;
    private Label[] stats = new Label[7];
    private HBox h1, h2, searchPane;
    private SplitPane bodyPane;
    private Label title;
    private Label from;
    private Label to;
    private DatePicker fromDate, toDate;
    private TextField searchField;
    private Button searchBtn, processBtn, resetBtn, printBtn;
    private ToggleGroup viewImpExpBtnGrp;
    private ToggleButton viewImportsBtn, viewExportsBtn;
    private TableView<StatementEntity> stmtTable;
    private TableColumn<StatementEntity, String> colType, colSCName, colSCId, colPaymentStatus;
    private DecimalFormat df;
    private Connection conn;
    private String fromDateStr = "", toDateStr = "", sectionName, currentSearchTerm = "";
    private Stage refStage;
    private static final String FETCH_DATA_QUERY = "SELECT 'Imports' AS type, invoice_number, supplier_name as party_name, supplier_id as party_id, net_total, payment_status, invoice_date FROM IMPORTS "
            + "UNION ALL "
            + "SELECT 'Exports' AS type, invoice_number, customer_name as party_name, customer_id as party_id, net_total, payment_status, invoice_date FROM EXPORTS "
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
    private static final String FETCH_NET_PROFIT = "SELECT (SELECT NVL(SUM(net_total),0) FROM exports) - (SELECT NVL(SUM(net_total),0) FROM imports) AS net_profit FROM dual";
    private static final String FETCH_PENDING_PAYMENTS = "SELECT * FROM (" +
            "SELECT SUM(net_total) AS imports_pending_payment FROM imports WHERE LOWER(payment_status) = 'pending'), " +
            "(SELECT SUM(net_total) AS exports_pending_payment FROM exports WHERE LOWER(payment_status) = 'pending')";
    private static final String FETCH_HIGHEST_IMPORT = "SELECT MAX(net_total) AS highest_import FROM imports";
    private static final String FETCH_HIGHEST_EXPORT = "SELECT MAX(net_total) AS highest_export FROM exports";


    public VBox initializeStatementSection(Connection connection, String sectionName, Stage refStage) {
        conn = connection;
        this.sectionName = sectionName;
        this.refStage = refStage;

        initializeUI();
        VBox main = new VBox();
        bodyPane = new SplitPane();
        bodyPane.setOrientation(Orientation.VERTICAL);
        bodyPane.getItems().addAll(stmtTable,summaryPane);
        main.getChildren().addAll(h1, h2, bodyPane);
        addStyles();

        return main;
    }

    private void initializeUI() {
        createHeadingPane();
        setupStatementsTable();
        setupFilterSection();
        setupSummarySection(FETCH_NET_PROFIT,FETCH_TOP_SUPPLIER,FETCH_TOP_CUSTOMER,FETCH_PENDING_PAYMENTS,FETCH_HIGHEST_IMPORT,FETCH_HIGHEST_EXPORT);
    }

    private void createHeadingPane() {
        title = new Label("Statements");
        searchPane = new HBox();
        searchField = new TextField();
        searchField.setPrefWidth(200);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if(newVal.isEmpty())
                resetBtn.fire();
            currentSearchTerm = newVal.toLowerCase();
            stmtTable.refresh();
        });
        searchField.setOnAction(e -> searchBtn.fire());
        searchBtn = new Button();
        searchBtn.setPrefWidth(25);
        setSearchBtnAction();
        searchPane.setAlignment(Pos.CENTER);
        searchPane.getChildren().addAll(searchField, searchBtn);
        h1 = new HBox();
        h1.getChildren().addAll(title, searchPane);
    }

    private void setupFilterSection() {
        from = new Label("From: ");
        fromDate = new DatePicker();
        fromDate.valueProperty().addListener((obc,oldDate,newDate) -> {
            if(newDate != null){
                fromDateStr = fromDate.getValue().toString();
            }
        });
        to = new Label("To: ");
        toDate = new DatePicker();
        toDate.valueProperty().addListener((obc,oldDate,newDate) -> {
            if(newDate != null){
                toDateStr = toDate.getValue().toString();
            }
        });
        processBtn = new Button("Process");
        setProcessBtnAction();
        resetBtn = new Button("Reset");
        setResetBtnAction();
        viewImportsBtn = new ToggleButton("Imports");
        viewExportsBtn = new ToggleButton("Exports");
        viewImpExpBtnGrp = new ToggleGroup();
        viewImpExpBtnGrp.getToggles().addAll(viewImportsBtn,viewExportsBtn);
        setToggleBtnsAction();
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        printBtn = new Button("Print Statement");
        setPrintStmtBtnAction();
        h2 = new HBox();
        h2.getChildren().addAll(viewImportsBtn, viewExportsBtn,from, fromDate, to, toDate, processBtn, resetBtn, spacer, printBtn);
    }

    private void setupStatementsTable() {
        stmtTable = new TableView<>();
        stmtTable.setRowFactory(tv -> {
            TableRow<StatementEntity> row = new TableRow<>();
            row.setPrefHeight(25);
            return row;
        });
        stmtTable.setMinHeight(300);
        stmtTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableColumn<StatementEntity, Integer> colSrno = new TableColumn<>("Sr No.");
        colType = new TableColumn<>("Transaction Type");
        TableColumn<StatementEntity, String> colInvoiceNo = new TableColumn<>("Invoice No");
        colSCName = new TableColumn<>("Supplier/Customer Name");
        colSCName.setCellValueFactory(new PropertyValueFactory<>("supplierCustomerName"));
        makeColumnSearchable(colSCName);
        colSCId = new TableColumn<>("Supplier/Customer Id");
        colSCName.setCellValueFactory(new PropertyValueFactory<>("supplierCustomerId"));
        makeColumnSearchable(colSCName);
        TableColumn<StatementEntity, String> colNetTotal = new TableColumn<>("Net Total");
        colPaymentStatus = new TableColumn<>("Payment Status");
        TableColumn<StatementEntity, String> colInvoiceDate = new TableColumn<>("Invoice Date");

        colSrno.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getSrno()).asObject());
        colType.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getType()));
        colInvoiceNo.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getInvoiceNo()));
        colSCName.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getSCName()));
        colSCId.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getSCId()));
        colInvoiceDate.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getInvoiceDate()));

        df = new DecimalFormat("##,##,###");
        colNetTotal.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty("Rs. " + df.format(cellData.getValue().getNetTotal())));
        colPaymentStatus.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getPaymentStatus()));
        colSrno.setPrefWidth(20);
        colType.setPrefWidth(70);
        colInvoiceNo.setPrefWidth(70);
        colNetTotal.setPrefWidth(50);
        setColumnDataColors();

        stmtTable.getColumns().addAll(colSrno, colType, colInvoiceNo, colSCName, colSCId, colNetTotal, colPaymentStatus, colInvoiceDate);
        loadStatementsData(FETCH_DATA_QUERY);
    }

    private void setupSummarySection(String netProfitQuery,String topSupplQuery, String topCustQuery, String pendingPaymentsQuery, String highestImportQuery, String highestExportQuery) {
        leftPart = new VBox(5);
        rightPart = new VBox(5);
        stats[0] = new Label("Total Transactions:  ");
        stats[1] = new Label("Top Supplier:  ");
        stats[2] = new Label("Top Customer:  ");
        stats[3] = new Label("Net Profit:  ");
        stats[4] = new Label("Pending Payments:  ");
        stats[5] = new Label("Highest Import:  ");
        stats[6] = new Label("Highest Export:  ");
        setSummaryLabels(netProfitQuery,topSupplQuery,topCustQuery,pendingPaymentsQuery,highestImportQuery,highestExportQuery);
        leftPart.getChildren().clear();
        rightPart.getChildren().clear();
        summaryPane.getChildren().clear();
        leftPart.getChildren().addAll(stats[0], stats[1], stats[2]);
        rightPart.getChildren().addAll(stats[3], stats[4], stats[5], stats[6]);
        summaryPane.getChildren().addAll(leftPart, rightPart);
    }

    private void setSummaryLabels(String netProfitQuery,String topSupplQuery, String topCustQuery, String pendingPaymentsQuery, String highestImportQuery, String highestExportQuery) {
        int totalTransactions = stmtTable.getItems().size();
        stats[0].setText(stats[0].getText() + totalTransactions);
        stats[1].setText(stats[1].getText() + getTopSupplier(topSupplQuery));
        stats[2].setText(stats[2].getText() + getTopCustomer(topCustQuery));
        stats[3].setText(stats[3].getText() + getNetProfit(netProfitQuery));
        stats[4].setText(stats[4].getText() + getPendingPayments(pendingPaymentsQuery));
        stats[5].setText(stats[5].getText() + getHighestImport(highestImportQuery));
        stats[6].setText(stats[6].getText() + getHighestExport(highestExportQuery));
    }



    private void loadStatementsData(String query) {
        stmtTable.getItems().clear();
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            int srno = 1;
            while (rs.next()) {
                StatementEntity stmtEntity = new StatementEntity(srno, rs.getString("type"),
                        rs.getString("invoice_number"), rs.getString("party_name"),
                        rs.getString("party_id"), rs.getDouble("net_total"),
                        rs.getString("payment_status"), processDateString(rs.getString("invoice_date")));
                stmtTable.getItems().add(stmtEntity);
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

    private void setSearchBtnAction(){
        searchBtn.setOnAction(e -> {
            if(currentSearchTerm!=null && !currentSearchTerm.isEmpty()){
                String FETCH_FILTERED_DATA_QUERY = "SELECT 'Imports' AS type, invoice_number, supplier_name as party_name, supplier_id as party_id, net_total, payment_status, invoice_date FROM IMPORTS WHERE (supplier_id LIKE '%"+currentSearchTerm+"%' OR supplier_name LIKE '%"+currentSearchTerm+"%') " +
                        " UNION ALL " +
                        " SELECT 'Exports' AS type, invoice_number, customer_name as party_name, customer_id as party_id, net_total, payment_status, invoice_date FROM EXPORTS WHERE (customer_id LIKE '%"+currentSearchTerm+"%' OR customer_name LIKE '%"+currentSearchTerm+"%') " +
                        " ORDER BY invoice_date ";
                String RANGE_BASED_FILTER_QUERY = "SELECT 'Imports' AS type, invoice_number, supplier_name AS party_name, supplier_id AS party_id, " +
                        "net_total, payment_status, invoice_date FROM imports " +
                        "WHERE (supplier_id LIKE '%"+currentSearchTerm+"%' OR supplier_name LIKE '%"+currentSearchTerm+"%') AND (invoice_date BETWEEN TO_DATE('" + fromDateStr + "','YYYY-MM-DD')" + " AND TO_DATE('" + toDateStr + "','YYYY-MM-DD')) " +
                        "UNION ALL " +
                        "SELECT 'Exports' AS type, invoice_number, customer_name AS party_name, customer_id AS party_id, " +
                        "net_total, payment_status, invoice_date FROM exports " +
                        "WHERE (customer_id LIKE '%"+currentSearchTerm+"%' OR customer_name LIKE '%"+currentSearchTerm+"%') AND (invoice_date BETWEEN TO_DATE('" + fromDateStr + "','YYYY-MM-DD')" + " AND TO_DATE('" + toDateStr + "','YYYY-MM-DD')) " +
                        "ORDER BY invoice_date";
                if(fromDateStr != null && toDateStr != null &&
                        !fromDateStr.isEmpty() && !toDateStr.isEmpty()){
                    loadStatementsData(RANGE_BASED_FILTER_QUERY);
                }else{
                    loadStatementsData(FETCH_FILTERED_DATA_QUERY);
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
//            fromDateStr = fromDate.getValue().toString();
//            toDateStr = toDate.getValue().toString();
            String RANGE_BASED_FILTER_QUERY = "SELECT 'Imports' AS type, invoice_number, supplier_name AS party_name, supplier_id AS party_id, " +
                    "net_total, payment_status, invoice_date FROM imports " +
                    "WHERE (supplier_id LIKE '%"+currentSearchTerm+"%' OR supplier_name LIKE '%"+currentSearchTerm+"%') AND (invoice_date BETWEEN TO_DATE('" + fromDateStr + "','YYYY-MM-DD')" + " AND TO_DATE('" + toDateStr + "','YYYY-MM-DD')) " +
                    "UNION ALL " +
                    "SELECT 'Exports' AS type, invoice_number, customer_name AS party_name, customer_id AS party_id, " +
                    "net_total, payment_status, invoice_date FROM exports " +
                    "WHERE (customer_id LIKE '%"+currentSearchTerm+"%' OR customer_name LIKE '%"+currentSearchTerm+"%') AND (invoice_date BETWEEN TO_DATE('" + fromDateStr + "','YYYY-MM-DD')" + " AND TO_DATE('" + toDateStr + "','YYYY-MM-DD')) " +
                    "ORDER BY invoice_date";
            loadStatementsData(RANGE_BASED_FILTER_QUERY);
            String rangeBasedTopSuppQuery = "SELECT * FROM ( " +
                    "SELECT i.supplier_id, i.supplier_name, SUM(ip.quantity) as total_import_qty FROM imports i " +
                            "JOIN import_products ip ON i.invoice_number = ip.invoice_number " +
                            "WHERE (supplier_id LIKE '%"+currentSearchTerm+"%' OR supplier_name LIKE '%"+currentSearchTerm+"%') AND (i.invoice_date BETWEEN TO_DATE('"+fromDateStr+"','YYYY-MM-DD') AND TO_DATE('"+toDateStr+"','YYYY-MM-DD'))"+
                            "GROUP BY i.supplier_id,i.supplier_name " +
                            "ORDER BY total_import_qty DESC " +
                            ") " +
                            "WHERE ROWNUM = 1";
            String rangeBasedTopCustQuery = "SELECT * FROM ( " +
                    "SELECT e.customer_id, e.customer_name, SUM(ep.price * ep.quantity) as total_purchase FROM exports e " +
                    "JOIN export_products ep ON e.invoice_number = ep.invoice_number " +
                    "WHERE (customer_id LIKE '%"+currentSearchTerm+"%' OR customer_name LIKE '%"+currentSearchTerm+"%') AND (e.invoice_date BETWEEN TO_DATE('"+fromDateStr+"','YYYY-MM-DD') AND TO_DATE('"+toDateStr+"','YYYY-MM-DD'))"+
                    "GROUP BY e.customer_id,e.customer_name " +
                    "ORDER BY total_purchase DESC " +
                    ") " +
                    "WHERE ROWNUM = 1";
            String rangeBasedNetProfitQuery = "SELECT " +
                    "(SELECT NVL(SUM(net_total),0) FROM exports WHERE (customer_id LIKE '%"+currentSearchTerm+"%' OR customer_name LIKE '%"+currentSearchTerm+"%') AND (invoice_date BETWEEN TO_DATE('"+fromDateStr+"','YYYY-MM-DD') AND TO_DATE('"+toDateStr+"','YYYY-MM-DD'))) -" +
                    "(SELECT NVL(SUM(net_total),0) FROM imports WHERE (supplier_id LIKE '%"+currentSearchTerm+"%' OR supplier_name LIKE '%"+currentSearchTerm+"%') AND (invoice_date BETWEEN TO_DATE('"+fromDateStr+"','YYYY-MM-DD') AND TO_DATE('"+toDateStr+"','YYYY-MM-DD'))) " +
                    "AS net_profit FROM dual";
            String rangeBasedPendingPaymentsQuery = "SELECT * FROM" +
                    "(SELECT SUM(net_total) AS imports_pending_payment FROM imports WHERE (supplier_id LIKE '%"+currentSearchTerm+"%' OR supplier_name LIKE '%"+currentSearchTerm+"%') AND LOWER(payment_status) = 'pending' AND invoice_date BETWEEN TO_DATE('"+fromDateStr+"','YYYY-MM-DD') AND TO_DATE('"+toDateStr+"','YYYY-MM-DD')), " +
                    "(SELECT SUM(net_total) AS exports_pending_payment FROM exports WHERE (customer_id LIKE '%"+currentSearchTerm+"%' OR customer_name LIKE '%"+currentSearchTerm+"%') AND LOWER(payment_status) = 'pending' AND invoice_date BETWEEN TO_DATE('"+fromDateStr+"','YYYY-MM-DD') AND TO_DATE('"+toDateStr+"','YYYY-MM-DD'))";
            String rangeBasedHighestImportQuery = "SELECT MAX(net_total) AS highest_import FROM imports WHERE (supplier_id LIKE '%"+currentSearchTerm+"%' OR supplier_name LIKE '%"+currentSearchTerm+"%') AND (invoice_date BETWEEN TO_DATE('"+fromDateStr+"','YYYY-MM-DD') AND TO_DATE('"+toDateStr+"','YYYY-MM-DD'))";
            String rangeBasedHighestExportQuery = "SELECT MAX(net_total) AS highest_export FROM exports WHERE (customer_id LIKE '%"+currentSearchTerm+"%' OR customer_name LIKE '%"+currentSearchTerm+"%') AND (invoice_date BETWEEN TO_DATE('"+fromDateStr+"','YYYY-MM-DD') AND TO_DATE('"+toDateStr+"','YYYY-MM-DD'))";
            setupSummarySection(rangeBasedNetProfitQuery,rangeBasedTopSuppQuery,rangeBasedTopCustQuery,rangeBasedPendingPaymentsQuery,rangeBasedHighestImportQuery,rangeBasedHighestExportQuery);
            addStyles();
            setPrintStmtBtnAction();
        });
    }

    private void setResetBtnAction() {
        resetBtn.setOnAction(event -> {
            fromDate.setValue(null);
            toDate.setValue(null);
            searchField.setText("");
            viewImportsBtn.setSelected(false);
            viewExportsBtn.setSelected(false);
            loadStatementsData(FETCH_DATA_QUERY);
            setupSummarySection(FETCH_NET_PROFIT,FETCH_TOP_SUPPLIER,FETCH_TOP_CUSTOMER,FETCH_PENDING_PAYMENTS,FETCH_HIGHEST_IMPORT,FETCH_HIGHEST_EXPORT);
            addStyles();
            setPrintStmtBtnAction();
        });
    }

    private String getTopSupplier(String query) {
        StringBuilder topSupplier = new StringBuilder();
        try {
            Statement fetchTopSupStmt = conn.createStatement();
            ResultSet rs = fetchTopSupStmt.executeQuery(query);
            rs.next();
            topSupplier.append(rs.getString("supplier_name"));
            topSupplier.append(" (").append(rs.getInt("total_import_qty")).append(" Qty Imported)");
        } catch (SQLException s) {
            DatabaseErrorHandler.handleDatabaseError(s);
            AlertUtils.showAlert(Alert.AlertType.INFORMATION, "Failed!", "Failed to fetch top supplier details");
        }
        return topSupplier.toString();
    }

    private String getTopCustomer(String query) {
        StringBuilder topCustomer = new StringBuilder();
        try {
            Statement fetchTopCustStmt = conn.createStatement();
            ResultSet rs = fetchTopCustStmt.executeQuery(query);
            rs.next();
            topCustomer.append(rs.getString("customer_name"));
            topCustomer.append(" (Total purchase Rs.").append(df.format(rs.getDouble("total_purchase"))).append("/-)");
        } catch (SQLException s) {
            DatabaseErrorHandler.handleDatabaseError(s);
            AlertUtils.showAlert(Alert.AlertType.INFORMATION, "Failed!", "Failed to fetch top customer details");
        }
        return topCustomer.toString();
    }

    private String getNetProfit(String query) {
        StringBuilder netProfit = new StringBuilder();
        try {
            Statement fetchNetProfitStmt = conn.createStatement();
            ResultSet rs = fetchNetProfitStmt.executeQuery(query);
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

    private String getPendingPayments(String query) {
        StringBuilder pendingPayments = new StringBuilder();
        try {
            Statement fetchPendingPaymentsStmt = conn.createStatement();
            ResultSet rs = fetchPendingPaymentsStmt.executeQuery(query);
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

    private String getHighestImport(String query) {
        StringBuilder highestImport = new StringBuilder();
        try {
            Statement fetchHighestImportStmt = conn.createStatement();
            ResultSet rs = fetchHighestImportStmt.executeQuery(query);
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

    private String getHighestExport(String query) {
        StringBuilder highestExport = new StringBuilder();
        try {
            Statement fetchHighestExportStmt = conn.createStatement();
            ResultSet rs = fetchHighestExportStmt.executeQuery(query);
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

    private void setToggleBtnsAction(){
        viewImportsBtn.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            if (isSelected) {
                viewImportsBtn.setText("✓Imports");
                viewImportsBtn.setStyle("-fx-background-color: rgb(130,190,255); -fx-opacity: 1; -fx-font-weight: bold;");
                StringBuilder FETCH_ONLY_IMPORTS = new StringBuilder("SELECT 'Imports' AS type, invoice_number, supplier_name AS party_name, supplier_id AS party_id, " +
                        "net_total, payment_status, invoice_date FROM imports WHERE 1=1 ");
                if(currentSearchTerm != null && !currentSearchTerm.isEmpty()){
                    FETCH_ONLY_IMPORTS.append(" AND (supplier_id LIKE '%").append(currentSearchTerm).append("%' OR supplier_name LIKE '%").append(currentSearchTerm).append("%')");
                }
//                System.out.println(fromDateStr + toDateStr);
                if(fromDateStr != null && toDateStr != null &&
                        !fromDateStr.isEmpty() && !toDateStr.isEmpty()){
                    FETCH_ONLY_IMPORTS.append(" AND (invoice_date BETWEEN TO_DATE('").append(fromDateStr).append("','YYYY-MM-DD')").append(" AND TO_DATE('").append(toDateStr).append("','YYYY-MM-DD'))");
                }
//                System.out.println(FETCH_ONLY_IMPORTS);
                loadStatementsData(FETCH_ONLY_IMPORTS.toString());
            } else {
                viewImportsBtn.setText("Imports");
                viewImportsBtn.setStyle("-fx-background-color: rgb(130,190,255); -fx-opacity: 0.6;");
                loadStatementsData(FETCH_DATA_QUERY);
            }
            setPrintStmtBtnAction();
        });
        viewExportsBtn.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            if (isSelected) {
                viewExportsBtn.setText("✓Exports");
                viewExportsBtn.setStyle("-fx-background-color: rgb(130,190,255); -fx-opacity: 1; -fx-font-weight: bold;");
                StringBuilder FETCH_ONLY_EXPORTS = new StringBuilder("SELECT 'Exports' AS type, invoice_number, customer_name AS party_name, customer_id AS party_id, " +
                        "net_total, payment_status, invoice_date FROM exports WHERE 1=1 ");
                if(currentSearchTerm != null && !currentSearchTerm.isEmpty()){
                    FETCH_ONLY_EXPORTS.append(" AND (customer_id LIKE '%").append(currentSearchTerm).append("%' OR customer_name LIKE '%").append(currentSearchTerm).append("%')");
                }
                if(fromDateStr != null && toDateStr != null &&
                        !fromDateStr.isEmpty() && !toDateStr.isEmpty()){
                    FETCH_ONLY_EXPORTS.append(" AND (invoice_date BETWEEN TO_DATE('").append(fromDateStr).append("','YYYY-MM-DD')").append(" AND TO_DATE('").append(toDateStr).append("','YYYY-MM-DD'))");
                }
                loadStatementsData(FETCH_ONLY_EXPORTS.toString());
            } else {
                viewExportsBtn.setText("Exports");
                viewExportsBtn.setStyle("-fx-background-color: rgb(130,190,255); -fx-opacity: 0.6;");
                loadStatementsData(FETCH_DATA_QUERY);
            }
            setPrintStmtBtnAction();
        });
    }

    private void setPrintStmtBtnAction(){
        ArrayList<StatementBillTableEntry> entries = getTableEntries();
        printBtn.setOnAction( e ->{
            try{
                String address,contact;
                address = "Plot No-08, MIDC Urun Islampur- 415409, Tal-Walwa, Dist-Sangli.";
                contact = "Contact: +91 8275057797, +91 9960013301.";
                StatementBill stmtBill = new StatementBill(sectionName,address,contact,processDateString(fromDateStr)+" to "+processDateString(toDateStr), entries);
                StringBuilder defaultFilename = new StringBuilder( (fromDateStr.isEmpty() || toDateStr.isEmpty()) ? "" : "Statement-"+processDateString(fromDateStr)+"-"+processDateString(toDateStr));
                if(viewImportsBtn.isSelected())
                    defaultFilename.insert(0,"Imports-");
                if (viewExportsBtn.isSelected())
                    defaultFilename.insert(0,"Exports-");
                if(currentSearchTerm !=null && !currentSearchTerm.isEmpty())
                    defaultFilename.insert(0,currentSearchTerm.toUpperCase()+"-");
                String filePath = PDFGenerator.getSaveLocation(refStage,defaultFilename.toString());
                if (filePath==null){
                    AlertUtils.showAlert(Alert.AlertType.ERROR,"Something went wrong.","Please select a correct file path to store!");
                    return;
                }
                PDFGenerator.generateStatementsPDF(filePath,stmtBill);
            }catch (Exception ex){
                System.out.println(ex);
                AlertUtils.showAlert(Alert.AlertType.ERROR,"Something went wrong.","Unable to print!");
            }
        });
    }

    private <T> void makeColumnSearchable(TableColumn<StatementEntity, T> column) {
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    String cellText = item.toString();
                    if (currentSearchTerm != null && !currentSearchTerm.isEmpty()
                            && cellText.toLowerCase().contains(currentSearchTerm.toLowerCase())) {
                        TextFlow textFlow = buildHighlightedText(cellText, currentSearchTerm);
                        textFlow.setLineSpacing(0);
                        textFlow.setPadding(Insets.EMPTY);
                        textFlow.setPrefWidth(getTableColumn().getWidth() - 10); // avoids wrapping
                        setGraphic(textFlow);
                        setText(null);
                        setAlignment(Pos.CENTER_LEFT);

                    } else {
                        setText(cellText);
                        setGraphic(null);
                        setContentDisplay(ContentDisplay.TEXT_ONLY);
                        setAlignment(Pos.CENTER_LEFT);
                    }
                }
            }
        });
    }

    private TextFlow buildHighlightedText(String text, String searchTerm) {
        int startIndex = text.toLowerCase().indexOf(searchTerm.toLowerCase());
        if (startIndex < 0) return new TextFlow(new Text(text));

        Text before = new Text(text.substring(0, startIndex));
        Text match = new Text(text.substring(startIndex, startIndex + searchTerm.length()));
        Text after = new Text(text.substring(startIndex + searchTerm.length()));
        match.setStyle("-fx-fill: rgb(42,230,0); -fx-font-weight: bold;");
        before.setWrappingWidth(Double.MAX_VALUE);
        match.setWrappingWidth(Double.MAX_VALUE);
        after.setWrappingWidth(Double.MAX_VALUE);
        TextFlow textFlow = new TextFlow(before, match, after);
        return textFlow;
    }

    private ArrayList getTableEntries(){
        ArrayList<StatementBillTableEntry> entries = new ArrayList<>();
        StatementBillTableEntry entry;
        for(StatementEntity stmtEnt : stmtTable.getItems()){
            entry = new StatementBillTableEntry(stmtEnt.getInvoiceDate(),stmtEnt.getType(),stmtEnt.getInvoiceNo(),
                    stmtEnt.getNetTotal());
            entries.add(entry);
        }
        return entries;
    }

    private String processDateString(String date){
        if(date.isEmpty()) return "";
        String processedDate;
        processedDate = date.substring(date.lastIndexOf('-')+1,date.lastIndexOf('-')+3) + "-" + date.substring(date.indexOf('-')+1,date.indexOf('-')+3) + "-" + "20" + date.substring(2,4);
        return processedDate;
    }

    //CSS STYLING----------------------------------------------------------------------------------------------
    private void addStyles() {
        title.setStyle("-fx-font-size:16px;-fx-font-weight:bold;");
        HBox.setMargin(searchPane, new Insets(0, 0, 0, 450));
        HBox.setMargin(from, new Insets(5, 0, 0, 10));
        HBox.setMargin(to, new Insets(5, 0, 0, 20));
        HBox.setMargin(processBtn, new Insets(0, 0, 5, 20));
        HBox.setMargin(resetBtn, new Insets(0, 0, 5, 10));
        HBox.setMargin(viewImportsBtn, new Insets(0, 0, 5, 0));
        HBox.setMargin(viewExportsBtn, new Insets(0, 0, 5, 5));
        HBox.setMargin(printBtn, new Insets(0, 10, 5, 0));
        from.setStyle("-fx-font-weight:bold;");
        to.setStyle("-fx-font-weight:bold;");
        searchBtn.setStyle("-fx-background-image: url('/search-icon.png'); " +
                "-fx-background-size: 15px; " +
                "-fx-background-repeat: no-repeat; " +
                "-fx-background-position: center;");
        processBtn.setPrefWidth(150);
        processBtn.setStyle("-fx-background-color: rgb(210,252,210); -fx-border-color: rgb(0,252,0); -fx-border-radius: 2px;");
        resetBtn.setPrefWidth(150);
        resetBtn.setStyle("-fx-background-color: rgb(252,210,210); -fx-border-color: rgb(252,0,0); -fx-border-radius: 2px;");
        printBtn.setPrefWidth(150);
        viewImportsBtn.setPrefWidth(80);
        viewExportsBtn.setPrefWidth(80);
        viewImportsBtn.setStyle("-fx-background-color: rgb(130,190,255); -fx-opacity: 0.6;");
        viewExportsBtn.setStyle("-fx-background-color: rgb(130,190,255); -fx-opacity: 0.6;");
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
        summaryPane.setMaxHeight(140);
        summaryPane.setMinHeight(50);
        VBox.setVgrow(bodyPane,Priority.ALWAYS);
    }
}

