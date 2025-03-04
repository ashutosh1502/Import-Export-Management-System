package com.project.application;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import java.util.ArrayList;

@SuppressWarnings("unchecked")
public class ExportController {
    private TableView<Exports> exportsTable;
    private TableColumn<Exports,String> col1,col2,col3,col4,col7;
    private TableColumn<Exports,Integer> col5;
    private TableColumn<Exports,Double> col6;

    public TableView<Exports> loadHistory(){
//        System.out.println("Called export controller");
        exportsTable = new TableView<>();
        exportsTable.setId("exports-table");
        col1=new TableColumn<>("SrNo.");
        col2=new TableColumn<>("Invoice No.");
        col3 = new TableColumn<>("Customer Name");
        col4 = new TableColumn<>("Products");
        col5= new TableColumn<>("Qty");
        col6= new TableColumn<>("Price");
        col7 = new TableColumn<>("Date");

        col1.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getSrNo()));
        col2.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getInvoiceNo()));
        col3.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCustomerName()));
        col4.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(String.join(", ", cellData.getValue().getProducts())));
        col5.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getQuantity()).asObject());
        col6.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getPrice()).asObject());
        col7.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getInvoiceDate()));

        resizeColumns();
        exportsTable.getColumns().addAll(col1,col2,col3,col4,col5,col6);
        insertData();

        return exportsTable;
    }

    public void insertData(){
        ObservableList<Exports> data= FXCollections.observableArrayList();
        exportsTable.setItems(data);

        ArrayList<String> products=new ArrayList<>();
        products.add("pr1");
        products.add("pr2");
        products.add("pr3");
        int srno=1;
        for(int i=0;i<30;i++){
            data.add(new Exports(srno++,"001","abc",products,100,150.65f,"01-01-2024"));
        }
        for(int i=0;i<30;i++){
            data.add(new Exports(srno++,"001","xyz",products,100,150.65f,"02-01-2024"));
        }
    }

    public void resizeColumns(){
        exportsTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        col1.setPrefWidth(50);
        col1.setMinWidth(50);
        col1.setResizable(true);

        exportsTable.widthProperty().addListener((observable, oldWidth, newWidth) -> {
            // Recalculate the available width excluding col1
            double totalWidth = newWidth.doubleValue() - col1.getWidth();
            double remainingColumnCount = exportsTable.getColumns().size() - 1; // Excluding col1

            // Calculate the width for the remaining columns
            if (remainingColumnCount > 0) {
                double columnWidth = totalWidth / remainingColumnCount;

                // Set the preferred width for each remaining column
                for (TableColumn<?, ?> col : exportsTable.getColumns()) {
                    if (col != col1) {
                        col.setPrefWidth(columnWidth);  // Distribute the available width equally
                        col.setMinWidth(50);
                        col.setResizable(true); // Allow resizing of other columns
                    }
                }
            }
        });
    }
}
