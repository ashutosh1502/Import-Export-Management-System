package com.project.application;

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
import java.sql.Statement;
import java.text.DecimalFormat;

public class StatementController {
    private VBox main,v1;
    private HBox h1,h2,searchPane;
    private Label title,from,to, totalImportsAmountLbl = new Label(), totalExportsAmountLbl = new Label();
    private DatePicker fromDate,toDate;
    private TextField searchText;
    private Button searchBtn,processBtn, resetBtn,printBtn;
    private Region spacer;
    private TableView<StatementEntity> stmtTable;
    private TableColumn<StatementEntity,Integer> colSrno,colTotalQty;
    private TableColumn<StatementEntity,String> colType,colInvoiceNo,colSCName,colSCId,colPaymentStatus,colSubTotal,colInvoiceDate;
    private DecimalFormat df;
    private Connection conn;
    private double totalImportsAmount,totalExportsAmount;
    private String fromDateStr,toDateStr;
    private String loadDataQuery = "SELECT 'Imports' AS type, invoice_number, supplier_name as party_name, supplier_id as party_id, sub_total, payment_status, invoice_date FROM imports "
            + "UNION ALL "
            + "SELECT 'Exports' AS type, invoice_number, customer_name as party_name, customer_id as party_id, sub_total, payment_status, invoice_date FROM exports "
            + "ORDER BY invoice_date";

    public VBox loadComponents(Connection connection){
        conn = connection;
        title = new Label("Statements");

        searchPane = new HBox();
        searchText = new TextField();
        searchText.setPrefWidth(200);
        searchBtn = new Button();
        searchBtn.setPrefWidth(25);
        searchPane.setAlignment(Pos.CENTER);
        searchPane.getChildren().addAll(searchText,searchBtn);

        h1 = new HBox();
        h1.getChildren().addAll(title,searchPane);

        from = new Label("From: ");
        fromDate = new DatePicker();
        to = new Label("To: ");
        toDate = new DatePicker();
        processBtn = new Button("Process");
        setProcessBtnAction();
        resetBtn = new Button("Reset");
        setResetBtnAction();
        spacer = new Region();
        HBox.setHgrow(spacer,Priority.ALWAYS);
        printBtn = new Button("Print");
//        printBtn.setAlignment(Pos.CENTER_RIGHT);
        h2 = new HBox();
        h2.getChildren().addAll(from,fromDate,to,toDate,processBtn, resetBtn,spacer,printBtn);

        setStmtTable();

        v1 = new VBox();
        totalImportsAmountLbl.setText("Total Imports(Rs.): ");
        totalExportsAmountLbl.setText("Total Export(Rs.): ");
        v1.getChildren().addAll(totalImportsAmountLbl, totalExportsAmountLbl);

        main = new VBox();
        main.getChildren().addAll(h1,h2,stmtTable, v1);

        addStyles();
        return main;
    }

    public void addStyles(){
        title.setStyle("-fx-font-size:16px;-fx-font-weight:bold;");
        HBox.setMargin(searchPane,new Insets(0,0,0,450));
        HBox.setMargin(from,new Insets(5,0,0,0));
        HBox.setMargin(to,new Insets(5,0,0,20));
        HBox.setMargin(processBtn,new Insets(0,0,0,20));
        HBox.setMargin(resetBtn,new Insets(0,0,0,10));
        HBox.setMargin(printBtn,new Insets(0,10,5,0));
        from.setStyle("-fx-font-weight:bold;");
        to.setStyle("-fx-font-weight:bold;");
        searchBtn.setStyle("-fx-background-image: url('/search-icon.png'); " +
                "-fx-background-size: 15px; " +
                "-fx-background-repeat: no-repeat; " +
                "-fx-background-position: center;");
        processBtn.setPrefWidth(150);
        resetBtn.setPrefWidth(150);
        printBtn.setPrefWidth(150);
        h1.setPadding(new Insets(5,0,5,10));
        h1.setStyle("-fx-border-color:black;-fx-border-width: 0 0 1px 0;");
        h2.setPadding(new Insets(5,0,0,10));
        h2.setStyle("-fx-border-color:black;-fx-border-width: 0 0 1px 0;");
        totalImportsAmountLbl.setStyle("-fx-font-size:16px;-fx-font-weight:bold;-fx-text-fill:blue;");
        totalExportsAmountLbl.setStyle("-fx-font-size:16px;-fx-font-weight:bold;-fx-text-fill:green;");
        VBox.setMargin(totalImportsAmountLbl,new Insets(0,0,0,10));
        VBox.setMargin(totalExportsAmountLbl,new Insets(0,0,0,10));
    }

