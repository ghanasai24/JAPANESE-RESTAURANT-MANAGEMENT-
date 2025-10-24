package com.restaurant.japanese.ui;

import com.restaurant.japanese.Main;
import com.restaurant.japanese.model.Order;
import com.restaurant.japanese.model.Table;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class PaymentViewController implements AppAwareController {

    @FXML private Label subtotalLabel;
    @FXML private Label gstLabel;
    @FXML private Label grandTotalLabel;

    private Main app;
    private Order activeOrder;

    @Override
    public void setApp(Main app) {
        this.app = app;
        this.activeOrder = app.getActiveOrder();
        populateBillDetails();
    }

    private void populateBillDetails() {
        if (activeOrder == null) return;
        subtotalLabel.setText(String.format("Subtotal: ₹%.2f", activeOrder.getTotalPrice()));
        gstLabel.setText(String.format("GST (5%%): ₹%.2f", activeOrder.getGst()));
        grandTotalLabel.setText(String.format("Grand Total: ₹%.2f", activeOrder.getFinalPrice()));
    }

    @FXML
    private void handleCashPayment() {
        processPayment(Order.PaymentMethod.CASH);
    }

    @FXML
    private void handleCardPayment() {
        processPayment(Order.PaymentMethod.CARD);
    }

    @FXML
    private void handleUpiPayment() {
        processPayment(Order.PaymentMethod.UPI);
    }

    private void processPayment(Order.PaymentMethod method) {
        activeOrder.setStatus(Order.OrderStatus.PAID);
        activeOrder.setPaymentMethod(method);
        app.getOrderDAO().updateOrder(activeOrder);

        if (activeOrder.getTableId() != null) {
            app.getTableDAO().updateTableStatus(activeOrder.getTableId(), Table.TableStatus.NEEDS_CLEANING);
        }

        app.setLastPaidOrder(activeOrder);
        app.setActiveOrder(null);

        // Navigate to feedback screen
        app.getMainViewController().loadView("FeedbackView.fxml");
    }
}