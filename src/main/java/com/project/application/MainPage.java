package com.project.application;

import com.project.utils.AlertUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MainPage{
    //Section Constants
    private static final String UNITECH_INDUSTRIES = "Unitech Industries";
    private static final String SURYA_INDUSTRIES = "Surya Industries";

    //UI Components
    protected Stage primaryStage;
    protected Scene mainScene;
    private HBox headingPane=new HBox(10);
    protected Label heading;
    private HBox leftHeadingPane=new HBox();
    private HBox rightHeadingPane=new HBox();
    private Button switchBtn=new Button();
    private VBox mainPane=new VBox();
    private SplitPane bodyPane;
    private VBox optionsPane;
    private Button[] options;
    private HBox operationsPane;
    private ScrollPane scrollPane;
    private HBox footerPane;
    private HBox printOptionPane;
    private Button printBtn;
    private List<Button>[] operations;

    //Controllers
    private ImportController importController;
    private ExportController exportController;
    private StockController stockController;
    private StatementController statementController;
    private Font currFont;
    private String[] btnTexts=new String[]{"Imports","Exports","Stocks","Statements","Report"};

    //Database
    private Connection conn;
    private final String sectionName;
    private static final String DB_URL ="jdbc:oracle:thin:@localhost:1521:xe";
    private static final String UNITECH_USERNAME ="unitech_admin";
    private static final String UNITECH_PASSWORD ="unitech@1234";
    private static final String SURYA_USERNAME ="surya_ind_admin";
    private static final String SURYA_PASSWORD ="surya@1234";

    @SuppressWarnings("unchecked")
    public MainPage(String sectionName){
        this.sectionName=sectionName.toLowerCase();
        this.primaryStage=new Stage();
        this.importController = new ImportController();
        this.exportController = new ExportController();
        this.stockController = new StockController();
        this.statementController = new StatementController();

        connectDatabase();
        initializeUI();

        this.mainScene=new Scene(mainPane);
        launchPrimaryStage(primaryStage,mainScene,heading);
    }

    private void initializeUI(){
        createHeaderPane();
        createOptionsPane();
        createBodyPane();
        createFooterPane();
        setUpMainPane();
    }

    private void createHeaderPane(){
        heading=new Label();
        heading.setStyle("-fx-font-size: 20;-fx-text-fill:white;");
        switchBtn.setText("Switch Section");
        switchBtn.setStyle("-fx-background-color:#1a5276;-fx-border-color:#2980b9;-fx-border-width:2px;-fx-text-fill:white;-fx-font-weight:bold;");
        switchBtn.setPrefHeight(35);
        switchBtn.setFocusTraversable(false);
        switchBtn.setOnAction(e ->{
            String newSectionName = sectionName.equalsIgnoreCase(UNITECH_INDUSTRIES) ? SURYA_INDUSTRIES : UNITECH_INDUSTRIES;
            new MainPage(newSectionName);
            primaryStage.close();
        });
        headingPane.setStyle("-fx-border-color: grey;-fx-border-width: 0 0 1px 0;-fx-border-style: solid;");
        leftHeadingPane.setPadding(new Insets(5,0,5,7));
        leftHeadingPane.setAlignment(Pos.CENTER_LEFT);
        leftHeadingPane.getChildren().add(heading);
        HBox.setHgrow(leftHeadingPane, Priority.ALWAYS);
        rightHeadingPane.setPadding(new Insets(2,2,2,0));
        rightHeadingPane.setAlignment(Pos.CENTER_RIGHT);
        rightHeadingPane.getChildren().add(switchBtn);
        headingPane.setAlignment(Pos.CENTER_LEFT);
        headingPane.getChildren().addAll(leftHeadingPane,rightHeadingPane);
        headingPane.setStyle("-fx-background-color:#1a5276;");
    }

    private void createOptionsPane(){
        bodyPane=new SplitPane();
        optionsPane=new VBox();
        options=new Button[5];

        options[0]=new Button(btnTexts[0]);
        options[1]=new Button(btnTexts[1]);
        options[2]=new Button(btnTexts[2]);
        options[3]=new Button(btnTexts[3]);
        options[4]=new Button(btnTexts[4]);
        optionsPane.setStyle("-fx-background-color:#1f618d;-fx-border-color:#2980b9;");
        optionsPane.setPrefWidth(140);
        optionsPane.setMaxWidth(140);
        VBox.setVgrow(optionsPane,Priority.ALWAYS);
        VBox.setVgrow(bodyPane,Priority.ALWAYS);
        HBox.setHgrow(bodyPane,Priority.ALWAYS);
        for(int i=0;i<5;i++){
            final int temp=i;
            options[i].setStyle("-fx-background-color:#1f618d;-fx-text-fill:white;");
            options[i].setOnMouseEntered(m -> options[temp].setStyle("-fx-background-color:#1a5276; -fx-text-fill: white;"));
            options[i].setOnMouseExited(m -> options[temp].setStyle("-fx-background-color:#1f618d;-fx-text-fill:white;"));
            options[i].focusedProperty().addListener((ob, b, isFocused) -> {
                if (isFocused) {
                    options[temp].setStyle("-fx-background-color: #1a5276; -fx-text-fill: white;");
                } else {
                    options[temp].setStyle("-fx-background-color:#1f618d;-fx-text-fill:white;");
                }
            });
            options[i].setPrefSize(150,40);
            addActionListener(i);
            optionsPane.getChildren().add(options[i]);
        }
        options[4].setStyle("-fx-background-color:#1f618d;-fx-text-fill:white;");
        options[4].setOnMouseEntered(m -> options[4].setStyle("-fx-background-color: #1a5276; -fx-text-fill: white;"));
        options[4].setOnMouseExited(m -> options[4].setStyle("-fx-background-color:#1f618d;-fx-text-fill:white;"));
        options[4].focusedProperty().addListener((ob, b, isFocused) -> {
            if (isFocused) {
                options[4].setStyle("-fx-background-color: #1a5276; -fx-text-fill: white;");
            } else {
                options[4].setStyle("-fx-background-color:#1f618d;-fx-text-fill:white;");
            }
        });
        currFont=options[0].getFont();
        options[0].setFont(Font.font(currFont.getFamily(), FontWeight.BOLD,16));
        selectOption(0);
        bodyPane.getItems().add(optionsPane);
    }

    private void createBodyPane(){
        scrollPane=new ScrollPane(importController.initializeImportsTable(conn));
        scrollPane.setId("scroll-pane");
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setPannable(true);
        HBox.setHgrow(scrollPane, Priority.ALWAYS);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        bodyPane.getItems().add(scrollPane);
    }

    private void createFooterPane(){
        footerPane=new HBox();
        footerPane.setStyle("-fx-background-color:#1a5276;");
        footerPane.setMinHeight(50);

        printBtn=new Button();
        printBtn.setStyle("-fx-font-weight:bold;-fx-background-color:#d4e6f1;");
        printBtn.setOnMouseEntered(m -> printBtn.setStyle("-fx-background-color:#a9cce3;"));
        printBtn.setOnMouseExited(m -> printBtn.setStyle("-fx-font-weight:bold;-fx-background-color:#d4e6f1;"));
        printBtn.setText("Print");
        printBtn.setPrefSize(120,30);
        HBox.setMargin(printBtn,new Insets(0,0,0,10));
        printOptionPane=new HBox();
        printOptionPane.getChildren().add(printBtn);
        printOptionPane.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(printOptionPane,Priority.ALWAYS);

        operationsPane=new HBox();
        operations=new ArrayList[options.length];
        initializeOperations();
        operationsPane.getChildren().clear();
        for(Button btn:operations[0])
            operationsPane.getChildren().add(btn);
        operationsPane.setAlignment(Pos.CENTER_RIGHT);
        footerPane.getChildren().addAll(printOptionPane,operationsPane);
    }

    private void setUpMainPane(){
        mainPane.getChildren().addAll(headingPane,bodyPane,footerPane);
        VBox.setVgrow(mainPane,Priority.ALWAYS);
    }

    //------------------------------------------------------------------------------
    public void launchPrimaryStage(Stage primaryStage,Scene mainScene,Label heading){
        primaryStage.setTitle(sectionName +" section");
        heading.setText((sectionName.equalsIgnoreCase(UNITECH_INDUSTRIES) ? UNITECH_INDUSTRIES+": Cold Storage" : SURYA_INDUSTRIES+": Agriculture Equipments"));
        primaryStage.setScene(mainScene);
        primaryStage.setMaximized(true);
        primaryStage.centerOnScreen();
        primaryStage.setOnCloseRequest(w -> {
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException e) {
                AlertUtils.showAlert(Alert.AlertType.WARNING,"Connection Closure!","Failed to close the connection!");
            }
        });
        primaryStage.show();
    }

    public void selectOption(int index){
        for(int i=0;i<options.length;i++){
            options[i].setText(btnTexts[i]);
            options[i].setFont(Font.font(currFont.getFamily(), FontWeight.NORMAL,14));
        }
        options[index].setFont(Font.font(currFont.getFamily(), FontWeight.BOLD,16));
    }

    public void addActionListener(int index){
        options[index].setOnAction(e ->{
            selectOption(index);
            if(index==0){
                scrollPane.setContent(importController.initializeImportsTable(conn));
                operationsPane.getChildren().clear();
                for(Button btn:operations[index])
                    operationsPane.getChildren().add(btn);
            }else if(index==1){
                scrollPane.setContent(exportController.initializeExportsTable(conn));
                operationsPane.getChildren().clear();
                for(Button btn:operations[index])
                    operationsPane.getChildren().add(btn);
            }else if(index==2){
                scrollPane.setContent(stockController.initializeStocksTable(conn));
                operationsPane.getChildren().clear();
                for(Button btn:operations[index])
                    operationsPane.getChildren().add(btn);
            }else if(index==3){
                scrollPane.setContent(statementController.loadComponents(conn));
                operationsPane.getChildren().clear();
                for(Button btn:operations[index])
                    operationsPane.getChildren().add(btn);
            }else if(index==4){
                try{
                    new ReportController(sectionName);
                } catch (Exception ex) {
                    AlertUtils.showAlert(Alert.AlertType.ERROR,"Something went wrong!","Unable to start Report Generation!");
                }
            }
        });
    }

    public void initializeOperations(){
        for(int i=0;i<options.length;i++){
            operations[i]=new ArrayList<>();
        }

        operations[0].add(new Button("Add Entry"));
        operations[0].getFirst().setOnAction(e -> importController.addEntry(scrollPane));
        operations[0].add(new Button("View/Update Entry"));
        operations[0].get(1).setOnAction(e -> importController.viewUpdateEntry(scrollPane));
        operations[0].add(new Button("Delete Entry"));
        operations[0].get(2).setOnAction(e -> importController.deleteEntry());

        operations[1].add(new Button("Add Entry"));
        operations[1].getFirst().setOnAction(e -> exportController.addEntry(scrollPane));
        operations[1].add(new Button("View/Update Entry"));
        operations[1].get(1).setOnAction(e -> exportController.viewUpdateEntry(scrollPane));
        operations[1].add(new Button("Delete Entry"));
        operations[1].get(2).setOnAction(e -> exportController.deleteEntry());

        operations[2].add(new Button("Add New Stock"));
        operations[2].getFirst().setOnAction(e ->{stockController.addStock();scrollPane.setContent(stockController.initializeStocksTable(conn));});
        operations[2].add(new Button("Update Stock"));
        operations[2].get(1).setOnAction(e ->{stockController.updateStock();scrollPane.setContent(stockController.initializeStocksTable(conn));});
        operations[2].add(new Button("Delete Stock"));
        operations[2].get(2).setOnAction(e ->{stockController.deleteStock();scrollPane.setContent(stockController.initializeStocksTable(conn));});

        for(List<Button> btnList:operations){
            for(Button btn:btnList){
                btn.setStyle("-fx-background-color:#d4e6f1;");
                btn.setOnMouseEntered(m -> btn.setStyle("-fx-background-color:#a9cce3;"));
                btn.setOnMouseExited(m -> btn.setStyle("-fx-background-color:#d4e6f1;"));
                HBox.setMargin(btn,new Insets(0,10,0,0));
                btn.setMinWidth(100);
                btn.setMinHeight(30);
            }
        }
    }

    public void connectDatabase(){
        try{
            Class.forName("oracle.jdbc.driver.OracleDriver");
            if(sectionName.equalsIgnoreCase(UNITECH_INDUSTRIES)){
                conn= DriverManager.getConnection(DB_URL, UNITECH_USERNAME, UNITECH_PASSWORD);
            }
            if(sectionName.equalsIgnoreCase(SURYA_INDUSTRIES)){
                conn= DriverManager.getConnection(DB_URL, SURYA_USERNAME, SURYA_PASSWORD);
            }
        } catch (Exception e) {
            AlertUtils.showAlert(Alert.AlertType.ERROR,"Database Error","Failed to connect to database: "+ e.getMessage());
        }
    }
}