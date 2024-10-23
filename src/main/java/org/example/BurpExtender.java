package org.example;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class BurpExtender implements BurpExtension {
    private MontoyaApi api;
    private JTable resultTable;
    private DefaultTableModel tableModel;
    private List<String> paramsList;

    @Override
    public void initialize(MontoyaApi api) {
        this.api = api;
        api.extension().setName("CaptchaDos");
        api.logging().logToOutput("此插件由吊毛涛赞助：）");

        // 初始化参数列表
        paramsList = new ArrayList<>();

        // 添加自定义Tab到UI
        api.userInterface().registerSuiteTab("CaptchaDos" ,CustomTab());
    }

    private Component CustomTab() {

        // 创建主面板
        JPanel mainPanel = new JPanel(new BorderLayout());

        // 创建顶部面板，包含URL输入框和按钮
        JPanel topPanel = new JPanel(new FlowLayout());

        JTextField urlField = new JTextField("https://example.com",50);
        topPanel.add(new JLabel("URL:"));
        topPanel.add(urlField);

        // 创建开始按钮
        JButton startButton = new JButton("Start");
        topPanel.add(startButton);

        // 创建参数输入面板，设置默认值
        JPanel paramPanel = new JPanel(new FlowLayout());

        JTextField heightField = new JTextField("2000", 5);
        JTextField hField = new JTextField("2000", 5);
        JTextField widthField = new JTextField("2000", 5);
        JTextField wField = new JTextField("2000", 5);
        JTextField marginField = new JTextField("2000", 5);
        JTextField mField = new JTextField("2000", 5);
        JTextField sizeField = new JTextField("2000", 5);
        JTextField sField = new JTextField("2000", 5);
        JTextField paddingField = new JTextField("2000", 5);
        JTextField pField = new JTextField("2000", 5);

        paramPanel.add(new JLabel("Height:"));
        paramPanel.add(heightField);
        paramPanel.add(new JLabel("Width:"));
        paramPanel.add(widthField);
        paramPanel.add(new JLabel("Margin:"));
        paramPanel.add(marginField);
        paramPanel.add(new JLabel("Size:"));
        paramPanel.add(paddingField);
        paramPanel.add(new JLabel("Padding:"));
        paramPanel.add(sizeField);
        paramPanel.add(new JLabel("h:"));
        paramPanel.add(hField);
        paramPanel.add(new JLabel("w:"));
        paramPanel.add(wField);
        paramPanel.add(new JLabel("m:"));
        paramPanel.add(mField);
        paramPanel.add(new JLabel("s:"));
        paramPanel.add(sField);
        paramPanel.add(new JLabel("p:"));
        paramPanel.add(pField);

        // 创建表格用于显示结果
        String[] columnNames = {"URL", "Code", "Time", "Size", "Y/N"};
        tableModel = new DefaultTableModel(columnNames, 0);
        resultTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(resultTable);

        // 添加组件到主面板
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(paramPanel, BorderLayout.SOUTH); // 将参数输入面板放在上面
        mainPanel.add(scrollPane, BorderLayout.CENTER); // 文本区域在下方

        // 定义按钮点击事件
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tableModel.setRowCount(0); // 清空表格

                // 获取输入的URL
                String baseUrl = urlField.getText().trim();
                String height = heightField.getText().trim();
                String h = hField.getText().trim();
                String width = widthField.getText().trim();
                String w = wField.getText().trim();
                String margin = marginField.getText().trim();
                String m = mField.getText().trim();
                String size = sizeField.getText().trim();
                String s = sField.getText().trim();
                String padding = paddingField.getText().trim();
                String p = pField.getText().trim();

                if (!baseUrl.isEmpty()) {
                    paramsList.clear();
                    paramsList.add("height=" + height);
                    paramsList.add("h=" + h);
                    paramsList.add("width=" + width);
                    paramsList.add("w=" + w);
                    paramsList.add("m=" + m);
                    paramsList.add("margin=" + margin);
                    paramsList.add("size=" + size);
                    paramsList.add("s=" + s);
                    paramsList.add("padding=" + padding);
                    paramsList.add("p=" + p);
                    paramsList.add("height=" + height + "&width=" + width);
                    paramsList.add("h=" + h + "&w=" + w);

                    ExecutorService executorService = Executors.newFixedThreadPool(paramsList.size());

                    for (String param : paramsList) {
                        executorService.submit(() -> {
                            String urlWithParam;
                            if (baseUrl.contains("?")) {
                                urlWithParam = baseUrl + "&" + param;
                            } else {
                                urlWithParam = baseUrl + "?" + param;
                            }
                            try {
                                long startTime = System.currentTimeMillis();
                                // 创建连接并发起请求
                                URL url = new URL(urlWithParam);
                                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                                connection.setRequestMethod("GET");
                                connection.setConnectTimeout(5000); // 设置超时时间
                                connection.connect();

                                int responseCode = connection.getResponseCode();
                                int contentLength = connection.getContentLength();

                                long endTime = System.currentTimeMillis();
                                long duration = endTime - startTime;

                                String isOverFiveSeconds = duration > 2500 ? "是" : "否";

                                // 在表格中显示结果
                                SwingUtilities.invokeLater(() -> {
                                    tableModel.addRow(new Object[]{
                                            urlWithParam,
                                            responseCode,
                                            duration,
                                            contentLength,
                                            isOverFiveSeconds
                                    });
                                });
                            } catch (Exception ex) {
                                SwingUtilities.invokeLater(() -> {
                                    tableModel.addRow(new Object[]{
                                            urlWithParam,
                                            "错误",
                                            "N/A",
                                            "N/A",
                                            "N/A"
                                    });
                                });
                            }



                        });
                    }

                    executorService.shutdown();
                    try {
                        executorService.awaitTermination(10, TimeUnit.SECONDS);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                } else {
                    JOptionPane.showMessageDialog(mainPanel, "请输入有效的URL和参数", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        return  mainPanel;
    }
}