    private void setStmtTable(){
        stmtTable = new TableView<>();
        stmtTable.setMaxHeight(450);
        stmtTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        colSrno = new TableColumn<>("Sr No.");
        colType = new TableColumn<>("Transaction Type");
        colInvoiceNo = new TableColumn<>("Invoice No");
        colSCName = new TableColumn<>("Supplier/Customer Name");
        colSCId = new TableColumn<>("Supplier/Customer Id");
        colSubTotal = new TableColumn<>("Subtotal");
        colPaymentStatus = new TableColumn<>("Payment Status");
        colInvoiceDate = new TableColumn<>("Invoice Date");

        colSrno.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getSrno()).asObject());
        colType.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getType()));
        colInvoiceNo.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getInvoiceNo()));
        colSCName.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getSCName()));
        colSCId.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getSCId()));
        colInvoiceDate.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getInvoiceDate()));

        df = new DecimalFormat("##,##,###");
        colSubTotal.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty("Rs. "+df.format(cellData.getValue().getSubTotal())));
        colPaymentStatus.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getPaymentStatus()));
        colSrno.setPrefWidth(20);
        colType.setPrefWidth(70);
        colInvoiceNo.setPrefWidth(70);
        colSubTotal.setPrefWidth(50);
        setColumnDataColors();

        stmtTable.getColumns().addAll(colSrno,colType,colInvoiceNo,colSCName,colSCId,colSubTotal,colPaymentStatus,colInvoiceDate);

        loadStatementsData(loadDataQuery);
    }

    private void loadStatementsData(String query){
        stmtTable.getItems().clear();
        totalImportsAmount = 0;
        totalExportsAmount = 0;

        try{
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            int srno = 1;
            while(rs.next()){
                StatementEntity stmtEntity = new StatementEntity(srno,rs.getString("type"),
                        rs.getString("invoice_number"), rs.getString("party_name"),
                        rs.getString("party_id"), rs.getDouble("sub_total"),
                        rs.getString("payment_status"), rs.getString("invoice_date"));
                stmtTable.getItems().add(stmtEntity);
                if(rs.getString("type").equalsIgnoreCase("exports")){
                    totalExportsAmount += rs.getDouble("sub_total");
                }else{
                    totalImportsAmount += rs.getDouble("sub_total");
                }
                srno+=1;
            }
            Platform.runLater(() -> {
                DecimalFormat df = new DecimalFormat("##,##,###.##");
                totalImportsAmountLbl.setText("Total Imports(Rs.): " + df.format(totalImportsAmount)+"/-");
                totalExportsAmountLbl.setText("Total Exports(Rs.): " + df.format(totalExportsAmount)+"/-");
            });
        }catch (Exception ex){
            System.out.println("StatementController.loadStatementsData:182 \n"+ex);
            ex.printStackTrace();
        }
    }

    private void setColumnDataColors(){
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
                        // Default color if not "imports" or "exports"
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
                        // Default color if not "imports" or "exports"
                        setTextFill(Color.BLACK);
                    }
                }
            }
        });
    }

    private void setProcessBtnAction(){
        processBtn.setOnAction(event->{
            if(fromDate.getValue()==null || toDate.getValue()==null){
                Alert alert = new Alert(Alert.AlertType.WARNING,"Please select date range!",ButtonType.OK);
                alert.showAndWait();
                return;
            }
            if(fromDate.getValue().isAfter(toDate.getValue())){
                Alert alert = new Alert(Alert.AlertType.WARNING,"Invalid Date Range!",ButtonType.OK);
                alert.showAndWait();
                return;
            }
            fromDateStr = fromDate.getValue().toString();
            toDateStr = toDate.getValue().toString();
            System.out.println(fromDateStr+" "+toDateStr);
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

    private void setResetBtnAction(){
        resetBtn.setOnAction(event->{
            fromDate.setValue(null);
            toDate.setValue(null);
            searchText.setText("");
            loadStatementsData(loadDataQuery);
        });
    }
}

