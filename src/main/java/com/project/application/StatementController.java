package com.project.application;

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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;

public class StatementController {
    private VBox main;
    private HBox h1,h2,searchPane;
    private Label title,from,to;
    private DatePicker fromDate,toDate;
    private TextField searchText;
    private Button searchBtn,processBtn,clearBtn,printBtn;
    private Region spacer;
    private TableView<StatementEntity> stmtTable;
    private TableColumn<StatementEntity,Integer> colSrno,colTotalQty;
    private TableColumn<StatementEntity,String> colType,colInvoiceNo,colSCName,colSCId,colPaymentStatus,colSubTotal,colInvoiceDate;
    private DecimalFormat df;
    private Connection conn;


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
        clearBtn = new Button("Clear");
        spacer = new Region();
        HBox.setHgrow(spacer,Priority.ALWAYS);
        printBtn = new Button("Print");
//        printBtn.setAlignment(Pos.CENTER_RIGHT);
        h2 = new HBox();
        h2.getChildren().addAll(from,fromDate,to,toDate,processBtn,clearBtn,spacer,printBtn);

        setStmtTable();

        main = new VBox();
        main.getChildren().addAll(h1,h2,stmtTable);

        addStyles();
        return main;
    }

    public void addStyles(){
        title.setStyle("-fx-font-size:16px;-fx-font-weight:bold;");
        HBox.setMargin(searchPane,new Insets(0,0,0,450));
        HBox.setMargin(from,new Insets(5,0,0,0));
        HBox.setMargin(to,new Insets(5,0,0,20));
        HBox.setMargin(processBtn,new Insets(0,0,0,20));
        HBox.setMargin(clearBtn,new Insets(0,0,0,10));
        HBox.setMargin(printBtn,new Insets(0,10,5,0));
        from.setStyle("-fx-font-weight:bold;");
        to.setStyle("-fx-font-weight:bold;");
        searchBtn.setStyle("-fx-background-image: url('/search-icon.png'); " +
                "-fx-background-size: 15px; " +
                "-fx-background-repeat: no-repeat; " +
                "-fx-background-position: center;");
        processBtn.setPrefWidth(150);
        clearBtn.setPrefWidth(150);
        printBtn.setPrefWidth(150);
        h1.setPadding(new Insets(5,0,5,10));
        h1.setStyle("-fx-border-color:black;-fx-border-width: 0 0 1px 0;");
        h2.setPadding(new Insets(5,0,0,10));
        h2.setStyle("-fx-border-color:black;-fx-border-width: 0 0 1px 0;");
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

        stmtTable.getColumns().addAll(colSrno,colType,colInvoiceNo,colSCName,colSCId,colSubTotal,colPaymentStatus,colInvoiceDate);

        loadStatementsData();
    }

    private void loadStatementsData(){
        stmtTable.getItems().clear();
        String query = "SELECT 'Imports' AS type, invoice_number, supplier_name as party_name, supplier_id as party_id, sub_total, payment_status, invoice_date FROM imports "
                + "UNION ALL "
                + "SELECT 'Exports' AS type, invoice_number, customer_name as party_name, customer_id as party_id, sub_total, payment_status, invoice_date FROM exports "
                + "ORDER BY invoice_date";
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
                srno+=1;
            }
        }catch (Exception ex){
            System.out.println("StatementController:135 \n"+ex);
        }
    }
}

